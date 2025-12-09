# Audio System Implementation

## Overview
BoneChild now includes full audio support with background music and sound effects.

## Audio Files Setup

### Required Location
Place your audio files in: `src/main/resources/assets/audio/`

### Background Music
The game is configured to use **"7th realm.mp3"** as the background music.

**File:** `7th realm.mp3`
- **Location:** `src/main/resources/assets/audio/7th realm.mp3`
- **Format:** MP3 (also supports OGG)
- **Behavior:** Loops automatically
- **Volume:** 50% by default

### Optional Sound Effects
The following sound effects can be added for enhanced gameplay:

1. **attack.wav** or **attack.ogg** - Player attack sound
2. **hit.wav** or **hit.ogg** - Enemy hit/damage sound  
3. **levelup.wav** or **levelup.ogg** - Level up sound effect
4. **death.wav** or **death.ogg** - Enemy death sound

**Format Support:** WAV or OGG
**Volume:** 60% by default

## Implementation Details

### Assets.java
- Added audio loading in `loadAudio()` method
- Primary check for "7th realm.mp3"
- Fallback to "background.ogg" or "background.mp3"
- Safe loading - game continues even if audio files are missing
- Helper methods: `playSound(Sound)` and `playSound(Sound, volume)`

### BoneChildGame.java
- Background music starts automatically when game initializes
- Music disposes properly when game closes

### Future Integration Points
Sound effects can be integrated by:
- Calling `assets.playSound(assets.getAttackSound())` when player attacks
- Calling `assets.playSound(assets.getHitSound())` when enemy is hit
- Calling `assets.playSound(assets.getLevelUpSound())` when player levels up
- Calling `assets.playSound(assets.getDeathSound())` when enemy dies

## How to Add Your Music

1. Place your "7th realm.mp3" file in `src/main/resources/assets/audio/`
2. Rebuild the project: `mvn clean package -DskipTests`
3. Run the game: `./run.sh`
4. Music will start automatically!

## Volume Control
Current volumes can be adjusted in `Assets.java`:
- Background music: `backgroundMusic.setVolume(0.5f)` (50%)
- Sound effects: `playSound(sound, 0.6f)` (60%)

## Supported Formats
- **Music (streaming):** MP3, OGG
- **Sound Effects (in-memory):** WAV, OGG

## Notes
- Audio is fully optional - the game runs fine without audio files
- MP3 support is provided by LibGDX's built-in jlayer library
- All audio is automatically disposed when the game closes
