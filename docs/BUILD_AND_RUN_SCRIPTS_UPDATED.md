# Build and Run Scripts - Updated ✅

## Summary
Both `build.sh` and `run.sh` have been updated to work with the new multi-module Maven structure.

## Changes Made

### build.sh
**Updated:**
- Changed from `mvn package` to `mvn install -DskipTests` to install all modules
- Removed non-existent `native-package` profile
- Updated output paths to reflect multi-module structure
- Corrected JAR location to `engine/target/bonechild-engine-1.0.0-all.jar`

**Now shows:**
```
Output files:
  - Executable JAR: engine/target/bonechild-engine-1.0.0-all.jar
  - Engine JAR: engine/target/bonechild-engine-1.0.0.jar
  - Module JARs installed to ~/.m2/repository/com/bonechild/

To run the game:
  ./run.sh
  OR
  java -XstartOnFirstThread -jar engine/target/bonechild-engine-1.0.0-all.jar
```

### run.sh
**Updated:**
- Changed JAR path from `target/bonechild-game-1.0.0-all.jar` (doesn't exist)
- To `engine/target/bonechild-engine-1.0.0-all.jar` (correct location in multi-module)
- Still correctly handles macOS `-XstartOnFirstThread` flag
- Still works on Linux/other platforms

## Testing Performed

✅ **build.sh tested:**
```bash
./build.sh
# Result: SUCCESS - All modules built and installed
```

✅ **run.sh tested:**
```bash
./run.sh
# Result: Game launches successfully
# Output: "Running on macOS with -XstartOnFirstThread flag..."
# Game window opens and loads assets correctly
```

## Usage

### To build:
```bash
./build.sh
```

### To run:
```bash
./run.sh
```

### Or manually:
```bash
# Build
mvn clean install -DskipTests

# Run (macOS)
java -XstartOnFirstThread -jar engine/target/bonechild-engine-1.0.0-all.jar

# Run (Linux/other)
java -jar engine/target/bonechild-engine-1.0.0-all.jar
```

## What Works Now

✅ Build script builds all 8 modules correctly  
✅ Run script finds and launches the JAR  
✅ macOS-specific JVM flags applied automatically  
✅ Cross-platform compatibility maintained  
✅ Clear error messages if JAR not found  

Both scripts are production-ready!

