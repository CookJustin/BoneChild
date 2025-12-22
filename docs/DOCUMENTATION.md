# Documentation Structure

## Overview
All modules now have proper documentation in dedicated `docs/` folders and README files.

## Structure

```
BoneChild/
├── README.md                          # Project overview and quick start
├── docs/
│   └── DOCUMENTATION.md              # This file - documentation guide
├── assets/
│   └── docs/
│       ├── README.md                 # Assets module guide
│       └── ASSET_SYSTEM.md          # Detailed asset system documentation
├── monsters/
│   └── docs/
│       └── README.md                 # Monsters module guide
└── engine/
    └── docs/
        └── README.md                 # Engine module guide
```

## Documentation Files

### Root Level
**README.md** - Project overview
- Module structure diagram
- Dependencies graph
- Quick start (build & run)
- Technology stack
- Roadmap

**docs/DOCUMENTATION.md** - Documentation structure guide (this file)
- How docs are organized
- Where to add new documentation

### Assets Module
**assets/docs/README.md** - Module overview
- What's in this module
- Dependencies
- Quick start code examples
- How to add new assets

**assets/docs/ASSET_SYSTEM.md** - Detailed guide
- Architecture overview
- JSON format reference
- Usage examples
- Migration path from old system

### Monsters Module
**monsters/docs/README.md** - Module overview
- Monster API structure
- All available mob types
- How to add new monsters
- Factory pattern explained

### Engine Module
**engine/docs/README.md** - Module overview
- Core systems (world, rendering, UI)
- Key features
- How to run the game
- Future refactoring plans

## Adding Documentation

When creating new features:

1. **Add to module README** - Update the "What's in this module" section
2. **Create detailed doc if complex** - Add to `{module}/docs/FEATURE_NAME.md`
3. **Update root README** - Add to roadmap or features list
4. **Keep it concise** - Code examples > long explanations

## Benefits

✅ **Discoverable** - Each module documents itself  
✅ **Maintainable** - Docs live with the code  
✅ **Onboarding** - New developers can understand structure quickly  
✅ **Modular** - Documentation is as modular as the code  

