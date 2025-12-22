# UI Module

## Purpose
User interface screens, HUD, and menus for BoneChild. Handles all presentation logic for displaying game state to the player.

## What's in this module

### Screens (`com.bonechild.ui`)
- **GameUI** - In-game HUD (health bar, XP bar, wave counter)
- **PauseMenu** - Pause screen with resume/settings/exit
- **PowerUpScreen** - Level-up power selection screen
- **GameOverScreen** - Death/victory screen
- **BossWarningScreen** - Boss wave warning
- **CharacterStatsScreen** - Player stats display
- **InventoryUI** - Inventory management (future)
- **MenuScreen** - Main menu
- **SettingsScreen** - Game settings
- **UIEffectsManager** - UI visual effects

## Dependencies
- `game-core` - For accessing Player, WorldManager game state
- `assets` - For fonts and UI textures
- LibGDX Core - For rendering and input

## Used By
- `engine` - Main game creates and switches between UI screens

## Key Responsibilities

### HUD Display
```java
GameUI gameUI = new GameUI(assets, player, worldManager);
gameUI.render();
// Shows: health bar, XP bar, wave counter, gold, level
```

### Menu Screens
```java
PauseMenu pauseMenu = new PauseMenu(assets, callback);
pauseMenu.render();
pauseMenu.handleInput();
```

### Level-Up Flow
```java
PowerUpScreen powerUpScreen = new PowerUpScreen(assets, player, callback);
// Displays 3 random power-ups for player to choose
```

## Architecture

```
┌──────────────┐
│  game-core   │  ← Game state (Player, WorldManager)
└──────┬───────┘
       │
       │ reads state
       │
┌──────▼───────┐
│      ui      │  ← Presentation layer
└──────────────┘
       ▲
       │ uses
       │
┌──────┴───────┐
│   engine     │  ← Orchestration
└──────────────┘
```

UI reads game state from game-core but doesn't modify it directly (except through callbacks).

## Screen Types

### Overlay Screens
- **GameUI** - Always visible during gameplay
- Renders on top of game world

### Modal Screens
- **PauseMenu** - Pauses game, blocks input to game
- **PowerUpScreen** - Blocks game until power-up selected
- **BossWarningScreen** - Shows warning before spawning boss

### Full Screens
- **MenuScreen** - Main menu (no game world)
- **GameOverScreen** - End game screen
- **SettingsScreen** - Settings menu

## Design Principles

✅ **Separation of concerns** - UI only handles presentation  
✅ **Read-only game state** - UI observes, doesn't mutate  
✅ **Callbacks for actions** - UI notifies engine through callbacks  
✅ **Stateless when possible** - Game state lives in game-core  

## Callback Pattern

UI screens use callbacks to communicate actions back to engine:

```java
interface PowerUpCallback {
    void onPowerUpSelected(int powerUpIndex);
}

PowerUpScreen screen = new PowerUpScreen(assets, player, (index) -> {
    // Engine handles the selection
    applyPowerUp(index);
    resumeGame();
});
```

## Future Enhancements
- [ ] Extract UI data models (separate from rendering)
- [ ] Theme system for customizable UI
- [ ] Animation system for UI transitions
- [ ] Localization support
- [ ] Accessibility features (screen reader, high contrast)

