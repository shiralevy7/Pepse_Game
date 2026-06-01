package pepse.world.trees;

import danogl.GameObject;
import danogl.util.Vector2;
import pepse.world.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Class represents vegetation on terrain
 */
public class Flora {
    private static final float TREE_PROBABILITY = 0.1f;
    private final Function<Float, Float> groundHeightAt;
    private final Vector2 windowDimensions;
    private final Consumer<Integer> adderInterface;
    private final int seed;

    /**
     * Constructor for Flora
     * @param groundHeightAt callback for terrain height at point
     * @param windowDimensions represents window on screen size
     * @param adderInterface interface to transfer for update energy
     * @param seed of random object for objects on terrain
     */
    public Flora(Function<Float, Float> groundHeightAt, Vector2 windowDimensions,
                 Consumer<Integer> adderInterface, int seed) {
        this.groundHeightAt = groundHeightAt;
        this.windowDimensions = windowDimensions;
        this.adderInterface = adderInterface;
        this.seed = seed;
    }

    /**
     * Creates trees in given range based on probability calculations
     * @param minX of session
     * @param maxX of session
     * @return List of trees
     */
    public List<GameObject> createInRange(int minX, int maxX) {
        List<GameObject> floraObjects = new ArrayList<>();

        // round to closest block
        int startX = (int) (Math.floor((float) minX / Block.SIZE) * Block.SIZE);
        for (int x = startX; x < maxX; x += Block.SIZE) {
            //create seed with hush function
            // combined with global seed creates a constant random session
            int locationSeed = Objects.hash(seed, x);
            Random random = new Random(locationSeed);
            // check if create tree in this location
            if (random.nextFloat() <= TREE_PROBABILITY) {
                float y = this.groundHeightAt.apply((float) x);
                List<GameObject> tree =
                        Tree.create(new Vector2(x, y), seed, this.windowDimensions, adderInterface);
                floraObjects.addAll(tree);
            }
        }
        return floraObjects;
    }
}