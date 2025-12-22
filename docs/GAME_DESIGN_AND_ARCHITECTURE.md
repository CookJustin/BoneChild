# BoneChild – Game Design & Architecture Plan (Draft)

> Goal: Evolve BoneChild from the current monolithic structure into a modular architecture where stages, waves, monsters, bosses, items, and UI are cleanly separated and easy to extend.

---

## 1. High-Level Game Concept

### 1.1 Core Loop

- Real-time survival / action combat.
- Player becomes more powerful over time via:
  - **Stat level-ups** (XP → level → health, damage, speed, etc.).
  - **Powers** (discrete upgrades like explosion chance, chain lightning, lifesteal).
  - **Items & equipment** (armor, weapons, trinkets from the item system).
  - **Skill tree** (long-term or run-based unlocks that shape builds).

- The run is structured into **Stages**, each composed of **Waves** of enemies.
- At the end of each Stage, the player gets a **Rest Phase** where they can:
  - Regroup: heal, shop, upgrade, manage items/skills.
  - Enter a **Portal** to the next Stage.

### 1.2 Design Goals

- Easily add new **monsters** and **bosses** without touching core game loop or renderer internals.
- Easily configure new **Stages** and **Waves** using data (IDs), reusing monsters from a centralized monster module.
- Clean separation between:
  - **Domain logic** (world, player, mobs, stages).
  - **Rendering** (LibGDX-specific drawing, particles, animations).
  - **UI** (menu, HUD, overlays, rest hub, etc.).
  - **Input** (mapping keys/gamepad to game commands).
- Support an effectively **unbounded number of stages**, limited only by available content.

---

## 2. Target Module / Package Structure

The goal is to gradually transition from the current structure into a more modular one. This is a conceptual module layout; actual Java packages will mirror it.

### 2.1 Core Modules

1. **core-domain** (current: `com.bonechild.world`, parts of `com.bonechild.util`)
   - Pure game logic & state, no LibGDX UI dependencies.
   - Contains:
     - `Player`, `Mob` abstractions, bosses, projectiles, pickups, explosions.
     - `WorldState` / `WorldManager` (simulation orchestrator).
     - Combat rules, XP, level-ups, loot and pickups.

2. **stage-system** (new: `com.bonechild.stage`)
   - Stage / wave / spawn data and progression logic.
   - Contains:
     - `StageDefinition`, `WaveDefinition`, `SpawnPattern`.
     - `StageRepository` (loads and stores stages).
     - `StageProgression` (runtime state machine for current stage & wave).

3. **monster-module** (new: `com.bonechild.monsters`)
   - Central registry and factory for all monsters/bosses.
   - Contains:
     - Monster interfaces (`MobEntity`, `BossEntity`, etc.).
     - Concrete monster classes (e.g. `Glob`, `Enemy17B`, `Orc`, `Boss08B`, `Vampire`).
     - `MobFactory` & `DefaultMobFactory` (typeId → constructor mapping).

4. **item-system** (new: `com.bonechild.items`)
   - Items, equipment, consumables, and inventory logic.
   - Contains:
     - `ItemDefinition`, `ItemRegistry`.
     - `Inventory` (attached to `Player`).
     - Item effects (stat modifiers, on-hit/on-kill hooks).

5. **powers-and-skilltree** (new: `com.bonechild.powers`)
   - Powers (speed, strength, explosion chance, etc.) and skill tree.
   - Contains:
     - `PowerDefinition`, `PowerRegistry`.
     - `SkillNode`, `SkillTree`.

6. **rendering** (current: `com.bonechild.rendering`)
   - LibGDX-specific drawing logic.
   - Contains:
     - `Renderer`, `Assets`, `Animation`, `ParticleSystem`, `ScreenEffects`.
     - Rendering strategies per entity type (see MobRenderer below).

7. **ui** (current: `com.bonechild.ui`)
   - All in-game and out-of-game screens & overlays.
   - Contains:
     - `MenuScreen`, `SettingsScreen`, `PauseMenu`, `GameUI`.
     - `PowerUpScreen`, `CharacterStatsScreen`, `InventoryUI`.
     - `GameOverScreen`, `BossWarningScreen`.
     - Future: `RestHubScreen`, `StageSelectScreen`.

8. **input** (current: `com.bonechild.input`)
   - Maps device-specific input to game commands.
   - Contains:
     - `PlayerInput`, keybind configs.

9. **app-shell** (current: `com.bonechild` root)
   - LibGDX bootstrap and high-level wiring.
   - Contains:
     - `BoneChildGame`, desktop launcher (`Main`).

Over time, we “end-of-life” the old ad-hoc glue in `BoneChildGame`/`WorldManager` and move toward these modules, while keeping the game runnable at each step.

---

## 3. Stage & Wave System Design

### 3.1 Core Concepts

