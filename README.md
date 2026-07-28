# MinecraftAndroidServer

## Runtime build status

The repository now contains reproducible GitHub Actions builds from official PRoot and Box64 sources under `runtime-build/`. Box64 upstream publishes an Actions matrix containing Android and ARM64 targets, so a source build is technically possible. PRoot upstream builds on Linux and has ARM64 source support, but cross-compilation and Android ptrace/seccomp compatibility still require validation on a real device.

The workflow uploads hashed artifacts only. They are not automatically trusted by the APK: an artifact must first be tested on ARM64 Android, promoted to an immutable GitHub Release asset, and pinned in `runtime-manifest.json`.

This avoids the old mistake of treating a source repository, temporary Actions artifact, or unsigned mirror as a production runtime binary.
