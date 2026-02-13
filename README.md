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

## Save/upload this project to GitHub
1. Create a new empty repository on GitHub (no README/license/gitignore).
2. In this project folder, set your remote URL:
   ```bash
   git remote add origin https://github.com/<your-username>/AxeEX-v1.git
   ```
   If `origin` already exists:
   ```bash
   git remote set-url origin https://github.com/<your-username>/AxeEX-v1.git
   ```
3. Push to GitHub:
   ```bash
   git branch -M main
   git push -u origin main
   ```
4. Refresh your GitHub repo page and confirm all files are uploaded.
