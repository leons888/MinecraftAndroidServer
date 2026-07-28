# MinecraftAndroidServer handoff

## Goal
Run the official Linux Minecraft Bedrock Dedicated Server on stock Android 14, no Termux, root, PRoot, or Ubuntu rootfs. Target device: Poco X4 GT, arm64-v8a.

## Architecture, do not replace
- `libbox64.so` is ARM64 Android Box64 v0.4.2, downloaded during Gradle build from GitHub Release `runtime-v1`, SHA-256 verified, packaged into `nativeLibraryDir`.
- On first launch, `runtime-manifest.json` downloads only the official Box64 x86 guest library bundle and x86_64 Playit CLI into private app data, with pinned SHA-256.
- BDS is downloaded from Mojang after selecting a version, verified by SHA-256, extracted to app data, then launched as `[libbox64.so, bedrock_server]`.
- Android API 29+ blocks executing app-data binaries. Only Box64 is executed from `nativeLibraryDir`; BDS and Playit are loaded by Box64.

## Current state
- BDS version registry: `ServerManager.listBdsVersions()` reads `EndstoneMC/bedrock-server-data/v2/versions.json`, then each `release/<version>/metadata.json` for Linux URL and SHA-256. UI has a refresh button and status/log tabs.
- Playit is blocked until BDS is actually `running`; public tunnel address is not a secret key.
- `RuntimeManager.env()` currently forces guest glibc with `BOX64_PREFER_EMULATED=1` and `BOX64_EMULATED_LIBS`.
- BDS has no automatic restart loop. If it exits, status becomes error and the detailed log contains the cause.

## Build
- Root project uses AGP 8.2.2, Gradle 8.2, Java 17, compileSdk 35, arm64-v8a only.
- GitHub workflow: `.github/workflows/android.yml`. It installs SDK 35, generates `gradlew`, runs `./gradlew :app:assembleDebug`, uploads `app-debug.apk` and its SHA-256 as an artifact.
- ACS Lite: use Java 17, open the folder containing `settings.gradle`, run `gradle :app:assembleDebug --no-daemon --stacktrace` if the wrapper is absent.

## Latest fix
- Commit `b7478de` introduced a metadata parser typo. The corrected `ServerManager.java` removes the extra `)` from the `new URL(...metadata.json)` expression.

## Known limitations
- A successful Box64 banner does not prove BDS works. `__libc_start_main` and other guest-glibc symbols still require validation on the Poco.
- Playit claim flow may need a URL from the CLI log; the tunnel address only works while the agent is online.

## Test order
1. Build and install APK.
2. Prepare Runtime and wait for `Runtime готов`.
3. Refresh versions, select BDS, verify SHA is populated.
4. Install BDS and wait for `BDS установлен`.
5. Start server and wait for `Minecraft сервер работает`.
6. Only then start Playit and share the displayed public address.
7. If anything fails, copy the detailed log and the exact status message.

## Debugging handoff
Ask for: latest commit, failing workflow step, first error line above the Gradle stack trace, and the first 30 device log lines around the failure. Do not infer the cause from `BUILD FAILED` alone.
