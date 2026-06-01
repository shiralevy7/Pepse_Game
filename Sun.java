package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Class represents sun object
 */
public class Sun {
    private static final String SUN_TAG = "sun";
    private static final float SUN_SIZE = 100f;
    private static final float INITIAL_SUN_ANGLE = 0f;
    private static final float FINAL_SUN_ANGLE = 360f;
    private static final float INITIAL_HEIGHT_FACTOR = (float) 1/3;
    private static final float HORIZON_HEIGHT = (float) 2/3;

    /**
     * Create sun object
     * @param windowDimensions represents window on screen size
     * @param cycleLength of full day
     * @return the created GameObject sun
     */
    public static GameObject create(Vector2 windowDimensions, float
            cycleLength) {
        float initialX = windowDimensions.x() / 2;
        float initialY = windowDimensions.y() * INITIAL_HEIGHT_FACTOR;
        Vector2 initialSunCenter = new Vector2(initialX, initialY);

        GameObject sun = new GameObject(
                new Vector2(initialSunCenter.x() - SUN_SIZE/2,
                        initialSunCenter.y() - SUN_SIZE/2),
                new Vector2(SUN_SIZE, SUN_SIZE),
                new OvalRenderable(Color.YELLOW)
        );
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag(SUN_TAG);

        float groundHeightAtX0 = windowDimensions.y() * HORIZON_HEIGHT;
        Vector2 cycleCenter = new Vector2(windowDimensions.x() / 2, groundHeightAtX0);

        new Transition<Float>(
                sun,
                (Float angle) -> sun.setCenter(
                        initialSunCenter.subtract(cycleCenter)
                                .rotated(angle)
                                .add(cycleCenter)),
                INITIAL_SUN_ANGLE,
                FINAL_SUN_ANGLE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );
        return sun;
    }

}
