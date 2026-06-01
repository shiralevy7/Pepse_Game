package pepse.world.avatar;

import danogl.GameObject;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.util.function.Supplier;

/**
 * Class for managing the energy scale on screen
 * Use callback to get information about energy data without knowing avatar class
 */
public class EnergyManager extends GameObject {
    private final Supplier<Integer> energySupplier;
    private TextRenderable energyText;
    private int previousEnergy;

    /**
     * Constructor for EnergyManager
     * @param topLeftCorner location scale
     * @param dimensions size cale
     * @param energySupplier callback getter for energy rate
     */
    public EnergyManager(Vector2 topLeftCorner,
                         Vector2 dimensions,
                         Supplier<Integer> energySupplier) {
        super(topLeftCorner, dimensions, new TextRenderable(""));
        this.energySupplier = energySupplier;
        this.previousEnergy = this.energySupplier.get();
        this.energyText = (TextRenderable) renderer().getRenderable();
        this.energyText.setString("Energy: " + this.energySupplier.get() + "%");

    }

    /**
     * Update energy scale on screen if there is a change
     * @param deltaTime for session
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        int currentEnergy = this.energySupplier.get();
        // update scale only if energy changed
        if (currentEnergy != this.previousEnergy) {
            this.energyText.setString("Energy: " + this.energySupplier.get() + "%");
            this.previousEnergy = currentEnergy;
        }
    }
}