# Pong


# Dev Note
## What should it look like?
- A simple pong game(step 1)
- Add advanced physics - change gravity, friction, etc (step 2)
- Make it funny - add some meme (boykisser ball, rubber chicken , etc) (step 3)

## Plan:
- A main thread to handle user input
- A thread to calculate physics & render graphics
- Physics handled using GDX

## Physic key components:
- AABB: axis aligned bounding box
- Checking collision (for a single time step):
  - For each line:
    - Has a base point (lowest)
    - Has height or highest point (haven't decided yet)
  - If the higher base point is lower than the lower highest point, then there is a collision

    ~~proof by my four finger tips and internet~~

- CCD: continuous collision detection
  - Basically calculate the line formula and check collision when two line's x coord is the same.

    ------ I think I should focus on the "program" itself, not the things behind the scenes ------
