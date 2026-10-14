# V1 validation record

Validated on 14 October 2026 in the development environment. This records build and automated checks, not phone behaviour.

## Build

- Windows, Oracle JDK 23.0.2, Gradle wrapper 8.14.3, AGP 8.10.1.
- Installed and verified Android Platform 36; compilation and target SDK 36, minimum SDK 28.
- `gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain` completed successfully.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, application ID `dev.sleepguard`, version `1.0.0` (1).
- APK signature validated using `apksigner verify --verbose` (v2 signature, one debug signer).
- APK manifest inspected using `aapt dump permissions`: no INTERNET, accessibility, exact-alarm, overlay, microphone or location permissions.
- Lint: **0 errors, 8 warnings**. Six warnings suggest newer dependency / Gradle versions; the validated dependency set is pinned. Two adaptive-icon resource warnings are non-blocking; Android 13+ has a separate monochrome resource. No lint baseline hides errors.
- Expected compiler deprecation notices concern compatible older vibration / test notification APIs. Gradle also notes APIs that would need updating before migrating to Gradle 9.

Local debug APK SHA-256 (debug signing keys differ between development / CI environments):

```text
E786E4B02CBD2CF53CE647737FA470B2CAECD02E26447DDE2A1E620492CDB37F
```

## Automated tests

**24 tests, zero failures or errors.**

| Suite | Tests | Coverage |
|---|---:|---|
| ScheduleChecker | 3 | Same-day and overnight windows, exact boundaries, empty / all-day schedule |
| KeywordMatcher | 6 | Case-insensitivity, partial text, Unicode whitespace, multiple rules, disabled rules, all fields |
| DuplicateDetector / cooldown | 5 | Expiry, changed content, fingerprint boundaries, bounded cache, optional independent cooldown |
| NotificationProcessor | 6 | Central Teams deployment mention, disabled/out-of-schedule/unselected gates, reposts, identical new message timestamps, all-day, cosmetic summary updates |
| NotificationTextExtractor | 4 | Standard/custom text extras, deduplication, unexpected values, individual MessagingStyle entries, raw message bundles |

Extractor tests use Robolectric at API 28 to exercise the minimum supported platform. Automated tests do not emulate all vendor notification payloads or modern Android background policy.

## GitHub workflow

The workflow builds, runs tests and lint on JDK 17, then uploads the debug APK and reports. Initial CI exposed a removed Android SDK `tools` package in the setup action's default; the workflow now explicitly requests `platform-tools`. Check the current run and artifacts in [GitHub Actions](https://github.com/sakibesrat71/sleep-guard/actions).

## Still requires real hardware

No physical Android device or emulator was connected during validation. Samsung Galaxy S24 Ultra behaviour is **not hardware-tested**.

Follow the README checklist for Teams/Slack payloads, immediate locked-phone detection, looping audio, repeated vibration, DND, alarm volume/audio routing, foreground-service eligibility, full-screen permissions, Stop cleanup, source destinations, reboot reconnection, work-profile policy, cooldown, duplicate updates and overnight idle reliability. Review battery exemption before relying on background alarms.

An unrestricted foreground Test Alarm demonstrates a different device state from a real incoming notification while locked. Test both. Force-stop, process termination, notification-preview suppression, organisation policies and Android/OEM restrictions remain platform limits.
