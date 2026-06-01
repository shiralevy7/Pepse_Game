package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.avatar.Avatar;
import pepse.world.avatar.EnergyManager;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Flora;
import pepse.world.trees.Tree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to control the session
 */
public class PepseGameManager extends GameManager {
    private static final int SEED = 455678;
    private static final int DAY_CYCLE = 30;
    private static final int CHUNK_SIZE = 7 * Block.SIZE;
    private static final int EXTRA_CHUNKS_BUFFER = 2;
    private static final Vector2 ENERGY_UI_SIZE = new Vector2(50, 50);
    private static final int SUN_LAYER_OFFSET = 2;
    private static final int SUN_HALO_LAYER_OFFSET = 1;
    private static final int INITIAL_AVATAR_X = 0;
    private static final float HALF = 0.5f;
    private static final int LEAF_LAYER = Layer.DEFAULT - 1;
    private final Set<Integer> loadedChunks = new HashSet<>();
    private final Map<Integer, List<ObjInLayer>> objectsByChunk = new HashMap<>();

    private static class ObjInLayer {
        final GameObject obj;
        final int layer;
        ObjInLayer(GameObject obj, int layer) {
            this.obj = obj;
            this.layer = layer;
        }
    }

    private Avatar avatar;
    private Terrain terrain;
    private Flora flora;
    private Vector2 windowDimensions;
    private int windowStartX;
    private int windowEndX;
    private EnergyManager energyManager;
    private int chunkRadius;

