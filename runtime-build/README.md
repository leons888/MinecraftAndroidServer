# Runtime build pipeline

This directory documents reproducible builds from upstream source. It deliberately does not place third-party binaries in the APK.

## PRoot

The upstream repository provides the source and build instructions. The workflow builds an AArch64 Linux static candidate and fails if the upstream source cannot be cross-built with the supplied toolchain. This is not treated as Android compatibility proof: PRoot depends on ptrace and syscall behavior, so the resulting binary still requires device validation.

## Box64

The upstream Box64 release workflow explicitly has Android and ARM64 build matrix targets. The project workflow pins the source tag `v0.4.2`, uses Android NDK 27.2, and produces a hashed arm64 artifact.

The app should consume only a successful artifact promoted to a GitHub Release after testing on a real ARM64 Android device. GitHub Actions artifacts are temporary and are not suitable as a permanent runtime download URL.
