package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import java.awt.*;

/**
 * Class represents sunHalo object
 */
public class SunHalo {

    private static final String HALO_TAG = "sun_halo";
    private static final float HALO_SIZE_FACTOR = 2f;
    private static final Color HALO_COLOR = new Color(255, 255, 0, 20);

    /**
     * Create sunHalo object
     * @param sun for halo
     * @return the created GameObject sunHalo
     */
    public static GameObject create(GameObject sun) {
        Vector2 haloSize = sun.getDimensions().mult(HALO_SIZE_FACTOR);
        GameObject sunHalo = new GameObject(
                Vector2.ZERO,
                haloSize,
                new OvalRenderable(HALO_COLOR)
        );
        sunHalo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sunHalo.setTag(HALO_TAG);
        sunHalo.addComponent(deltaTime -> sunHalo.setCenter(sun.getCenter()));
        return sunHalo;
    }
}
