# MinecraftAndroidServer

Minecraft Bedrock Dedicated Server on stock Android. No Termux, no root, no PRoot, no Linux rootfs. Target device: Poco X4 GT, Android 14.

## Current architecture

Box64/runtime is unchanged: the verified ARM64 Box64 executable is downloaded by Gradle from the `runtime-v1` Release and packaged into `nativeLibraryDir`; guest libraries and Playit are downloaded on first launch into app data. BDS is loaded directly by Box64.

## BDS installation

The app requests the public `EndstoneMC/bedrock-server-data` version registry, fills the dropdown with available versions, and uses the selected metadata URL and SHA-256. The ZIP is downloaded, verified, extracted, and given a basic `server.properties` automatically. The advanced section still accepts a manual version, HTTPS URL, SHA-256, or a local ZIP. Local ZIPs are still checked against the entered SHA-256, so there is no silent bypass.

If the metadata registry is unavailable, use the advanced mode. The Mojang BDS download page remains the authoritative source for terms and downloads.

## Playit.gg

The Network section can start and stop the pinned x86_64 Playit CLI through Box64, save a secret key for the current session, auto-start it with BDS, show a detected `*.gl.at.ply.gg:port` address, and copy that address. The first claim/login flow may still require Playit interaction in the log and stdin command field.

## ACS Lite build

Use Java 17, AGP 8.2.2, and Gradle 8.2. Open the folder containing `settings.gradle`, then run `gradlew assembleDebug` or ACS Lite's `app:assembleDebug`. The build still downloads and verifies `libbox64.so` automatically. No binary copying is required.

## What to test on Poco X4 GT

Install the APK, wait for runtime files, refresh the BDS list, select a version, confirm URL and SHA-256 populate, install it, then start BDS. Confirm Box64 logs, BDS `Server started`, LAN UDP 19132, console `list` and `stop`. For Playit, enter the secret only if your account requires it, start the tunnel, wait for the public address, copy it, and connect externally. If a feature fails, save the first 30 log lines and the runtime status JSON.
