# Playable Characters Module

## Purpose
Contains concrete implementations of playable character types. This allows multiple character classes (warrior, mage, rogue, etc.) without modifying game-core.

## Why Separate Module?

### Problem with Player in game-core:
- **game-core** should contain abstract game logic, not concrete implementations
- If we want multiple character types, we'd have to keep adding them to game-core
- Hard to extend without modifying core game logic

### Solution: playable-characters module
- **game-core** defines `PlayableCharacter` interface (contract)
- **playable-characters** provides implementations (Player, Mage, Warrior, etc.)
- Easy to add new character types without touching game-core

## Architecture

```
game-core/
  └── PlayableCharacter (interface)
       ↑ implements
       │
playable-characters/
  ├── Player (default character)
  ├── Warrior (future - tank character)
  ├── Mage (future - spell caster)
  └── Rogue (future - fast character)
```

## Current Characters

### Player
The default character with balanced stats:
- Health: 100
- Speed: 200
- Attacks with fireballs
- Can dodge
- Levels up with XP
- Has power-up system

## Adding New Character Types

To add a new character (e.g., Warrior):

### 1. Create the class
```java
package com.bonechild.playablecharacters;

public class Warrior extends LivingEntity implements PlayableCharacter {
    public Warrior(float x, float y) {
        super(x, y, 64, 64, 150f, 150f); // More health, slower
        // Warrior-specific initialization
    }
    
    @Override
    public float getAttackDamage() {
        return baseDamage * 1.5f; // Warriors hit harder
    }
    
    // ... implement other PlayableCharacter methods
}
```

### 2. Use in game
```java
// In WorldManager or Engine
PlayableCharacter player = new Warrior(centerX, centerY);
// Or
PlayableCharacter player = new Mage(centerX, centerY);
```

**That's it!** No changes to game-core needed.

## Benefits

✅ **Multiple character types** - Easy to add warrior, mage, rogue, etc.  
✅ **No game-core changes** - Add characters without modifying core logic  
✅ **Character selection** - Players can choose their character class  
✅ **Multiplayer ready** - Different players can have different characters  
✅ **Modular** - Characters are self-contained  

## Dependencies

```
playable-characters → game-core (for PlayableCharacter interface)
playable-characters → monsters (for MobEntity)
```

## Future Character Types

### Warrior (Tank)
- ✅ High health (150 HP)
- ✅ High damage (1.5x)
- ❌ Slow speed (150)
- Special: Shield ability (damage reduction)

### Mage (Glass Cannon)
- ❌ Low health (75 HP)
- ✅ High damage (2x)
- ✅ Normal speed (200)
- Special: Area-of-effect spells

### Rogue (Assassin)
- ✅ Normal health (100 HP)
- ✅ Critical hits (higher crit chance)
- ✅ Very fast (300 speed)
- Special: Invisibility/stealth

### Archer (Ranged)
- ✅ Normal health (100 HP)
- ✅ Long range attacks
- ✅ Fast speed (250)
- Special: Multi-shot ability

---

## Module Structure

```
playable-characters/
├── pom.xml
├── src/main/java/com/bonechild/playablecharacters/
│   ├── Player.java       # Default character
│   ├── Warrior.java      # Future: Tank
│   ├── Mage.java         # Future: Spell caster
│   └── Rogue.java        # Future: Fast assassin
└── docs/
    └── README.md
```

---

**Result:** Clean separation! game-core defines contracts, playable-characters provides implementations. Easy to extend! 🎯

