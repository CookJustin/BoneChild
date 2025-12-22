# Assets Module

## Purpose
Shared asset management system for BoneChild. Provides textures and animations to all other modules through a data-driven registry.

## What's in this module
- **AssetRegistry** - String-based lookup for textures and animations
- **AssetLoader** - JSON manifest parser
- **Animation** - Frame-based animation wrapper
- **Assets** (compatibility) - Wrapper maintaining old API

## Dependencies
- LibGDX Core (textures, file I/O)

## Used By
- `engine` - Game logic, UI, rendering
- `monsters` - Monster/boss animations

## Documentation
- [Asset System Guide](./docs/ASSET_SYSTEM.md) - How to add and use assets

## Asset Manifests
Asset definitions are loaded from modular JSON files:
- `player-assets.json` - Player sprites
- `monsters-assets.json` - Monster/boss sprites  
- `effects-assets.json` - Particles, projectiles, pickups
- `ui-assets.json` - UI elements (future)
- `stages-assets.json` - Stage backgrounds (future)

## Quick Start

### Loading Assets
```java
Assets assets = new Assets();
assets.load(); // Auto-loads all *-assets.json files

AssetRegistry registry = assets.getRegistry();
```

### Using Assets
```java
// Get a texture
Texture tex = registry.getTexture("boss08b_sheet");

// Get an animation (independent copy)
Animation anim = registry.getAnimation("player_walk");
anim.update(delta);
TextureRegion frame = anim.getCurrentFrame();
```

### Adding New Assets
1. Place image files in `src/main/resources/assets/`
2. Register in appropriate `*-assets.json` file
3. Reference by ID in code

No Java code changes or recompilation needed!

