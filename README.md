# MinecraftAndroidServer

## Checked runtime sources

- Ubuntu Jammy ARM64 rootfs: official Ubuntu cloud-image endpoint, with SHA-256 pinned in `runtime-manifest.json`.
- Playit Agent `v1.0.10` ARM64 Linux: official GitHub release asset, with GitHub release digest pinned in `runtime-manifest.json`.
- PRoot: official Ubuntu ARM64 `.deb` exists and has a published SHA-256, but it is not a standalone binary. The current verified pipeline does not silently treat a `.deb` as an executable.
- Box64: official source, Debian ARM64 package, and official GitHub bundle releases exist. The checked official GitHub release does not publish a standalone ARM64 executable, and the checked Debian endpoint did not expose a usable direct SHA-256 asset for safe unattended installation. It remains blocked rather than guessed.
- Official BDS: Mojang publishes the Linux server for Ubuntu 22.04+ and requires accepting Mojang terms. Android ARM64 still needs Box64 and a compatible Linux userspace.

The project downloads only artifacts with a pinned SHA-256. Missing or unverified runtime components deliberately stop startup instead of producing a fake running state.
