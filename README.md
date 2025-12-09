# BoneChild

A top-down survival action game built with LibGDX and packaged with jpackage.

## 🎮 About

BoneChild is a fast-paced top-down survival game. Fight off waves of enemies, collect experience, level up, and upgrade your abilities to survive as long as possible!

## 🛠️ Technology Stack

- **LibGDX** - Game framework
- **Scene2D UI** - User interface
- **Maven** - Build tool
- **jpackage** - Native packaging (Java 17+)

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- (Optional) For native packaging: Platform-specific tools
  - macOS: Xcode Command Line Tools
  - Windows: WiX Toolset or Inno Setup
  - Linux: rpm-build or fakeroot

## 🚀 Getting Started

### Building the Project

**On macOS/Linux:**
```bash
./build.sh
```

**On Windows:**
```cmd
build.bat
```

**Using Maven directly:**
```bash
# Build JAR with dependencies
mvn clean package

# Build native package
mvn clean package -Pnative-package
```

### Running the Game

**On macOS (requires -XstartOnFirstThread flag):**
```bash
java -XstartOnFirstThread -jar target/bonechild-game-1.0.0-all.jar
```

**On Windows/Linux:**
```bash
java -jar target/bonechild-game-1.0.0-all.jar
```

**Or use the run script:**
```bash
./run.sh     # macOS/Linux
run.bat      # Windows
```

**Run with Maven:**
```bash
mvn exec:java -Dexec.mainClass="com.bonechild.Main"
```

## 📦 Packaging

### Create Native Installers

The project uses jpackage to create native installers for your platform:

```bash
mvn clean package -Pnative-package
```

This will create:
- **macOS**: `.dmg` or `.pkg` installer in `target/dist/`
- **Windows**: `.msi` or `.exe` installer in `target/dist/`
- **Linux**: `.deb` or `.rpm` package in `target/dist/`

### Distribution

The packaged installers include:
- The game JAR and all dependencies
- A custom Java runtime (via jlink)
- Platform-specific launcher
- Application icon (configure in `src/main/resources/icon.png`)

## 🎯 Project Structure

```
BoneChild/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── bonechild/
│       │           ├── Main.java              # Entry point
│       │           ├── BoneChildGame.java     # Main game class
│       │           └── [game classes]         # Game entities, systems, etc.
│       └── resources/
│           ├── icon.png                       # Application icon
│           └── [assets]                       # Sprites, sounds, etc.
├── pom.xml                                    # Maven configuration
├── build.sh                                   # Build script (Unix)
├── build.bat                                  # Build script (Windows)
└── README.md
```

## 🎨 Game Features (To Implement)

- [ ] Player character with movement
- [ ] Auto-attacking weapons
- [ ] Enemy waves with increasing difficulty
- [ ] Experience gems and leveling system
- [ ] Upgrade selection system
- [ ] Multiple weapon types
- [ ] Special abilities
- [ ] Power-ups and collectibles
- [ ] Boss fights
- [ ] Character selection
- [ ] Persistent unlocks

## 🔧 Development

### Adding Assets

Place your assets in `src/main/resources/`:
- Sprites: `textures/`
- Sounds: `sounds/`
- Music: `music/`
- Fonts: `fonts/`

### Key Classes to Implement

1. **Player.java** - Player character with health, movement, and weapons
2. **Enemy.java** - Base enemy class
3. **Weapon.java** - Weapon system with auto-targeting
4. **Projectile.java** - Bullet/projectile management
5. **WaveManager.java** - Enemy wave spawning
6. **UpgradeSystem.java** - Level-up and upgrade selection
7. **GameScreen.java** - Main gameplay screen
8. **MenuScreen.java** - Main menu and UI

## 📝 Build Configuration

The `pom.xml` includes:
- LibGDX core and desktop backend (LWJGL3)
- Scene2D for UI components
- Box2D for physics (optional)
- FreeType for font rendering
- Maven Shade Plugin for fat JAR creation
- jpackage Maven Plugin for native installers
- jlink for custom JRE creation

## 🐛 Troubleshooting

### "Maven not found"
Install Maven: `brew install maven` (macOS) or download from [maven.apache.org](https://maven.apache.org)

### "Java version error"
Ensure Java 17+ is installed and set as JAVA_HOME

### "jpackage fails"
- Ensure you're using JDK 17+ (not JRE)
- Install platform-specific packaging tools (see Prerequisites)

### Game doesn't start
Check the logs in the terminal for LibGDX errors

## 📄 License

MIT License - Feel free to use this template for your own projects!

## 🤝 Contributing

This is a template project. Fork it and make it your own!

---

Built with ❤️ using LibGDX and jpackage
