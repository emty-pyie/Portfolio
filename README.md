# Axe EX (v1)

Minecraft Forge mod for **Minecraft Java 1.21.1** that adds a charged **Throwable Axe** mechanic.

## Features
- Hold the **R** key to charge a throw and release to launch your held axe.
- Flight feel is trident-like (`shootFromRotation`) with charge-based velocity.
- Heavy damage scales with charge and **Sharpness** enchantment.
- Axe can stick into wood-related blocks and be recovered by right-clicking with an empty main hand.
- Server-side handling and packet validation for multiplayer/mod compatibility.

## Controls
- `R` = Throw Axe (hold to charge)

## Project structure
- `src/main/java/com/axex/axe` contains the mod code.
- `src/main/resources` contains Forge metadata, language, models, and tags.

## Build (when Forge repositories are reachable)
```bash
./gradlew build
```

Built JAR should appear under `build/libs`.



## Release
- See `RELEASE.md` for how to build the `.jar` and publish GitHub releases (automatic and manual methods).
