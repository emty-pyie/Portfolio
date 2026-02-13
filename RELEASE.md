# Release Guide (Axe EX)

## 1) Build the mod jar locally
Run from project root:

```bash
# Linux/macOS
JAVA_HOME=/path/to/java-21 PATH=$JAVA_HOME/bin:$PATH gradle build
```

After a successful build, your mod jar is in:

- `build/libs/axe-<version>.jar`

## 2) Create a GitHub release automatically (recommended)
This repository includes `.github/workflows/release.yml`.

### Option A: Tag-based release
1. Commit and push your changes.
2. Create and push a version tag:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
3. GitHub Actions will build the mod and attach the jar to a GitHub Release.

### Option B: Manual release run
1. Open **GitHub → Actions → Build and Release Mod JAR**.
2. Click **Run workflow**.
3. After completion, open **Releases** to download the jar artifact.

## 3) Manual release (without Actions)
If you already have a built jar, you can upload it directly:

1. Go to **GitHub → Releases → Draft a new release**.
2. Choose tag (e.g. `v1.0.0`) and title (e.g. `Axe EX v1.0.0`).
3. Upload `build/libs/axe-<version>.jar` as a binary asset.
4. Publish release.
