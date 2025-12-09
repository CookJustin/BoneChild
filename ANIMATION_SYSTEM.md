# Player Animation System

## Overview
Implemented a complete sprite animation system for the player character using the skeleton sprite sheet.

## Implementation Details

### 1. Animation Class (`rendering/Animation.java`)
- Handles sprite sheet cutting and frame management
- Supports looping and non-looping animations
- Frame-based animation with configurable duration
- Automatic frame progression based on delta time

### 2. Updated Assets (`rendering/Assets.java`)
- Loads `SkeletonSpriteSheet.png` (576x192 pixels)
- Creates three animations from the sprite sheet:
  - **Idle Animation** (Row 0): 9 frames, 0.1s per frame, looping
  - **Walk Animation** (Row 1): 9 frames, 0.08s per frame, looping
  - **Attack Animation** (Row 2): 9 frames, 0.06s per frame, non-looping

### 3. Player Animation States (`world/Player.java`)
Added animation state management:
- **AnimationState enum**: IDLE, WALKING, ATTACKING
- Automatic state detection based on velocity
- Direction tracking (facing left/right)
- State change detection for animation resets

```java
public enum AnimationState {
    IDLE, WALKING, ATTACKING
}
```

### 4. Renderer Updates (`rendering/Renderer.java`)
- Selects appropriate animation based on player state
- Updates animation frames with delta time
- Handles sprite flipping for left/right facing
- Resets animations when state changes

## Sprite Sheet Configuration

**File**: `SkeletonSpriteSheet.png`
- **Dimensions**: 576x192 pixels (9 frames x 3 rows)
- **Frame Size**: 64x64 pixels
- **Layout**:
  - Row 0 (Y: 0-64): Idle animation
  - Row 1 (Y: 64-128): Walk animation  
  - Row 2 (Y: 128-192): Attack animation

## Animation Behavior

### Idle State
- Triggered when player velocity is zero
- Loops continuously
- Slower animation (0.1s per frame)

### Walking State
- Triggered when player is moving (velocity > 0)
- Loops continuously
- Medium speed animation (0.08s per frame)
- Sprite flips based on movement direction

### Attacking State (Future)
- Can be triggered with `player.attack()` method
- Plays once (non-looping)
- Fastest animation (0.06s per frame)
- Returns to idle/walk after completion

## Player Size Update
- Changed player size from 32x32 to 64x64 to match sprite dimensions
- Updated collision detection accordingly
- Mobs also resized to 48x48 for better balance

## Controls Unchanged
- **WASD** or **Arrow Keys**: Move (triggers walk animation)
- **ESC**: Exit game
- **I**: Toggle inventory

## Technical Features

1. **Frame-based Animation**: Uses TextureRegion array for efficient frame storage
2. **Delta Time Updates**: Frame-rate independent animation
3. **State Management**: Automatic state transitions based on gameplay
4. **Sprite Flipping**: Horizontal flip for left-facing direction
5. **Animation Reset**: Smooth transitions between states

## Future Enhancements

- [ ] Add attack animation trigger (currently placeholder)
- [ ] Add death animation
- [ ] Add hit/damage animation
- [ ] Add special ability animations
- [ ] Sound effects for state changes
- [ ] Particle effects for attacks

## Performance Notes

- TextureRegion caching for efficient rendering
- Single sprite sheet reduces texture binding overhead
- Animation state only updated when needed
- Sprite flipping done without creating new textures

---

**Result**: Player now displays as an animated skeleton character that idles when standing still and walks when moving! The animation smoothly transitions between states and faces the correct direction based on movement.