- **Stage** – an environment/theme with:
  - A list of `WaveDefinition`s.
  - Biome/tileset/music IDs.
  - Stage-level modifiers (e.g., enemies faster, lower healing).

- **Wave** – a self-contained encounter within a Stage:
  - A list of `SpawnPattern`s.
  - Optional boss spawn.
  - Completion condition (time-based, kill-based, or boss-defeat).

- **Rest Phase** – a safe hub at the end of a Stage:
  - No enemy spawns.
  - Player can heal, shop, upgrade, manage items and skills.
  - Player uses a portal to proceed to the next Stage.

### 3.2 Data Model (Conceptual Java Types)

```java
public final class SpawnPattern {
    public String mobTypeId;        // e.g. "glob", "orc", "boss08b"
    public int count;
    public SpawnShape shape;        // CIRCLE_AROUND_PLAYER, FROM_EDGES, RANDOM, etc.
    public float startTime;         // seconds from wave start
    public float spreadTime;        // seconds over which to spawn this group
    // runtime-only field: spawnedSoFar
}

public enum SpawnShape {
    RANDOM_AROUND_PLAYER,
    FROM_EDGES,
    CIRCLE_AROUND_PLAYER,
    FIXED_POINTS
}

public final class WaveDefinition {
    public String id;               // "stage1_wave1"
    public float durationSeconds;   // 0 for purely kill-based
    public boolean killAllToComplete;

    public List<SpawnPattern> spawns = new ArrayList<>();
    public String bossMobTypeId;    // optional; e.g. "boss08b"
}

public final class StageDefinition {
    public String id;               // "stage1_forest"
    public String displayName;      // "Whispering Forest"
    public String biomeId;          // tileset/music ID

    public List<WaveDefinition> waves = new ArrayList<>();

    public List<String> nextStageIds; // for linear/branching stage progression
    // optional: StageModifierSet modifiers;
}
```

These can initially be constructed in code, then moved to JSON/YAML when we want fully externalized configuration.

### 3.3 StageProgression Controller

**Responsibilities:**

- Keep track of:
  - Current Stage & Wave.
  - Per-wave timers and spawn progress.
  - Whether we are in REST or RUNNING state.
- Use `SpawnPattern`s to call into `MobFactory` at the right times.
- Determine when a wave or stage is complete, and signal to the app shell/UI.

**Interface (conceptual):**

```java
public interface StageProgression {
    StageDefinition getCurrentStage();
    WaveDefinition getCurrentWave();
    boolean isInRestPhase();

    void startRun(String startingStageId);
    void update(float delta); // called every frame
}
```

**Runtime behavior:**

- Each frame:
  - Advance `waveTime` for the current wave.
  - For each `SpawnPattern` in the wave:
    - Compute how many mobs should have spawned given `startTime`, `spreadTime`, and `waveTime`.
    - If `spawnedSoFar < expectedCount`, call `MobFactory.create(pattern.mobTypeId, ctx)` and `worldManager.addMob()`.
  - Check completion:
    - If `killAllToComplete` → wait until `worldManager.getMobs()` is empty and all spawns are done.
    - Else if `durationSeconds > 0` → complete when `waveTime >= durationSeconds` and all spawns are done.
  - On wave completion:
    - If not last wave → advance to next wave.
    - If last wave → enter REST phase.

### 3.4 Rest Phase & Portal

- When `StageProgression` enters REST:
  - `isInRestPhase()` returns true.
  - No new spawns are triggered.
  - `BoneChildGame` / UI transitions to a "Rest Hub" state:
    - Show "Stage Complete" banner.
    - Enable shop, inventory, skill tree, stats review.
  - A portal UI element (or in-world object) calls:

    ```java
    stageProgression.startRun(nextStageId);
    worldManager.resetForNewStage();
    ```

- Stage selection logic can be linear (Stage 1 → 2 → 3) or branching, controlled by `nextStageIds` in `StageDefinition`.

---

## 4. Monster Module & Registry

### 4.1 Monster Interfaces

We decouple the rest of the game from concrete monster classes using minimal interfaces:

```java
public interface Entity {
    float getX();
    float getY();
    float getWidth();
    float getHeight();
    boolean isActive();
    boolean isDead();
}

public interface MobEntity extends Entity {
    String getTypeId();           // e.g. "glob", "orc", "boss08b"
    float getHealthPercentage();
    boolean isBoss();             // optional convenience
}

public interface BossEntity extends MobEntity {
    // Additional boss queries if needed
}
```

Concrete monsters (classes in `com.bonechild.world` or moved into `com.bonechild.monsters`) implement these interfaces.


### 4.2 MobFactory & Registry

A central registry to map `mobTypeId` → creation function:

