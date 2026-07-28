# jniLibs/arm64-v8a

Drop `libbox64.so` here before building the APK.

It is the ARM64 Android Box64 build produced by
`.github/workflows/runtime-build.yml` (job `box64-android-arm64`, artifact
`box64-android-arm64`, file `libbox64.so`).

Why a `.so` name for an executable: starting with Android 10 (API 29) an app may
not `exec()` anything inside its own data directory. `nativeLibraryDir` is the
only app-owned location where execution is still permitted, and only files named
`lib*.so` are extracted there. So Box64 is shipped as `libbox64.so` and executed
from `nativeLibraryDir`.

BDS itself does not need this treatment: Box64 maps and relocates the x86_64 ELF
with its own loader, so `bedrock_server` never goes through the kernel's
`execve()` and can live in app data without an exec bit.

Only `*.so` files are packaged into the APK, so this README is ignored by the
build.
