package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.ScheduledTask;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.avatar.Avatar;

import java.awt.*;
import java.util.function.Consumer;

/**
 * Class represents fruit
 * the avatar eat a fruit to get extra 10 points and it reappears after 30 seconds
 */
public class Fruit extends GameObject {
    /**
     * Constant for fruit tag
     */
    public static final String FRUIT_TAG = "fruit";

    private static final float HIDDEN_OPAQUENESS = 0.0f;
    private static final float VISIBLE_OPAQUENESS = 1.0f;
    private static final Color FRUIT_COLOR = Color.RED;
    private static final float REPAIR_TIME = 30f;
    private static final int ENERGY_GAIN = 10;
    private static final float FRUIT_PART_FROM_LEAF = 0.7f;
    private boolean isAvailable = true;
    private Consumer<Integer> adderInterface;

    /**
     * Constructor for fruit
     * @param topLeftCorner of fruit
     * @param adderInterface interface to transfer for update energy
     */
    public Fruit(Vector2 topLeftCorner, Consumer<Integer> adderInterface) {
        super(topLeftCorner,
                new Vector2(Block.SIZE * FRUIT_PART_FROM_LEAF, Block.SIZE * FRUIT_PART_FROM_LEAF),
                new OvalRenderable(FRUIT_COLOR));
        this.adderInterface = adderInterface;
        this.setTag(FRUIT_TAG);
    }

    /**
     * Manage behavior when fruit collides with an object
     * @param other object
     * @param collision
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        if (other.getTag().equals(Avatar.AVATAR_TAG) && this.isAvailable) {
            eatFruit();
        }
    }

    /**
     * Manage behavior when a fruit is behavior
     */
    private void eatFruit() {
        this.isAvailable = false;
        this.adderInterface.accept(ENERGY_GAIN);
        this.renderer().setOpaqueness(HIDDEN_OPAQUENESS);
        new ScheduledTask(this,
                REPAIR_TIME,
                false,
                () -> {
            this.isAvailable = true;
            this.renderer().setOpaqueness(VISIBLE_OPAQUENESS);
        });
    }

}