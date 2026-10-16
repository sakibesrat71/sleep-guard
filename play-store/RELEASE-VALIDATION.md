# Play release candidate 1.0.1 (2)

## Completed locally

- Signed release App Bundle and sideload APK created with a new dedicated RSA-3072 upload key.
- `bundleRelease`, `assembleRelease`, `testReleaseUnitTest` and `lintRelease` passed.
- 26 release unit tests, zero failures/errors. Tests include fresh-install privacy defaults, upgrade preservation of existing keywords, and refusal to process notifications before consent.
- Release lint has zero errors; dependency/resource suggestions remain non-blocking.
- bundletool 1.18.3 validates the App Bundle structure.
- JAR signature verification confirms the AAB is signed. The self-signed upload certificate is expected; it is not a public web-server certificate.
- apksigner validates the APK signature.
- Native ELF PT_LOAD alignment is at least 16 KB for all bundled 64-bit libraries. `zipalign -c -P 16` verifies the APK's native-library packaging alignment. These static checks do not replace a 16 KB device/emulator test or Play Console validation of generated APKs.
- App remains local-only: no new INTERNET permission, SDK, analytics, backend or network processing.

## Release-specific changes

New installations start with no personal keywords. Users configure their own names/phrases. Existing stored keywords remain intact. Notification processing now requires explicit in-app consent before Android Notification Access is requested. Existing users upgrading must accept the disclosure in Setup before monitoring resumes. Consent can be withdrawn locally. The alarm mechanism is otherwise unchanged.

The developer's two-night S24 Ultra report concerns the earlier build. The new disclosure/release packaging has not been retested by that user on hardware yet.

## Signing custody

The upload key and Windows-protected password are outside the source repository. `scripts/build-play-release.ps1` reads the encrypted password locally without printing it. The encrypted password is tied to the Windows user/machine; it is not a portable backup by itself. The account owner should securely back up the key and store its password in a password manager. Use Google Play App Signing for Play distribution. Do not share signing material in email, chat, GitHub, or store assets.

## Not completed

Google Play developer enrollment, payments-profile linking, identity/device/contact verification, registration payment, agreement acceptance, app creation, upload, content declarations, actual phone screenshots/reviewer demo, required closed test, production-access approval and Google review.

**This candidate is built and signed; it is not submitted, approved or published on Google Play.**
