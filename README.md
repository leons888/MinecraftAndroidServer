# MinecraftAndroidServer

Minecraft Bedrock Dedicated Server on stock Android. No Termux, no root, no
PRoot, no Linux rootfs. The first target device is Poco X4 GT, Android 14.

## ACS Lite / AIDE compatibility

This project intentionally uses a conservative Android Gradle setup:

- Android Gradle Plugin 8.2.2
- Gradle 8.2, pinned in `gradle/wrapper/gradle-wrapper.properties`
- Java 17
- compileSdk 35, minSdk 26, targetSdk 35
- classic `buildscript` Gradle syntax, no version catalogs, no Kotlin DSL, no
  Compose, no Android Studio-only plugins
- only two Java dependencies, Apache Commons Compress and XZ
- only `arm64-v8a`, matching Poco X4 GT

If ACS Lite cannot use the wrapper, select Gradle 8.2 in its project settings.
Do not use Gradle 8.6 or AGP 8.6 for this project.

## Build the first APK in ACS Lite

1. In a browser open https://github.com/leons888/MinecraftAndroidServer.
2. Tap **Code**, then **Download ZIP**.
3. Extract the ZIP into internal storage, for example
   `Download/MinecraftAndroidServer-main`.
4. Before opening it, run the Box64 GitHub Action: open the repository's
   **Actions**, select **Build runtime artifacts**, press **Run workflow**, and
   wait for the `box64-android-arm64` artifact.
5. Download the artifact ZIP and extract `libbox64.so`. Copy it to:

   `app/src/main/jniLibs/arm64-v8a/libbox64.so`

   The folder must contain the real binary, not only the README. Its name must
   stay exactly `libbox64.so`.
6. Open ACS Lite, choose **Open existing project**, and select the extracted
   `MinecraftAndroidServer-main` folder, the folder containing `settings.gradle`.
7. Set the Gradle JDK to Java 17 and Gradle to 8.2 if ACS Lite asks. Allow it
   to download Android platform 35 and the two Maven dependencies.
8. Select the `app` module and run **Assemble Debug APK**. The APK should be at
   `app/build/outputs/apk/debug/app-debug.apk`.
9. Install that APK on the Poco X4 GT.

If ACS Lite reports that `libbox64.so` is not found, check that the file is
inside `app/src/main/jniLibs/arm64-v8a`, not in `assets`, `res/raw`, or the ZIP
root. If the APK builds without the file, it will install but the runtime will
correctly report Box64 as missing.

## How the server runs

`nativeLibraryDir/libbox64.so` is the only app-owned executable. Box64 loads
`files/bds-runtime/server/bedrock_server` and the official x86_64 glibc bundle
from app data through `BOX64_LD_LIBRARY_PATH`. No PRoot or Ubuntu rootfs is used.

## Device test

1. Install the APK and launch it.
2. Wait for `Installed box64-x86-libs-bundle` in the log.
3. Check status: `box64Present: true` and non-empty `guestLibDirs`.
4. Enter a BDS version and trusted SHA-256, then install it.
5. Press Start. First success marker is `[BOX64] Box64 v0.4.2`.
6. Continue until BDS says `Server started`, then connect over LAN to the
   Poco's IP on UDP port 19132.
7. Test console commands `list`, `stop`, and restart.
8. Save the complete log if it fails. The first 30 lines matter most.

This repository can be assembled before `libbox64.so` is added, but that APK
will not run the server. A real first-test APK requires the verified Box64
artifact in `jniLibs`.
