# BoneChild

A modular roguelike action game built with LibGDX.

## Project Structure

BoneChild is organized into independent Maven modules:

```
BoneChild/
├── pom.xml                 # Parent POM
├── docs/                   # Project-level documentation
│   └── DOCUMENTATION.md   # Documentation structure guide
├── assets/                 # Asset management module
│   ├── docs/              
│   │   ├── README.md      # Asset module guide
│   │   └── ASSET_SYSTEM.md # Asset system documentation
│   └── src/
│       ├── main/java/     # AssetRegistry, AssetLoader, Animation
│       └── main/resources/
│           ├── player-assets.json
│           ├── effects-assets.json
│           └── monsters/  # Per-monster asset files
├── monsters/               # Monster entities and behavior
│   ├── docs/
│   │   └── README.md      # Monster module guide
│   └── src/main/java/
│       ├── api/           # MobEntity, MobFactory interfaces
│       ├── core/          # DefaultMobFactory
│       └── impl/          # Concrete monsters (Boss08B, Orc, etc.)
├── game-core/              # Core game logic and domain models
│   ├── docs/
│   │   └── README.md      # Game-core module guide
│   └── src/main/java/
│       ├── world/         # Player, WorldManager, entities
│       └── input/         # PlayerInput
├── ui/                     # User interface screens and HUD
│   ├── docs/
│   │   └── README.md      # UI module guide
│   └── src/main/java/
│       └── ui/            # GameUI, menus, screens
└── engine/                 # Game engine orchestration
    ├── docs/
    │   └── README.md      # Engine module guide
    └── src/main/java/
        ├── Main.java
        ├── BoneChildGame.java
        ├── rendering/     # Renderer, effects, camera
        └── world/         # TileMap, GhostSprite (rendering-specific)
```

## Module Dependencies

```
         ┌─────────┐
         │ assets  │
         └────┬────┘
              │
        ┌─────┴─────┬─────────┐
        │           │         │
   ┌────▼────┐ ┌───▼────┐    │
   │monsters │ │game-   │    │
   │         │ │core    │◄───┘
   └────┬────┘ └───┬────┘
        │          │
    ┌───▼────┐ ┌──┴───────┐
    │stages  │ │playable- │
    └───┬────┘ │characters│
        │      └──┬───────┘
        │         │
        │      ┌──┴───┐
        │      │  ui  │
        │      └──┬───┘
        │         │
        └─────┬───┴───┐
              │       │
          ┌───▼───────▼──┐
          │    engine    │
          └──────────────┘
```

**Key:**
- `assets` → Foundation (textures, animations)
- `monsters` → Monster entities (depends on assets)
- `game-core` → Game logic interfaces (depends on assets, monsters)
- `stages` → Wave spawning (depends on monsters)
- `playable-characters` → Character implementations (depends on game-core)
- `ui` → User interface (depends on game-core, assets)
- `engine` → Orchestration (depends on everything)

**No circular dependencies!** ✅

## Quick Start

### Build
```bash
mvn clean package
```

### Run
```bash
java -jar engine/target/bonechild-engine-1.0.0-all.jar
```

### Development
Each module can be developed independently. See module documentation:
- [Assets Module](assets/docs/README.md) - Asset system
- [Monsters Module](monsters/docs/README.md) - Monster entities
- [Stages Module](stages/docs/README.md) - Wave spawning & stages
- [Game Core Module](game-core/docs/README.md) - Core game logic
- [UI Module](ui/docs/README.md) - User interface
- [Engine Module](engine/docs/README.md) - Game orchestration
- [Documentation Guide](docs/DOCUMENTATION.md) - How docs are organized

## Key Features

### Data-Driven Assets
- No code changes to add sprites/animations
- Modular JSON manifests per module
- See [Asset System Guide](assets/docs/ASSET_SYSTEM.md)

### Modular Monster System
- Mobs spawned via factory by type ID
- Each monster defined in separate class
- Easy to add new monsters

### Wave-Based Combat
- Progressive difficulty
- Boss waves with warning screens
- Power-up selection on level up

## Technology Stack
- **Language**: Java 17
- **Framework**: LibGDX 1.12.1
- **Build**: Maven
- **Physics**: Box2D
- **Backend**: LWJGL3

## Architecture Goals

✅ **Modular** - Independent modules with clear boundaries  
✅ **Data-driven** - Game content in JSON, not Java  
✅ **Maintainable** - Small, focused files  
✅ **Extensible** - Easy to add monsters, stages, items  

## Roadmap

### Completed
- [x] Modular asset system with JSON manifests
- [x] Monster module with factory pattern
- [x] Type-based mob spawning
- [x] Basic combat and wave system
- [x] Game-core module (domain logic separation)
- [x] UI module extraction

### In Progress
- [ ] Clean up unused assets
- [ ] Renderer refactor (remove instanceof checks)

### Future
- [ ] Stage/progression module
- [ ] Items/equipment system
- [ ] Skill tree
- [ ] Multiple stages with portals
- [ ] Data-driven wave definitions

## Contributing

When adding new features:
1. Choose the appropriate module (or create a new one)
2. Update the module's README
3. Add documentation to module's `docs/` folder
4. Keep modules loosely coupled

## License
[Add license here]

