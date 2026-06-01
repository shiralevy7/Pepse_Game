# Pepse_Game
Procedural 2D game world and physics engine implemented in Java with optimized memory and state management.
Pepse - a 2D Game World & Engine
A robust 2D game world engine implemented in Java, featuring procedural terrain generation, object physics, and an optimized state management system.
Developed as a collaborative project.

1. The Avatar
How did we implement the Avatar:
It is located in the pepse.world.avatar package.
It is made of 2 classes:
the Avatar class and the EnergyManager.

The EnergyManager class:
This is the UI component responsible for displaying the current energy level as text for the user.
Inherits from GameObject and overrides the update function.

The Avatar class:
It represents an instance of the game avatar. Inherits from GameObject class.
The class is responsible for physical properties, handling user input and managing internal energy levels.
Public functions we have added (will explain later):
Getter and setter functions for the avatar's energy levels.

The relationship between them & how we designed the avatar's energy changes:
As compatible with OOP principles, the 2 classes are connected using functional intefaces.
We defined a public getter function for the avatar's energy level.
This function is sent to the EnergyManager class as a Supplier<Integer>.
This allows the manager to access energy data without "knowing" what is the avatar,
or whose energy it represents.
We have also defined a public setter function for the avatar's energy level.
This method is sent as a reference to the class representing a fruit, so that the avatar's energy level will
be updated without the fruit and avatar "knowing" each other.

How we designed the avatar's display changes:
The avatar has 3 states: idle/ run/ jump.
We updated the avatar's states in the update function (which we overrided from GameObject).
Jump state: we set the avatar's display to jump when the velocity of y was not 0.
Run state: we set the avatar's display to run when the y velocity was 0 and the x velocity was not 0.
Idle state: we set the avatar's display to idle when both velocities (x and y) were 0.
Practically- each state was set using AnimationRenderable.

How we updated the energy's display:
It is displayed in the Layer.UI level, so that it will not disturb the game's performance.
We overrided the update function of the GameObject class.
In every update, we tested checked what was the avatar's energy level using the functional inteface,
and updated the display accordingly.
We only updated the enrgy display if the enrgy really changed, by saving the previous energy.
This is more efficient.

2. The trees package
Here we have 3 classes:

Flora-
This class acts as a high level manager who is responsible for 
determining where trees should be planted within a given range.
It uses a biased coin flip logic (10% probability)
to decide if a tree should be generated in a specific column.

Tree-
A class used to construct the physical structure of a tree.
It creates a trunk with a random height and a 3x3 or 5x5 grid of leaves and fruits around the top.

Fruit-
Inherits from GameObject. This class handles collision logic with the Avatar.
It manages its own visibility and energy granting logic,
disappearing upon consumption and reappearing after a 30-second cycle.
It receives a function interface to be able to update the avatar's energy level.

The connection between them:
The system utilizes functional callbacks to decouple components,
strictly adhering to the principle of "programming to an interface, not an implementation".
Flora and Terrain: Flora has a Function<Float, Float> callback to retrieve ground heights,
ensuring it remains entirely independent of the Terrain class.
Fruit and Avatar: Fruit uses a Consumer<Integer> 
callback to update energy levels, removing any direct dependency on the Avatar class.
Orchestration: PepseGameManager glues the system together by injecting method references
(e.g., avatar::setEnergy) during initialization.

3. API changes:
Flora Constructor: We updated the constructor to receive an int seed and
a Consumer<Integer> adderInterface.
This allows the class to generate consistent trees using a deterministic
seed and pass the energy update logic down to the fruits.

Tree.create Signature: The static method was modified to accept the adderInterface callback.
This was necessary to decouple the Fruit objects (created within this method) from the Avatar class.

PepseGameManager Update Logic: We moved the super.update(deltaTime) call to the end of the method.
This ensures that the procedural generation and chunk loading occur before the physics engine calculates
collisions for the current frame.

Chunk Based World Generation:
Instead of a simple createInRange call in the initialization,
we moved to a chunk based system using loadChunksAround and unloadFarChunks to manage memory effectively
in an infinite world.
