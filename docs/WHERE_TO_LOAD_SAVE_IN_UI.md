# 📍 Where to Load Save in the UI - Complete Guide

## Current Implementation Status

✅ **The save loading UI is FULLY IMPLEMENTED and working!**

## Where the Save Loading Happens

### 1. **Menu Screen (Main Entry Point)**
**File:** `ui/src/main/java/com/bonechild/ui/MenuScreen.java`

When you launch the game, the **MenuScreen** automatically checks for a save file and shows the appropriate buttons:

```
┌─────────────────────────────────────┐
│                                     │
│         BONE CHILD GAME             │
│                                     │
│   ╔═════════════════════════╗      │
│   ║      CONTINUE           ║ ⬅ Shows if save exists (Wave 4)
│   ╚═════════════════════════╝      │
│   ┌─────────────────────────┐      │
│   │      NEW GAME           │      │
│   └─────────────────────────┘      │
│   ┌─────────────────────────┐      │
│   │      SETTINGS           │      │
│   └─────────────────────────┘      │
│   ┌─────────────────────────┐      │
│   │     EXIT GAME           │      │
│   └─────────────────────────┘      │
└─────────────────────────────────────┘
```

**If no save exists:**
```
┌─────────────────────────────────────┐
│                                     │
│         BONE CHILD GAME             │
│                                     │
│   ╔═════════════════════════╗      │
│   ║     START GAME          ║      │
│   ╚═════════════════════════╝      │
│   ┌─────────────────────────┐      │
│   │      SETTINGS           │      │
│   └─────────────────────────┘      │
│   ┌─────────────────────────┐      │
│   │     EXIT GAME           │      │
│   └─────────────────────────┘      │
└─────────────────────────────────────┘
```

### 2. **How It Works**

#### Step 1: Menu Initialization
```java
// In MenuScreen constructor:
this.hasSaveFile = callback.hasSaveFile();
```

The menu asks BoneChildGame if a save file exists.

#### Step 2: BoneChildGame Checks Save
```java
// In BoneChildGame.java:
@Override
public boolean hasSaveFile() {
    if (worldManager != null) {
        return worldManager.hasSaveFile();
    }
    WorldManager tempWM = new WorldManager(new Player(0, 0));
    return tempWM.hasSaveFile();
}
```

This checks if `~/Library/Application Support/BoneChild/bonechild_save.json` exists.

#### Step 3: Button Click Handling

**CONTINUE Button Clicked:**
```java
// In MenuScreen:
if (continueButton != null && continueButton.contains(mouseX, mouseY)) {
    callback.onContinueGame();  // ← Calls BoneChildGame
}

// In BoneChildGame:
@Override
public void onContinueGame() {
    Gdx.app.log("BoneChild", "Continue game selected");
    startGameWithSave(true);  // ← Load save = TRUE
}
```

**NEW GAME Button Clicked:**
```java
// In MenuScreen:
if (startButton.contains(mouseX, mouseY)) {
    callback.onNewGame();  // ← Calls BoneChildGame
}

// In BoneChildGame:
@Override
public void onNewGame() {
    Gdx.app.log("BoneChild", "New game selected");
    startGameWithSave(false);  // ← Load save = FALSE
}
```

#### Step 4: Loading the Save
```java
// In BoneChildGame.startGameWithSave():
if (loadSave && worldManager.hasSaveFile()) {
    SaveState saveState = worldManager.loadGame();
    
    // Restore player stats
    player.setLevel(saveState.level);
    player.setExperience(saveState.experience);
    player.setGold(saveState.gold);
    player.setCurrentHealth(saveState.currentHealth);
    player.setMaxHealth(saveState.maxHealth);
    
    // Restore power-ups (loops through each level)
    for (int i = 0; i < saveState.speedLevel; i++) {
        player.applyPowerUp("SPEED");
    }
    // ... more power-ups
    
    // Skip to saved wave
    worldManager.skipToWave(saveState.currentWave);
}
```

## How to Test

