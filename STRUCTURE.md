# BoneChild Project Structure

## Overview
BoneChild is now structured as a complete Vampire Survivors-style game with proper package organization and separation of concerns.

## Package Structure

```
com.bonechild
├── BoneChildGame.java          # Main game loop and orchestration
├── Main.java                   # Desktop launcher with LWJGL3
│
├── world/                      # Game entities and world management
│   ├── Entity.java            # Base entity class (position, velocity, collision)
│   ├── LivingEntity.java      # Living entities with health/damage
│   ├── Player.java            # Player character with leveling system
│   ├── Mob.java               # Enemy that chases player
│   └── WorldManager.java      # Spawns and updates all entities, manages waves
│
├── rendering/                  # Asset management and rendering
│   ├── Assets.java            # Loads and manages textures, fonts, sounds
│   └── Renderer.java          # Renders all game objects with proper order
│
├── input/                      # Input handling
│   └── PlayerInput.java       # WASD/Arrow key movement, action buttons
│
├── ui/                         # User interface
│   ├── GameUI.java            # HUD with health, level, exp, wave info
│   └── InventoryUI.java       # Placeholder for upgrade system
│
└── util/                       # Utility functions
    └── MathUtils.java         # Math helpers (clamp, lerp, distance, etc.)
```

## Current Features

### ✅ Implemented
- **Player Character**
  - WASD/Arrow key movement
  - Health system (100 HP)
  - Experience and leveling system
  - Movement speed: 200 units/sec
  
- **Enemy System**
  - Mobs chase the player
  - Attack on collision (10 damage/sec)
  - Health system (50 HP each)
  - Drop experience when killed (25 XP)
  
- **Wave Spawning**
  - Automatic wave spawning every 10 seconds
  - Difficulty scaling (more mobs each wave)
  - Enemies spawn at screen edges
  
- **UI/HUD**
  - Health display with color coding
  - Level indicator
  - Experience bar
  - Current wave number
  - Enemy count
  - Health bar at bottom of screen
  - Experience progress bar
  
- **Rendering**
  - Player rendered as white square (32x32)
  - Enemies rendered as red squares (32x32)
  - Health bars above entities
  - Proper render order

### 🎮 Controls
- **WASD** or **Arrow Keys**: Move player
- **ESC**: Exit game
- **I**: Toggle inventory (placeholder)

## Assets

### Current Assets
- `assets/player.png` - 32x32 white square (temporary placeholder)

### TODO: Add Assets
- Player sprite/animation
- Enemy sprites (different types)
- Weapon effects
- Background tiles
- UI elements
- Sound effects
- Background music

## Game Loop

```
1. Handle Input (PlayerInput)
2. Update World (WorldManager)
   - Update player position
   - Update enemy positions/AI
   - Check collisions
   - Spawn waves
   - Handle experience/leveling
3. Render
   - Clear screen
   - Render player
   - Render enemies
   - Render UI/HUD
```

## Next Steps to Implement

### High Priority
1. **Weapon System**
   - Auto-attacking weapons
   - Multiple weapon types
   - Projectile management
   - Damage calculation

2. **Upgrade System**
   - Level-up choices
   - Weapon upgrades
   - Stat improvements
   - Passive abilities

3. **Enemy Variety**
   - Different enemy types
   - Varied behaviors
   - Boss enemies
   - Enemy spawning patterns

### Medium Priority
4. **Polish**
   - Particle effects
   - Screen shake
   - Better sprites/animations
   - Sound effects and music
   - Death effects

5. **Persistence**
   - Save/load game state
   - Unlockable characters
   - Meta-progression
   - Statistics tracking

### Low Priority
6. **Menus**
   - Main menu
   - Pause menu
   - Settings
   - Character selection

## Building & Running

### Development
```bash
./run.sh              # Run the game
mvn clean compile     # Compile only
mvn clean package     # Build JAR
```

### Distribution
```bash
mvn clean package -Pnative-package    # Create native installer
```

This creates platform-specific installers:
- **macOS**: `.dmg` or `.pkg`
- **Windows**: `.msi` or `.exe`  
- **Linux**: `.deb` or `.rpm`

## Technical Details

- **Game Engine**: LibGDX 1.12.1
- **UI Framework**: Scene2D
- **Build Tool**: Maven
- **Java Version**: 17+
- **Window Size**: 1280x720 (resizable)
- **Target FPS**: 60 FPS (VSync enabled)

## Code Organization Principles

1. **Separation of Concerns**: Each package has a specific responsibility
2. **Entity System**: Base classes for shared behavior
3. **Manager Pattern**: WorldManager orchestrates entity lifecycle
4. **Asset Management**: Centralized asset loading and disposal
5. **Input Abstraction**: Separate input handling from game logic

## Performance Considerations

- Entity pooling (TODO)
- Spatial partitioning for collision detection (TODO)
- Asset caching
- Efficient rendering with sprite batching
- Delta time-based updates for framerate independence

---

**Status**: ✅ Core foundation complete, ready for gameplay features!
