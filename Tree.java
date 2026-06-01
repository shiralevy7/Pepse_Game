package pepse.world.trees;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.utils.ColorSupplier;
import pepse.world.Block;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Class represents tree object
 */
public class Tree {
    /**
     * Constant for trunk tag
     */
    public static final String TRUNK_TAG = "trunk";

    /**
     * Constant for leaf tag
     */
    public static final String LEAF_TAG = "leaf";

    private static final Color TRUNK_COLOR = new Color(100, 50, 20);
    private static final Color LEAF_COLOR = new Color(50, 200, 30);
    private static final int FOLIAGE_GRID_SIZE = 3;
    private static final float MIN_TRUNK_HEIGHT_FACTOR = 0.2f;
    private static final float MAX_TRUNK_HEIGHT_FACTOR = 0.3f;
    private static final float TRUNK_Y_OFFSET = 2f;
    private static final float LEAF_SPAWN_PROBABILITY = 0.7f;
    private static final float FRUIT_SPAWN_PROBABILITY = 0.1f;
    private static final float LEAF_MIN_ANGLE = -5f;
    private static final float LEAF_MAX_ANGLE = 5f;
    private static final float LEAF_ANGLE_BASE_DURATION = 1.5f;
    private static final float LEAF_MIN_WIDTH_DELTA = -3f;
    private static final float LEAF_MAX_WIDTH_DELTA = 3f;
    private static final float LEAF_SIZE_BASE_DURATION = 2f;
    private static Consumer<Integer> adderInterface;

    /**
     * Create all parts of a tree
     * @param baseLocation of tree
     * @param seed for randomize creation
     * @param windowDimensions  represents window on screen size
     * @param adderInterfaceFunc interface to transfer for update energy
     * @return List of objects representing a tree
     */
    public static List<GameObject> create(
            Vector2 baseLocation, int seed, Vector2 windowDimensions, Consumer<Integer> adderInterfaceFunc) {
        adderInterface = adderInterfaceFunc;
        List<GameObject> treeParts = new ArrayList<>();
        Random random = new Random(Objects.hash(seed, baseLocation.x()));
        float minHeight = baseLocation.y() * MIN_TRUNK_HEIGHT_FACTOR;
        float maxHeight = baseLocation.y() * MAX_TRUNK_HEIGHT_FACTOR;
        // create trunk
        float trunkHeight = minHeight + random.nextFloat() * (maxHeight - minHeight);
        GameObject trunk = createTrunk(baseLocation, trunkHeight);
        treeParts.add(trunk);
        // create leafs and fruits
        Vector2 trunkTop = new Vector2(baseLocation.x(), baseLocation.y() - trunkHeight);
        treeParts.addAll(createLeafs(trunkTop, random));
        return treeParts;
    }

    /**
     * Create trunk tree
     * @param baseLocation of trunk
     * @param height of trunk
     * @return GameObject represents a trunk
     */
    private static GameObject createTrunk(Vector2 baseLocation, float height) {
        Vector2 pos = new Vector2(baseLocation.x(), baseLocation.y() - height + TRUNK_Y_OFFSET);
        GameObject trunk = new GameObject(pos, new Vector2(Block.SIZE, height),
                new RectangleRenderable(ColorSupplier.approximateColor(TRUNK_COLOR)));
        trunk.setTag(TRUNK_TAG);
        trunk.physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
        trunk.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        return trunk;
    }

    /**
     * Create leafs
     * @param trunkTop location
     * @param random for randomize creation
     * @return List of leafs
     */
    private static List<GameObject> createLeafs(Vector2 trunkTop, Random random) {
        List<GameObject> leafs = new ArrayList<>();
        for (int i = -FOLIAGE_GRID_SIZE / 2; i <= FOLIAGE_GRID_SIZE / 2; i++) {
            for (int j = -FOLIAGE_GRID_SIZE / 2; j <= FOLIAGE_GRID_SIZE / 2; j++) {
                Vector2 leafPosition = new Vector2(trunkTop.x() + (i * Block.SIZE),
                        trunkTop.y() + (j * Block.SIZE));
                Float randomizrOfCreation = random.nextFloat();
                if (randomizrOfCreation < LEAF_SPAWN_PROBABILITY) {
                    leafs.add(createLeaf(leafPosition, random));
                }
                if (randomizrOfCreation < FRUIT_SPAWN_PROBABILITY) {
                    leafs.add(new Fruit(leafPosition, adderInterface));
                }
            }
        }
        return leafs;
    }

    /**
     * Create leaf
     * @param leafPosition location
     * @param random for randomize creation
     * @return leaf GameObject
     */
    private static GameObject createLeaf(Vector2 leafPosition, Random random) {
        GameObject leaf = new GameObject(leafPosition, new Vector2(Block.SIZE, Block.SIZE),
                new RectangleRenderable(ColorSupplier.approximateColor(LEAF_COLOR)));
        leaf.setTag(LEAF_TAG);
        new ScheduledTask(leaf,
                random.nextFloat(),
                false,
                () -> {
            new Transition<>(leaf,
                    leaf.renderer()::setRenderableAngle,
                    LEAF_MIN_ANGLE,
                    LEAF_MAX_ANGLE,
                    Transition.LINEAR_INTERPOLATOR_FLOAT,
                    LEAF_ANGLE_BASE_DURATION + random.nextFloat(),
                    Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                    null);

            new Transition<>(leaf,
                    (val) -> leaf.setDimensions(new Vector2(Block.SIZE + val, Block.SIZE)),
                    LEAF_MIN_WIDTH_DELTA,
                    LEAF_MAX_WIDTH_DELTA,
                    Transition.LINEAR_INTERPOLATOR_FLOAT,
                    LEAF_SIZE_BASE_DURATION + random.nextFloat(),
                    Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                    null);
        });
        return leaf;
    }

}