```java
public interface MobFactory {
    MobEntity create(String mobTypeId, SpawnContext ctx);
}

public final class SpawnContext {
    public final float x;
    public final float y;
    public final Player player;      // target for AI
    public final Assets assets;      // for sprite/animation lookup
    public final WorldManager world; // optional

    // constructor, getters
}

public class DefaultMobFactory implements MobFactory {
    private final Map<String, Function<SpawnContext, MobEntity>> registry = new HashMap<>();

    public DefaultMobFactory register(String typeId, Function<SpawnContext, MobEntity> creator) {
        registry.put(typeId, creator);
        return this;
    }

    @Override
    public MobEntity create(String mobTypeId, SpawnContext ctx) {
        var creator = registry.get(mobTypeId);
        if (creator == null) {
            throw new IllegalArgumentException("Unknown mob typeId: " + mobTypeId);
        }
        return creator.apply(ctx);
    }
}
```

At startup, we register monster types once:

```java
MobFactory mobFactory = new DefaultMobFactory()
    .register("glob",     ctx -> new Glob(ctx.x, ctx.y, ctx.player, ctx.assets))
    .register("enemy17b", ctx -> new Enemy17B(ctx.x, ctx.y, ctx.player, ctx.assets))
    .register("orc",      ctx -> new Orc(ctx.x, ctx.y, ctx.player, ctx.assets))
    .register("boss08b",  ctx -> new Boss08B(ctx.x, ctx.y, ctx.player, ctx.assets));
```

> **Adding a new monster** is then:
> - Implement its class (`Vampire`),
> - Register it (`register("vampire", ...)`),
> - Reference `"vampire"` in any `SpawnPattern`.

The Stage/Wave system and `WorldManager` don’t know or care about the concrete classes, only `mobTypeId` and `MobEntity` interface.

---

## 5. Rendering Monsters by Type (Decorator Pattern)

To avoid `instanceof` chains in `Renderer`, we use type-based renderers.

### 5.1 MobRenderer Registry

```java
public interface MobRenderer {
    boolean supports(String mobTypeId);
    void render(MobEntity mob, SpriteBatch batch, float deltaTime);
}

public class MobRenderRegistry {
    private final List<MobRenderer> renderers = new ArrayList<>();

    public void register(MobRenderer renderer) {
        renderers.add(renderer);
    }

    public MobRenderer getRendererFor(String typeId) {
        for (MobRenderer r : renderers) {
            if (r.supports(typeId)) return r;
        }
        throw new IllegalArgumentException("No renderer for mob type: " + typeId);
    }
}
```

`Renderer.renderMobs(...)` becomes:

```java
public void renderMobs(List<MobEntity> mobs) {
    if (mobs == null || mobs.isEmpty()) return;

    batch.begin();
    for (MobEntity mob : mobs) {
        if (!mob.isActive()) continue;
        MobRenderer r = mobRenderRegistry.getRendererFor(mob.getTypeId());
        r.render(mob, batch, deltaTime);
    }
    batch.end();
}
```

Each monster type defines its own rendering logic:

```java
public class OrcRenderer implements MobRenderer {
    @Override
    public boolean supports(String mobTypeId) { return "orc".equals(mobTypeId); }

    @Override
    public void render(MobEntity mob, SpriteBatch batch, float dt) {
        // use Assets and mob position/size to draw
    }
}
```

> This makes monsters **pluggable** on the rendering side as well.

---

## 6. Player Progression: Stats, Powers, Items, Skill Tree

### 6.1 Stats & Level-Ups

The Player already tracks level/XP and sets `leveledUpThisFrame`. We will formalize it:

- Core stats:
  - Health, armor, regen.
  - Damage, crit chance/multiplier.
  - Attack speed / cooldown.
  - Move speed, pickup radius, dodge charges.

- Level up flow:
  - XP threshold from `experienceToNextLevel`.
  - On level-up:
    - Increment level.
    - Recalculate `experienceToNextLevel`.
    - Heal some health.
    - Set `leveledUpThisFrame = true` → triggers `PowerUpScreen`.

### 6.2 Powers

Existing powers (SPEED, STRENGTH, GRAB, ATTACK_SPEED, MAX_HP, XP_BOOST, EXPLOSION_CHANCE, CHAIN_LIGHTNING, LIFESTEAL) will be described as data:

```java
public final class PowerDefinition {
    public String id;          // "speed", "explosion_chance", etc.
    public String name;
    public String description;
    public int maxLevel;
    public Map<String, Float> perLevelModifiers;
}
```

- `Player.applyPowerUp(String powerUpType)` looks up a `PowerDefinition` and applies its modifiers, instead of hard-coding logic in a `switch`.
- `PowerUpScreen` gets a list of available `PowerDefinition`s to offer.

### 6.3 Items & Inventory

We add an explicit item model:

