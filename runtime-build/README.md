# runtime-build

One artifact is built here: **Box64 for Android ARM64**.

`.github/workflows/runtime-build.yml` compiles Box64 v0.4.2 from the official
source with the same flags upstream uses for its `ANDROID` target (NDK r26b
`aarch64-linux-android31-clang`, `-DANDROID=1 -DARM_DYNAREC=1 -DBAD_SIGNAL=1`).
It emits `libbox64.so` plus a SHA-256, and prints `file` / `readelf -h` /
`readelf -d` so the ELF class, ABI and dependencies can be checked from the log.

The artifact is not trusted automatically. It has to be:

1. downloaded from the successful run,
2. verified on a real ARM64 Android device,
3. promoted to an immutable `runtime-v*` GitHub Release,
4. copied into `app/src/main/jniLibs/arm64-v8a/libbox64.so` for the APK build.

## Why PRoot and the Ubuntu rootfs are gone

They could not work in an unrooted APK:

* Android API 29+ blocks `execve()` on anything inside the app's data directory,
  so a downloaded PRoot binary can never be launched. Only `lib*.so` files in
  `nativeLibraryDir` remain executable.
* PRoot depends on `ptrace`, which is heavily restricted by Android's seccomp
  policy outside of Termux's special environment.
* The rootfs was an ARM64 *host* userspace. BDS is x86_64, so it provided none
  of the libraries that were actually missing.

Box64 replaces all of it: it runs from `nativeLibraryDir` and maps the x86_64
`bedrock_server` with its own ELF loader, which means BDS itself never goes
through the kernel's `execve()` and needs no exec bit. The x86_64 glibc guest
libraries come from the official `box64-bundle-x86-libs` release asset pinned in
`runtime-manifest.json`.
