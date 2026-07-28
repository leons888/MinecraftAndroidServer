# MinecraftAndroidServer

Android WebView control panel for the official Minecraft Bedrock Dedicated Server.

## Important runtime boundary

Mojang publishes BDS for Linux x86_64 and Windows, not Android ARM64. The app therefore does not execute the server through the Android linker. A compatible embedded Linux glibc userspace and Box64 runtime are required before the official BDS binary can run. The UI reports missing or incompatible runtime errors instead of fabricating a running server.

The app downloads the BDS archive from the official Mojang URL pattern, computes SHA-256, rejects a supplied mismatching hash, extracts with zip-slip protection, forwards stdout/stderr to the WebView, accepts stdin commands, and exposes real Android process metrics.

ACS Lite can open the Gradle Android project directly.
