package pepse.world.avatar;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.world.trees.Fruit;

import java.awt.event.KeyEvent;

/**
 * Class represents avatar object
 */
public class Avatar extends GameObject {
    /**
     * Constant for object avatar size width
     */
    public static final int AVATAR_WIDTH = 50;

    /**
     * Constant for object avatar size height
     */
    public static final int AVATAR_HEIGHT = 50;

    /**
     * Constant for object avatar tag
     */
    public static final String AVATAR_TAG = "avatar";

    private static final String AVATAR_IMAGE_IDLE_0 = "src/assets/idle/idle_0.png";
    private static final String AVATAR_IMAGE_IDLE_FILE = "src/assets/idle";
    private static final String AVATAR_IMAGE_RUN_FILE = "src/assets/run";
    private static final String AVATAR_IMAGE_JUMP_FILE = "src/assets/jump";
    private static final int ENERGY_FOR_WALKING_ON_GROUND = 2;
    private static final int MAX_NUMBER_OF_JUMPS_FOR_DUBLE_JUMP = 2;
    private static final int ENERGY_FOR_REGULAR_JUMP = 20;
    private static final int ENERGY_ADDED_TO_COMPLETE_REGULAR_JUMP_TO_DOUBLE_JUMP = 30;
    private static final float VELOCITY_X = 200;
    private static final float VELOCITY_Y = -300;
    private static final float GRAVITY = 400;
    private static final int MAX_ENERGY = 100;
    private static final int MIN_ENERGY = 0;
    private static final float TIME_BETWEEN_CLIPS = 0.1f;
    private int energy = MAX_ENERGY;
    private final UserInputListener inputListener;
    private final Renderable idleRenderable;
    private final Renderable runAnimation;
    private final Renderable jumpAnimation;
    private boolean isKeySpacePressed = false;
    private int jumpCounter = 0;

    /**
     * Constructor for avatar
     * @param topLeftCorner of object
     * @param inputListener for avatar
     * @param imageReader for avatar
     */
    public Avatar(Vector2 topLeftCorner,
                  UserInputListener inputListener,
                  ImageReader imageReader) {
        super(topLeftCorner,
                new Vector2(AVATAR_WIDTH, AVATAR_HEIGHT),
                imageReader.readImage(AVATAR_IMAGE_IDLE_0, true));
        this.inputListener = inputListener;
        this.idleRenderable = 
                new AnimationRenderable(AVATAR_IMAGE_IDLE_FILE, imageReader, true, TIME_BETWEEN_CLIPS);
        this.runAnimation =
                new AnimationRenderable(AVATAR_IMAGE_RUN_FILE, imageReader, true, TIME_BETWEEN_CLIPS);
        this.jumpAnimation =
                new AnimationRenderable(AVATAR_IMAGE_JUMP_FILE, imageReader, true, TIME_BETWEEN_CLIPS);
        this.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        transform().setAccelerationY(GRAVITY);
        this.setTag(AVATAR_TAG);
    }

    /**
     * Update avatar according to key pressed
     * @param deltaTime for session
     */
    @Override
    public void update(float deltaTime) {
        float xVel = 0;
        boolean isOnGround = getVelocity().y() == 0;
        if (isOnGround) {
            this.jumpCounter = 0;
        }

        if (inputListener.isKeyPressed(KeyEvent.VK_RIGHT) &&
                inputListener.isKeyPressed(KeyEvent.VK_LEFT));
        // left/right movement
        else {
            if (inputListener.isKeyPressed(KeyEvent.VK_LEFT)) {
                // movement on ground
                if (isOnGround && this.energy >= ENERGY_FOR_WALKING_ON_GROUND) {
                    xVel -= VELOCITY_X;
                    this.energy -= ENERGY_FOR_WALKING_ON_GROUND;
                    renderer().setIsFlippedHorizontally(true);
                }
                // free movment in air without energy consumption
                else if (!isOnGround) {
                    xVel -= VELOCITY_X;
                    renderer().setIsFlippedHorizontally(true);
                }
            }
            if (inputListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
                // movement on ground
                if (isOnGround && this.energy >= ENERGY_FOR_WALKING_ON_GROUND) {
                    xVel += VELOCITY_X;
                    this.energy -= ENERGY_FOR_WALKING_ON_GROUND;
                    renderer().setIsFlippedHorizontally(false);
                }
                // free movment in air without energy consumption
                else if (!isOnGround) {
                    xVel += VELOCITY_X;
                    renderer().setIsFlippedHorizontally(false);
                }
            }
        }
        transform().setVelocityX(xVel);
        // jump
        if (inputListener.isKeyPressed(KeyEvent.VK_SPACE) && !this.isKeySpacePressed
        && this.jumpCounter < MAX_NUMBER_OF_JUMPS_FOR_DUBLE_JUMP) {
            if (isOnGround && this.energy >= ENERGY_FOR_REGULAR_JUMP) {
                // regular jump
                transform().setVelocityY(VELOCITY_Y);
                this.energy -= ENERGY_FOR_REGULAR_JUMP;
                this.isKeySpacePressed = true;
                this.jumpCounter++;
                isOnGround = false;
            }
            else if (!isOnGround && isFalling() &&
                    this.energy >= ENERGY_ADDED_TO_COMPLETE_REGULAR_JUMP_TO_DOUBLE_JUMP) {
                // double jump
                transform().setVelocityY(VELOCITY_Y);
                this.energy -= ENERGY_ADDED_TO_COMPLETE_REGULAR_JUMP_TO_DOUBLE_JUMP;
                this.isKeySpacePressed = true;
                this.jumpCounter++;
            }
        }
        else if (this.isKeySpacePressed) {
            this.isKeySpacePressed = false;
        }
        // load energy on ground
        if (isOnGround && xVel == 0 && this.energy < MAX_ENERGY) {
            this.energy ++;
        }
        updateAnimation();
        super.update(deltaTime);
    }

    /**
     * Getter for energy
     * @return energy
     */
    public int getEnergy() {
        return this.energy;
    }

    /**
     * Setter for energy
     * @param addition to energy
     */
    public void setEnergy(int addition) {
        this.energy = Math.min(this.energy + addition, MAX_ENERGY);
    }

    /**
     * Check if avatar is falling
     * @return true if falling else false
     */
    private boolean isFalling() {
        return this.getVelocity().y() > 0;
    }

    /**
     * Update images according to avatar movements
     */
    private void updateAnimation() {
        if (getVelocity().y() != 0) {
            renderer().setRenderable(jumpAnimation);
        } else if (getVelocity().x() != 0) {
            renderer().setRenderable(runAnimation);
        } else {
            renderer().setRenderable(idleRenderable);
        }
    }

}