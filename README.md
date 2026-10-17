# Mindful Hub

A personal Android app that keeps the useful parts of social media and puts friction on the
addictive parts. Every feature traces to research. Start with [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md),
then [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Build (Arch Linux)

Needs JDK 21 and the Android SDK in `~/Android/Sdk` (platform `android-37.0`, build-tools 36.1).
`local.properties` points to the SDK and is not committed.

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew :core:test            # pure-Kotlin rules (logical day, sessions, heatmap)
./gradlew :app:assembleDebug    # APK at app/build/outputs/apk/debug/app-debug.apk
~/Android/Sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Installing with `adb` means Android 15 doesn't treat the app as sideloaded, so there's no
"restricted settings" step (D-017, STACK-OPTIONS S8).

## Modules

`:core` pure Kotlin rules · `:data` Room + UsageStats import · `:app` Compose UI and workers.
`:gate` (accessibility service) comes in milestone 2.
