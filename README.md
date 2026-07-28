# MinecraftAndroidServer

Minecraft Bedrock Dedicated Server on stock Android. No Termux, no root, no
PRoot, no Linux rootfs. Target device: Poco X4 GT, Android 14.

## One-click project flow

This repository is designed so the user does **not** copy binaries manually:

1. Download the repository ZIP.
2. Open the project root in ACS Lite.
3. Build `app:assembleDebug`.
4. Gradle automatically downloads and SHA-256 verifies
   `libbox64.so` from the project's `runtime-v1` GitHub Release and packages
   it into the APK's `jniLibs/arm64-v8a`.
5. Install the APK. On first launch, the app downloads only the pinned
   x86_64 guest-library bundle and x86_64 Playit CLI into private app data.

No `libbox64.so`, runtime archive, or other binary is copied into the source
tree.

## Important first-build requirement

The repository must have a published runtime Release named `runtime-v1` with
these assets:

- `libbox64.so`
- `SHA256SUMS`, containing a line for `libbox64.so`

If the Release is missing, ACS Lite stops with a clear error instead of building
an APK with an unverified or nonfunctional Box64. This is intentional.

## ACS Lite build

Use Java 17, AGP 8.2.2, and Gradle 8.2. Open the folder containing
`settings.gradle`, then run **Assemble Debug APK**. The output is
`app/build/outputs/apk/debug/app-debug.apk`. The build needs internet access for
the Gradle distribution, Maven dependencies, and the runtime Release.

## First launch

The app verifies and downloads exactly two runtime artifacts from
`runtime-manifest.json`: the official Box64 x86_64 guest-library bundle and the
Playit x86_64 CLI. They are stored in private app data. BDS is downloaded later
after the user enters its version and trusted SHA-256.

If a first-run download fails, the log shows the artifact name and URL. Press
Runtime install again after restoring internet or storage. No manual file copy is
needed.

## Device test

1. Install the APK on Poco X4 GT.
2. Wait for `Runtime files ready` and verify `box64Present: true`.
3. Enter the BDS version and trusted SHA-256, then install.
4. Press Start. The first success marker is `[BOX64] Box64 v0.4.2`.
5. Continue until BDS says `Server started`, then connect over LAN to UDP
   port 19132.
6. Test `list`, `stop`, and restart. Keep the phone charging and disable
   battery optimization for the app.

If the build fails with “runtime-v1 is unavailable”, the Release has not been
published yet. That is a repository release problem, not an ACS Lite problem.
