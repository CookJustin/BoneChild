# Hitbox System for Mobs

## Overview
This document explains the reusable hitbox system that provides accurate collision detection for mobs with large sprite displays but smaller actual hitboxes.

## The Perfect Scaling Formula

### Current Mob Setup (Enemy_17_B)
```java
// Sprite dimensions: 48x48 pixels (original PNG size)
// Display size: 240x240 pixels (5x scale for visibility)
// Hitbox size: 20x20 pixels (tight collision box)

public Mob(float x, float y, Player target) {
    super(x, y, 240, 240, 50f, 100f); // Display size: 240x240 (5x the 48px sprite)
    this.target = target;
    this.damage = 10f;
    this.attackCooldown = 1f;
    this.timeSinceLastAttack = 0;
    
    // Set smaller hitbox (20x20) centered on the large sprite for better accuracy
    // Offset: (240 - 20) / 2 = 110 pixels from bottom-left to center the hitbox
    setHitbox(20, 20, 110, 110);
}
```

## Formula for Any Mob

### Step 1: Determine Your Sizes
- **Original Sprite Size**: The actual PNG dimensions (e.g., 48x48)
- **Display Scale**: How much to scale up for visibility (e.g., 5x)
- **Display Size**: Original × Scale (e.g., 48 × 5 = 240)
- **Hitbox Size**: The tight collision box matching the character body (e.g., 20x20)

### Step 2: Calculate Centered Offset
```java
// For a square hitbox (width = height):
float offset = (displaySize - hitboxSize) / 2;

// Example: (240 - 20) / 2 = 110
```

### Step 3: Apply the Formula
```java
public YourMob(float x, float y, Player target) {
    // Constructor: x, y, displayWidth, displayHeight, speed, maxHealth
    super(x, y, DISPLAY_SIZE, DISPLAY_SIZE, speed, health);
    
    // setHitbox(width, height, offsetX, offsetY)
    // Centering formula: offset = (displaySize - hitboxSize) / 2
    setHitbox(HITBOX_SIZE, HITBOX_SIZE, OFFSET, OFFSET);
}
```

## Example Configurations

### Small Enemy (32px sprite, 3x scale)
```java
// Sprite: 32x32, Display: 96x96, Hitbox: 16x16
super(x, y, 96, 96, 75f, 50f);
setHitbox(16, 16, 40, 40); // offset = (96 - 16) / 2 = 40
```

### Medium Enemy (48px sprite, 5x scale) - CURRENT
```java
// Sprite: 48x48, Display: 240x240, Hitbox: 20x20
super(x, y, 240, 240, 50f, 100f);
setHitbox(20, 20, 110, 110); // offset = (240 - 20) / 2 = 110
```

### Large Boss (64px sprite, 6x scale)
```java
// Sprite: 64x64, Display: 384x384, Hitbox: 30x30
super(x, y, 384, 384, 30f, 500f);
setHitbox(30, 30, 177, 177); // offset = (384 - 30) / 2 = 177
```

### Rectangular Hitbox Example
```java
// Sprite: 48x48, Display: 240x240, Hitbox: 24x16 (wider than tall)
super(x, y, 240, 240, 50f, 100f);
// offsetX = (240 - 24) / 2 = 108
// offsetY = (240 - 16) / 2 = 112
setHitbox(24, 16, 108, 112);
```

## Why This Works

1. **Large Display**: Makes the mob visible and easy to see (240x240)
2. **Small Hitbox**: Provides precise collision detection (20x20)
3. **Centered Offset**: Keeps the hitbox centered on the visible sprite
4. **Consistent Formula**: Works for any size mob with any scale factor

## Quick Reference Table

| Sprite Size | Scale | Display Size | Suggested Hitbox | Offset Calc |
|-------------|-------|--------------|------------------|-------------|
| 32x32       | 3x    | 96x96        | 16x16            | (96-16)/2 = 40 |
| 48x48       | 4x    | 192x192      | 20x20            | (192-20)/2 = 86 |
| **48x48**   | **5x**| **240x240**  | **20x20**        | **(240-20)/2 = 110** ⭐ |
| 64x64       | 5x    | 320x320      | 28x28            | (320-28)/2 = 146 |
| 64x64       | 6x    | 384x384      | 30x30            | (384-30)/2 = 177 |

⭐ = Current working configuration for Enemy_17_B mobs

## Implementation Checklist

When adding a new mob type:

1. ✅ Determine original sprite dimensions
2. ✅ Choose appropriate display scale (3x-6x recommended)
3. ✅ Calculate display size (sprite × scale)
4. ✅ Choose hitbox size (usually 40-60% of sprite size)
5. ✅ Calculate centered offset using formula
6. ✅ Apply in constructor using `super()` and `setHitbox()`
7. ✅ Test collision detection in-game
8. ✅ Adjust hitbox size if needed (offset formula stays the same)

## Notes

- The hitbox size should match the solid part of the sprite (character body, not decorative elements)
- For Enemy_17_B, the 20x20 hitbox matches the creature's body perfectly
- The offset formula automatically centers any hitbox on any display size
- This system works with both square and rectangular hitboxes
- Health bars are positioned relative to the hitbox, not the display size
