# Engine Module

## Purpose
Game engine orchestration for BoneChild. Contains the main game loop, rendering pipeline, and coordinates between game-core, UI, and asset systems.

## What's in this module

### Core (`com.bonechild`)
- **Main** - Application entry point
- **BoneChildGame** - Main game class, screen management, game loop orchestration

### Rendering (`com.bonechild.rendering`)
- **Renderer** - Master renderer for all game objects
- **CameraShake** - Screen shake effects
- **ParticleSystem** - Blood, sparks, celebration particles
- **ScreenEffects** - Flash effects and post-processing
- **DamageNumber** - Floating damage text

### World (Rendering) (`com.bonechild.world`)
- **TileMap** - Background tilemap rendering
- **GhostSprite** - Ghost trail effects

## Dependencies
- `game-core` - For Player, WorldManager, game logic
- `ui` - For UI screens and HUD
- `assets` - Asset management
- `monsters` - Mob entities and factory
- LibGDX Core, Backend (LWJGL3), Box2D, FreeType

## Module Structure
```
engine/
├── src/main/java/com/bonechild/
│   ├── Main.java              # Entry point
│   ├── BoneChildGame.java     # Main game orchestration
│   ├── rendering/             # Graphics pipeline
│   └── world/                 # Rendering-specific (TileMap, etc.)
```

## Key Responsibilities

### Game Loop Orchestration
1. **Initialize** - Create assets, game-core, UI systems
2. **Update** - Call game-core update, handle input
3. **Render** - Render world, then UI overlay
4. **Cleanup** - Dispose resources

### Screen Management
- Manages game states (menu, playing, paused, game over)
- Switches between UI screens
- Coordinates UI callbacks with game logic

### Rendering Pipeline
- Renders game world from game-core state
- Applies visual effects (camera shake, particles, damage numbers)
- Overlays UI on top

## What Engine Does NOT Do

❌ **Game Logic** - Now in `game-core` module  
❌ **UI Rendering** - Now in `ui` module  
❌ **Asset Loading** - Now in `assets` module  
❌ **Monster Behavior** - Now in `monsters` module

## Architecture Role

Engine is the **orchestration layer**:

```
┌────────────────────────────────────┐
│          Engine                    │
│  ┌──────────────────────────────┐ │
│  │   BoneChildGame              │ │
│  │                              │ │
│  │   • Initialize systems       │ │
│  │   • Run game loop            │ │
│  │   • Coordinate modules       │ │
│  │   • Handle high-level flow   │ │
│  └──────────────────────────────┘ │
└────────────────────────────────────┘
         │         │         │
    ┌────┴────┐ ┌──┴───┐ ┌──┴──┐
    │game-core│ │  ui  │ │ren- │
    │         │ │      │ │ der │
    └─────────┘ └──────┘ └─────┘
```

## Running the Game

### From Maven
```bash
mvn clean package
java -jar engine/target/bonechild-engine-1.0.0-all.jar
```

### From IDE
Run `com.bonechild.Main` main class

## Benefits of Modular Architecture

✅ **Separation of concerns** - Each module has clear purpose  
✅ **Testability** - Can test game logic without rendering  
✅ **Maintainability** - Changes isolated to specific modules  
✅ **Reusability** - Game-core could be used for server/replay  

## Future Refactoring
- [ ] Extract rendering into separate module
- [ ] Move stage/progression to dedicated module
- [ ] Data-driven wave definitions
- [ ] Event system for loose coupling

