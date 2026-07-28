# MinecraftAndroidServer

Minecraft Bedrock Dedicated Server on stock Android. No Termux, no root, no
PRoot, no Linux rootfs.

## How the server actually runs

```
APK
 └─ nativeLibraryDir/libbox64.so        (ARM64 Box64, shipped in the APK)
      └─ exec()  ← allowed: nativeLibraryDir is the only app-owned exec path on API 29+
           └─ loads files/bds-runtime/server/bedrock_server   (x86_64, mapped by Box64's ELF loader)
                with BOX64_LD_LIBRARY_PATH = server dir : files/bds-runtime/x86lib/**
                                                          (official Box64 x86_64 glibc bundle)
```

Two rules make this work on an unrooted device:

1. **Only Box64 is exec'd, and only from `nativeLibraryDir`.** Android 10+
   refuses `execve()` on files in the app data directory, and only `lib*.so`
   entries are extracted into `nativeLibraryDir`, hence the name `libbox64.so`.
2. **Nothing else is ever exec'd.** `bedrock_server`, its libraries and the
   playit agent are all x86_64 and are mapped by Box64 itself, so they can live
   in app data with no exec bit.

The playit tunnel agent runs the same way: the pinned **x86_64** `playit-cli`
under Box64, because the official aarch64 playit build is glibc-linked and
cannot run on Android's bionic userspace.

## Building the APK (AIDE / ACS Lite)

1. Run `.github/workflows/runtime-build.yml` (Actions → Run workflow).
2. Download the `box64-android-arm64` artifact.
3. Put `libbox64.so` into `app/src/main/jniLibs/arm64-v8a/`.
4. Open the project in AIDE or ACS Lite and build. `arm64-v8a` is the only ABI,
   `extractNativeLibs` is `true`, and `doNotStrip` keeps `libbox64.so` intact.

Without step 3 the APK still builds and installs, and the UI reports
`libbox64.so is missing from …` instead of pretending to be ready.

## First run on the device

1. Launch the app. It fetches `runtime-manifest.json` and installs the pinned
   artifacts into app data with SHA-256 verification:
   * `box64-bundle-x86-libs-v0.4.2.tar.gz` → `files/bds-runtime/x86lib`
   * `playit-cli-linux-amd64` → `files/bds-runtime/bin`
2. Enter the BDS version and its trusted SHA-256, accept the Mojang terms, and
   install. BDS lands in `files/bds-runtime/server`.
3. Press Start. Box64 launches `bedrock_server`; stdout/stderr stream into the
   log view and console commands go to its stdin.

## Verification status

The Box64 build, the pinned hashes and the launch path are in place, but
**BDS has not yet been observed starting on a physical device**. Until that
happens the app reports the runtime as present, not as proven.

## Testing on a Poco X4 GT (Android 14)

Dimensity 8100, arm64-v8a, 4 KB pages: a supported Box64 configuration.

1. Install the APK (allow install from unknown sources).
2. Open the app, let the runtime install finish. Expect
   `Installed box64-x86-libs-bundle` and `Installed playit-cli-linux-amd64`.
3. Check that the status block shows `box64Present: true` and a non-empty
   `guestLibDirs`.
4. Install BDS with a version and matching SHA-256.
5. Press Start and read the first log lines:
   * `[BOX64] Box64 v0.4.2 … Running on …` means Box64 itself started.
   * `[BOX64] Error loading …` or a missing-library message means
     `BOX64_LD_LIBRARY_PATH` does not cover something BDS needs.
   * `Permission denied` on `libbox64.so` means `extractNativeLibs` was lost or
     the ABI split removed the library.
6. Wait for `Server started.` in the BDS output, then join from the Minecraft
   client on the same Wi-Fi at the device's LAN IP, port 19132 (UDP).
7. Send `list` and then `stop` in the console to confirm stdin and clean
   shutdown, then confirm the watchdog does not restart after a manual stop.
8. For remote play, start playit and drive the claim flow from the log output.
9. If the process dies instantly, retry with `BOX64_DYNAREC=0` behaviour in mind
   (interpreter only, much slower) to separate dynarec bugs from loader bugs.

Keep the device on a charger and disable battery optimisation for the app: the
foreground service survives screen-off, aggressive OEM power management does not
always respect it.
