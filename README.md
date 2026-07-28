# MinecraftAndroidServer

This project is an Android WebView control app for an official Minecraft Bedrock Dedicated Server Linux binary.

The app does not bundle Mojang's server. It also refuses to launch without verified runtime artifacts. Runtime assets must be pinned by SHA-256 in `runtime-manifest.json`; placeholders intentionally block installation. This is a security requirement, not a demo.

The official BDS is x86_64 Linux. Android ARM64 therefore needs a compatible x86_64 userspace and Box64. Android kernel/security differences, especially ptrace/seccomp behavior, mean compatibility is device and OS-version dependent. The UI reports errors and never invents a running state.
