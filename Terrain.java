package pepse.world;

import danogl.gui.WindowController;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.utils.ColorSupplier;
import pepse.utils.NoiseGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class represents terrain
 */
public class Terrain {
    private static final float GROUND_HEIGHT_SETTER = (float) 2/3;
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
    private static final int TERRAIN_DEPTH = 20;
    private static final String GROUND_TAG = "ground";
    private static final int TERRAIN_WAVELENGTH = 7;

    private float groundHeightAtX0;
    private final Vector2 windowDimensionds;
    private final NoiseGenerator noiseGenerator;

    /**
     * Constructor for terrain
     * @param windowDimensions represents window on screen size
     * @param seed of random object for objects on terrain
     */
    public Terrain(Vector2 windowDimensions, int seed) {
        this.groundHeightAtX0 = windowDimensions.y() * GROUND_HEIGHT_SETTER;
        this.noiseGenerator = new NoiseGenerator(seed, (int) this.groundHeightAtX0);
        this.windowDimensionds = windowDimensions;
    }

    /**
     * Getter for ground height
     * @param x location on screen
     * @return height terrain in point x + noise
     */
    public float groundHeightAt(float x) {
        float noise = (float) this.noiseGenerator.noise(x, Block.SIZE * TERRAIN_WAVELENGTH);
        return groundHeightAtX0 + noise;
    }

    /**
     * Create blocks in terrain
     * @param minX on screen
     * @param maxX on screen
     * @return List of terrain blocks
     */
    public List<Block> createInRange(int minX, int maxX) {
        List<Block> blocks = new ArrayList<>();

        int firstBlockX = (int) (Math.floor((double) minX / Block.SIZE) * Block.SIZE);
        int lastBlockX = (int) (Math.ceil((double) maxX / Block.SIZE) * Block.SIZE);

        for (int x = firstBlockX; x < lastBlockX; x += Block.SIZE) {
            float rawHeight = groundHeightAt(x);
            float topY = (float) Math.floor(rawHeight / Block.SIZE) * Block.SIZE;

            for (int i = 0; i < TERRAIN_DEPTH; i++) {
                float currentY = topY + (i * Block.SIZE);

                var renderable = new RectangleRenderable(
                        ColorSupplier.approximateColor(BASE_GROUND_COLOR));

                Block block = new Block(new Vector2(x, currentY), renderable);
                block.setTag(GROUND_TAG);
                block.physics().preventIntersectionsFromDirection(Vector2.ZERO);
                blocks.add(block);
            }
        }
        return blocks;
    }
}