### Test 1: With Save File (Continue)
```bash
# Ensure save exists at wave 4
cat ~/Library/Application\ Support/BoneChild/bonechild_save.json

# Run game
./run.sh

# You should see:
# - "CONTINUE" button (highlighted)
# - "NEW GAME" button below it
# 
# Click CONTINUE or press ENTER
# Game loads at Wave 4 with your saved character!
```

**Console Output:**
```
[MenuScreen] Save file exists: true
[BoneChild] Continue game selected
[BoneChild] 📁 Loading saved game...
[BoneChild] ⏩ Skipping to wave 4
[WorldManager] Skipped to wave 4
[BoneChild] ✅ Save loaded! Level 5, Wave 4
```

### Test 2: With Save File (New Game)
```bash
# Run game
./run.sh

# Click "NEW GAME" button
# Game starts fresh at Wave 1 (save is ignored)
```

**Console Output:**
```
[MenuScreen] Save file exists: true
[BoneChild] New game selected
[BoneChild] Starting game...
[BoneChild] No save file found - starting new game
```

### Test 3: Without Save File
```bash
# Delete save
rm ~/Library/Application\ Support/BoneChild/bonechild_save.json

# Run game
./run.sh

# You should see:
# - "START GAME" button (no Continue option)
#
# Click START GAME or press ENTER
# Game starts at Wave 1
```

## UI Flow Diagram

```
┌─────────────────────────────────────────────┐
│           Game Launches                     │
│           MenuScreen.create()               │
└────────────────┬────────────────────────────┘
                 │
                 ▼
        ┌────────────────────┐
        │ Check Save File?   │
        │ callback.hasSave() │
        └────────┬───────────┘
                 │
        ┌────────┴────────┐
        │                 │
   ✅ YES            ❌ NO
        │                 │
        ▼                 ▼
┌───────────────┐  ┌──────────────┐
│  Show:        │  │  Show:       │
│  - CONTINUE   │  │  - START     │
│  - NEW GAME   │  │  - SETTINGS  │
│  - SETTINGS   │  │  - EXIT      │
│  - EXIT       │  │              │
└───────┬───────┘  └──────┬───────┘
        │                 │
        │                 │
    USER CLICKS       USER CLICKS
        │                 │
   ┌────┴─────┐           │
   │          │           │
CONTINUE   NEW GAME    START GAME
   │          │           │
   ▼          ▼           ▼
┌──────┐  ┌──────┐  ┌──────────┐
│ Load │  │ Fresh│  │  Fresh   │
│ Save │  │ Wave1│  │  Wave1   │
└──────┘  └──────┘  └──────────┘
   │          │           │
   └──────────┴───────────┘
              │
              ▼
      Game Starts Playing
```

## Current Save File Location

**Path:** `~/Library/Application Support/BoneChild/bonechild_save.json`

**Current Contents (Wave 4):**
```json
{
  "level": 5,
  "experience": 0,
  "experienceToNextLevel": 500,
  "gold": 500,
  "currentHealth": 150,
  "maxHealth": 150,
  "speedLevel": 2,
  "strengthLevel": 2,
  "grabLevel": 1,
  "attackSpeedLevel": 2,
  "maxHpLevel": 1,
  "xpBoostLevel": 1,
  "explosionChanceLevel": 1,
  "chainLightningLevel": 0,
  "lifestealLevel": 0,
  "currentStageId": "stage_1",
  "currentWave": 4,
  "saveTime": 1703274895432
}
```

## Keyboard Shortcuts

- **ENTER** - Start/Continue game (Continue if save exists)
- **ESC** - Exit game
- **Click buttons** - Mouse interaction

## Summary

**The save loading happens in the UI through:**

1. ✅ **MenuScreen** - Detects save file and shows CONTINUE button
2. ✅ **CONTINUE button** - Calls `onContinueGame()` → loads save
3. ✅ **NEW GAME button** - Calls `onNewGame()` → starts fresh
4. ✅ **Automatic** - No manual save loading needed, it's all UI-driven!

**You just need to click CONTINUE when you see it!** 🎮💾✨

