# MinecraftAndroidServer

## Verified source audit

Confirmed and pinned: Ubuntu 22.04 ARM64 rootfs from the official Ubuntu cloud-image endpoint, and Playit Agent v1.0.10 `linux-aarch64` from the official Playit GitHub release. Both have direct HTTPS URLs and SHA-256 digests in `runtime-manifest.json`.

PRoot is confirmed as an official Ubuntu ARM64 `.deb` with SHA-256, but not as a current standalone binary. Box64 is confirmed as official source plus Debian ARM64 package and GitHub bundle releases. The GitHub release does not expose a standalone ARM64 executable; the Debian endpoint did not expose a verified direct checksum in the checked response. They are therefore not silently installed as executable files.

The Ubuntu ARM64 rootfs is the **host** userspace. It is not the x86_64 guest library set required by BDS. The launch command therefore requires a separate verified PRoot and Box64 plus x86_64 glibc libraries. The code keeps this distinction explicit and refuses to run when those prerequisites are missing.
