# Save State System

## Overview
BoneChild now has a save/load system that persists player progress and stats to a local JSON file.

## What Gets Saved

### Player Stats
- Level
- Experience & XP to next level  
- Gold
- Current & Max Health

### Power-Up Levels
- Speed, Strength, Grab
- Attack Speed, Max HP
- XP Boost
- Explosion Chance
- Chain Lightning
- Lifesteal

### Progression
- Current Stage ID
- Current Wave Number
- Save Timestamp

## Save File Location

**File:** `bonechild_save.json`  
**Location:** Local application data directory
- **macOS:** `~/Library/Application Support/BoneChild/`
- **Windows:** `%APPDATA%/BoneChild/`
- **Linux:** `~/.local/share/BoneChild/`

## Usage

### Save Game (Manual)
```java
worldManager.saveGame();
```

### Load Game
```java
SaveState state = worldManager.loadGame();
if (state != null) {
    // Restore player stats from state
    player.setLevel(state.level);
    player.setExperience(state.experience);
    // ... etc
}
```

### Check for Save File
```java
if (worldManager.hasSaveFile()) {
    // Show "Continue" button
}
```

## Example Save File
```json
{
  "level": 5,
  "experience": 250.5,
  "experienceToNextLevel": 500.0,
  "gold": 1250,
  "currentHealth": 120.0,
  "maxHealth": 120.0,
  "speedLevel": 2,
  "strengthLevel": 3,
  "grabLevel": 1,
  "attackSpeedLevel": 2,
  "maxHpLevel": 1,
  "xpBoostLevel": 0,
  "explosionChanceLevel": 1,
  "chainLightningLevel": 0,
  "lifestealLevel": 0,
  "currentStageId": "stage_1",
  "currentWave": 5,
  "saveTime": 1703274895432
}
```

## TODO: Integration with BoneChildGame

To fully integrate the save system, you'll need to:

1. **Add "Continue" button to menu** - Check if save file exists
2. **Auto-save on wave clear** - Save progress after each wave
3. **Restore player state on load** - Apply saved stats to player
4. **Skip to saved wave** - Use DEBUG_START_WAVE logic to jump to saved wave
5. **Delete save on death** - Optional: remove save when player dies

## Future Enhancements

- Multiple save slots
- Cloud saves
- Auto-save settings
- Save file encryption
- Backup/restore functionality

## API Reference

### WorldManager Methods

```java
// Save current game state
public void saveGame()

// Load game state (returns null if no save exists)
public SaveState loadGame()

// Check if save file exists
public boolean hasSaveFile()
```

### SaveStateManager Methods

```java
// Save state to JSON file
public void saveGame(SaveState state)

// Load state from JSON file
public SaveState loadGame()

// Check if save file exists
public boolean hasSaveFile()

// Delete save file
public void deleteSave()
```

## Notes

- Save file is human-readable JSON
- Easy to debug/modify for testing
- LibGDX handles cross-platform file paths
- No passwords/encryption (single-player game)
- Automatically creates directory if needed

**Your progress is now safe!** 💾✨