    /**
     * Initialize the game
     * @param imageReader for game
     * @param soundReader for game
     * @param inputListener for game
     * @param windowController for game
     */
    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader,
                               UserInputListener inputListener, WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        this.windowDimensions = windowController.getWindowDimensions();
        this.chunkRadius = (int) Math.ceil(this.windowDimensions.x() / CHUNK_SIZE) + EXTRA_CHUNKS_BUFFER;
        GameObject sky = Sky.create(windowController.getWindowDimensions());
        gameObjects().addGameObject(sky, Layer.BACKGROUND);
        gameObjects().layers().shouldLayersCollide(Layer.STATIC_OBJECTS, Layer.STATIC_OBJECTS, false);
        gameObjects().layers().shouldLayersCollide(Layer.DEFAULT, Layer.STATIC_OBJECTS, true);
        gameObjects().layers().shouldLayersCollide(Layer.DEFAULT, Layer.DEFAULT - 1, false);
        GameObject night = Night.create(windowController.getWindowDimensions(), DAY_CYCLE);
        gameObjects().addGameObject(night, Layer.BACKGROUND);
        GameObject sun = Sun.create(windowController.getWindowDimensions(),  DAY_CYCLE);
        gameObjects().addGameObject(sun, Layer.BACKGROUND + SUN_LAYER_OFFSET);
        GameObject sunHalo = SunHalo.create(sun);
        gameObjects().addGameObject(sunHalo, Layer.BACKGROUND + SUN_HALO_LAYER_OFFSET);
        this.terrain = new Terrain(windowController.getWindowDimensions(), SEED);
        float avatarStartY = terrain.groundHeightAt(INITIAL_AVATAR_X) - Avatar.AVATAR_HEIGHT;
        this.avatar = new Avatar(new Vector2(0, avatarStartY),
                inputListener, imageReader);
        gameObjects().addGameObject(this.avatar);
        Vector2 initialAvatarLocation = this.avatar.getTopLeftCorner();
        Vector2 cameraFocus =
                this.windowDimensions.mult(HALF).subtract(initialAvatarLocation);
        setCamera(new Camera(avatar, Vector2.ZERO, windowDimensions, windowDimensions));
        this.energyManager = new EnergyManager(Vector2.ZERO, ENERGY_UI_SIZE, avatar::getEnergy);
        gameObjects().addGameObject(this.energyManager, Layer.UI);
        this.flora =
                new Flora(terrain::groundHeightAt,
                        windowController.getWindowDimensions(),
                        avatar::setEnergy,
                        SEED);
        int currentChunk = chunkIndexAtX(this.avatar.getTopLeftCorner().x());
        loadChunksAround(currentChunk);
    }

    /**
     * Update the game
     * @param deltaTime for session
     */
    @Override
    public void update(float deltaTime) {
        float halfWindowX = this.windowDimensions.x() * HALF;
        float halfWindowY = this.windowDimensions.y() * HALF;
        this.energyManager.setTopLeftCorner(
                new Vector2(this.camera().getCenter().x() - halfWindowX,
                        this.camera().getCenter().y() - halfWindowY));
        int currentChunk = chunkIndexAtX(this.avatar.getTopLeftCorner().x());
        loadChunksAround(currentChunk);
        unloadFarChunks(currentChunk);
        super.update(deltaTime);
    }

    /**
     * Function to create world
     * @param startX location
     * @param endX location
     * @return List of objects in world
     */
    private List<ObjInLayer> createWorld(int startX, int endX) {
        List<ObjInLayer> created = new ArrayList<>();
        List<Block> newGroundBlocks = this.terrain.createInRange(startX, endX);
        for (Block block : newGroundBlocks) {
            gameObjects().addGameObject(block, Layer.STATIC_OBJECTS);
            created.add(new ObjInLayer(block, Layer.STATIC_OBJECTS));
        }
        this.flora.createInRange(startX, endX).forEach(treePart -> {
            if (treePart.getTag().equals(Tree.TRUNK_TAG)) {
                gameObjects().addGameObject(treePart, Layer.STATIC_OBJECTS);
                created.add(new ObjInLayer(treePart, Layer.STATIC_OBJECTS));
            } else if (treePart.getTag().equals(Tree.LEAF_TAG)) {
                gameObjects().addGameObject(treePart, LEAF_LAYER);
                created.add(new ObjInLayer(treePart, LEAF_LAYER));
            } else {
                gameObjects().addGameObject(treePart);
                created.add(new ObjInLayer(treePart, Layer.DEFAULT));
            }
        });
        return created;
    }

    /**
     * Getter for block index
     * @param x location of block
     * @return block index
     */
    private int chunkIndexAtX(float x) {
        return Math.floorDiv((int) Math.floor(x), CHUNK_SIZE);
    }

    /**
     * Load all the visual implementation of the session by chunks
     * @param centerChunk current chunk in the middle of the screen
     */
    private void loadChunksAround(int centerChunk) {
        for (int chunk = centerChunk - chunkRadius; chunk <= centerChunk + chunkRadius; chunk++) {
            if (loadedChunks.contains(chunk))
                continue;
            loadChunk(chunk);
        }
    }

    /**
     * Load a single chunk of the visual implementation to the screen
     * @param chunkIndex of current chunk
     */
    private void loadChunk(int chunkIndex) {
        int startX = chunkIndex * CHUNK_SIZE;
        int endX = startX + CHUNK_SIZE;
        List<ObjInLayer> created = createWorld(startX, endX);
        objectsByChunk.put(chunkIndex, created);
        loadedChunks.add(chunkIndex);
    }

    /**
     * Unload all the visual implementation of the session by chunks
     * @param centerChunk current chunk in the middle of the screen
     */
    private void unloadFarChunks(int centerChunk) {
        List<Integer> toRemove = new ArrayList<>();
        for (int chunk : loadedChunks) {
            if (Math.abs(chunk - centerChunk) > chunkRadius) {
                toRemove.add(chunk);
            }
        }
        for (int chunk : toRemove) {
            unloadChunk(chunk);
        }
    }

    /**
     * Unload a single chunk of the visual implementation to the screen
     * @param chunkIndex of current chunk
     */
    private void unloadChunk(int chunkIndex) {
        List<ObjInLayer> objects = objectsByChunk.remove(chunkIndex);
        if (objects == null) {
            loadedChunks.remove(chunkIndex);
            return;
        }
        for (ObjInLayer o : objects) {
            gameObjects().removeGameObject(o.obj, o.layer);
        }
        loadedChunks.remove(chunkIndex);
    }

    /**
     * Main function to run the program
     * @param args arguments to run the program
     */
    public static void main(String[] args) {
        new PepseGameManager().run();
    }

}