```java
public final class ItemDefinition {
    public String id;           // "adamantine_helmet_1"
    public String displayName;
    public String slot;         // "helmet", "chest", "boots", "weapon", etc.
    public Map<String, Float> statModifiers;
    public String spriteId;     // points to Assets
}

public final class ItemInstance {
    public final ItemDefinition definition;
    // optional: durability, enchantments, etc.
}

public final class Inventory {
    private final List<ItemInstance> items;
    private final Map<String, ItemInstance> equippedBySlot;

    // add/remove/equip/unequip methods
}
```

- `ItemRegistry` holds all `ItemDefinition`s, possibly loaded from external data.
- `Player` holds an `Inventory` reference and **derives effective stats** from base stats + powers + equipped items.

### 6.4 Skill Tree

Skill tree nodes represent build-defining choices:

```java
public final class SkillNode {
    public String id;
    public String name;
    public String description;
    public List<String> prerequisiteIds;
    public PowerDefinition grantedPower;  // or raw stat mods
    public boolean persistent;            // meta progression (across runs) vs run-specific
}
```

- `SkillTree` manages unlocks, prerequisites, and available choices.
- Rest phases or special events can route to a `SkillTreeScreen` where the player spends skill points.

---

## 7. App Shell: BoneChildGame Responsibilities

`BoneChildGame` becomes a relatively thin orchestrator:

- Constructs and wires:
  - `Assets`, `Renderer`.
  - `WorldManager` (core domain state).
  - `MobFactory` (monster module).
  - `StageRepository` + `StageProgression` (stage system).
  - UI screens (`MenuScreen`, `GameUI`, `RestHubScreen`, etc.).
  - `PlayerInput` with references to `PlayerController` / `GameSession`.

- Tracks high-level game state:
  - `MENU`, `RUNNING`, `PAUSED`, `REST`, `GAME_OVER`, `BOSS_WARNING`.

- Per frame:
  - Handles input at the game state level (pause, stats, inventory, confirming stage transitions).
  - Calls `stageProgression.update(delta)` and `worldManager.update(delta)` when in RUNNING state.
  - Delegates rendering to `Renderer` and relevant UI screens depending on state.

Over time, direct references from `BoneChildGame` to low-level details (specific mob classes, deep world internals) will be removed in favor of narrow interfaces (`WorldView`, `GameSession`, etc.).

---

## 8. Migration Plan (High-Level)

1. **Introduce Stage/Wave types and StageProgression**
   - Implement `StageDefinition`, `WaveDefinition`, `SpawnPattern` as simple Java classes.
   - Implement a basic `StageProgressionImpl` that drives spawns via `WorldManager` and `MobFactory`.
   - Hard-code 3 example stages:
     - Stage 1: early waves with `glob`, `enemy17b`.
     - Stage 2: mixes in `orc` and tougher mobs.
     - Stage 3: boss-centric with `boss08b`.

2. **Introduce MobFactory & MobEntity interfaces**
   - Wrap existing monsters to implement `MobEntity`.
   - Create `DefaultMobFactory` and register existing types.
   - Refactor `WorldManager` to spawn mobs only through `MobFactory`.

3. **Refactor Renderer to use MobRenderer registry**
   - Create `MobRenderer` interface and registry.
   - Move per-mob drawing code out of `Renderer` into specific `MobRenderer` implementations.
   - Replace `instanceof` logic where possible.

4. **Add Rest Phase and Portal**
   - Extend `StageProgression` with rest state.
   - Add a basic Rest UI state in `BoneChildGame`.
   - Provide an input/action that triggers `advanceToNextStage`.

5. **Gradually modularize items/powers/skill tree**
   - Introduce `PowerDefinition` and wire existing powers.
   - Define initial `ItemDefinition` and basic `Inventory`.
   - Plan and stub out `SkillTree` for future work.

6. **End-of-life legacy coupling**
   - Replace direct calls in `BoneChildGame` and `WorldManager` with interface-based calls.
   - Remove or quarantine logic that mixes UI, stage progression, and world simulation.

---

## 9. Next Steps

Immediate tasks we can do next:

1. Create a `com.bonechild.stage` package and add:
   - `StageDefinition`, `WaveDefinition`, `SpawnPattern`, `SpawnShape`.
   - A simple `StageRepository` with 3 example stages.
   - `StageProgressionImpl` that drives mob spawns via a stubbed `MobFactory`.

2. Create a `com.bonechild.monsters` (or reuse `com.bonechild.world`) and add:
   - `MobEntity` interface.
   - `MobFactory` + `DefaultMobFactory` and register a subset of existing mobs.

3. Wire `StageProgression` into `BoneChildGame` and `WorldManager` without breaking current gameplay.

Once these pieces are in place, we’ll have a solid foundation to:

- Add new monsters and bosses by adding code + registry entries.
- Add them to any stage/wave via data definitions, not Java glue.
- Scale to many stages while keeping the codebase manageable.

