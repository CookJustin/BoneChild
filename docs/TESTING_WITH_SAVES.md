# Quick Testing Guide - Using Save Files

## Test Any Wave Instantly

Instead of playing through waves or using debug constants, just edit the save file!

### Step 1: Create Save File

**macOS:**
```bash
mkdir -p ~/Library/Application\ Support/BoneChild/
cat > ~/Library/Application\ Support/BoneChild/bonechild_save.json << 'EOF'
{
  "level": 10,
  "experience": 0,
  "experienceToNextLevel": 500,
  "gold": 999,
  "currentHealth": 200,
  "maxHealth": 200,
  "speedLevel": 3,
  "strengthLevel": 3,
  "grabLevel": 2,
  "attackSpeedLevel": 2,
  "maxHpLevel": 2,
  "xpBoostLevel": 1,
  "explosionChanceLevel": 1,
  "chainLightningLevel": 1,
  "lifestealLevel": 1,
  "currentStageId": "stage_1",
  "currentWave": 5,
  "saveTime": 1703274895432
}
EOF
```

**Windows:**
```cmd
mkdir %APPDATA%\BoneChild
notepad %APPDATA%\BoneChild\bonechild_save.json
REM Paste the JSON from above
```

**Linux:**
```bash
mkdir -p ~/.local/share/BoneChild/
cat > ~/.local/share/BoneChild/bonechild_save.json << 'EOF'
{JSON from above}
EOF
```

### Step 2: Edit Wave Number

Just change `"currentWave"` to test different waves:

- `"currentWave": 1` - Normal start
- `"currentWave": 3` - Mid-game
- `"currentWave": 5` - Boss wave 🚨

### Step 3: Start Game

```bash
./run.sh
# Click "Continue" (when implemented)
# Or load save programmatically
```

## Quick Wave Reference

From `stages/stage-1.json`:

- **Wave 1** - 5 goblins
- **Wave 2** - 8 goblins
- **Wave 3** - 10 goblins
- **Wave 4** - 6 goblins + 3 mobs
- **Wave 5** - 1 boss08b (BOSS) 🚨

## Power-Up Levels

Want to test with different builds? Edit these:

```json
{
  "speedLevel": 5,        // Max speed
  "strengthLevel": 5,     // Max damage
  "attackSpeedLevel": 5,  // Super fast attacks
  "grabLevel": 10,        // Vacuum loot
  "lifestealLevel": 3,    // 45% lifesteal
  ...
}
```

## Common Test Scenarios

### Test Boss Wave
```json
{
  "level": 10,
  "currentWave": 5,
  "speedLevel": 3,
  "strengthLevel": 5,
  "currentHealth": 200,
  "maxHealth": 200
}
```

### Test Weak Character
```json
{
  "level": 1,
  "currentWave": 5,
  "speedLevel": 0,
  "strengthLevel": 0,
  "currentHealth": 100,
  "maxHealth": 100
}
```

### Test Overpowered Build
```json
{
  "level": 50,
  "currentWave": 1,
  "speedLevel": 10,
  "strengthLevel": 10,
  "attackSpeedLevel": 10,
  "lifestealLevel": 5,
  "currentHealth": 500,
  "maxHealth": 500
}
```

**Happy testing!** 🎮✨

