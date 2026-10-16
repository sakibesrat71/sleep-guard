# Play Console submission — preparation and remaining steps

## Current state

The developer reports two successful nights on a Samsung Galaxy S24 Ultra. This is user-reported testing, not a formal certification or a completed Play closed test. Google Play enrollment is not yet completed. No app has been submitted or published to Play.

Approved listing defaults: EN corporation, sakibesrat71@gmail.com, Sleep Guard, free, no ads/purchases, English, Tools, worldwide where permitted, dev.sleepguard.

## Account owner must complete

1. Open https://play.google.com/console/signup while signed in as sakibesrat71@gmail.com.
2. Choose the truthful account type. A public developer brand is not proof of a registered organisation. Organisation enrollment can require registration / D-U-N-S details.
3. Supply Google's requested account / identity details directly in Console, pay the registration fee displayed by Google, and personally accept the Developer Distribution Agreement. Do not send passwords, identity documents, payment details, or recovery codes in chat.
4. Complete email / phone / device verification as Google requires.
5. For a new personal developer account, complete the required closed test (currently 12 opted-in testers continuously for 14 days) and apply for production access. An internal test alone does not satisfy it.

## Release artefacts

Use the signed .aab for Play uploads; the separate release APK is for sideload checks. Upload signing files are outside the public source repository under the private signing directory. Never upload the keystore/password to GitHub, store assets, support email, or Play's listing attachments. Use Google Play App Signing for the actual distribution signing key.

The previous debug APK is signed differently from the upload key / Play distribution key. Do not expect a Play build to update over that debug install. Uninstalling the debug app removes its local settings/history; retain the working phone install until ready to migrate, and record settings manually first.

## Data safety draft — confirm against the actual Console questions

- App data collected off-device: No. App data shared: No. Notification text and history are processed/stored locally; app has no INTERNET permission or collection SDKs.
- No Sleep Guard account creation. Account-deletion form flow is not applicable to a nonexistent account. Local history can be cleared; app storage/uninstall removes local data.
- No ads, purchases, tracking, location, camera, microphone, contacts, Accessibility or external storage access.
- Do not claim independent security certification, end-to-end encryption, or an app-level encrypted database. Private storage and Android device security are distinct from those claims.
- Support email and the externally hosted policy website are separate from app notification processing; privacy policy explains them.
- Reference: https://support.google.com/googleplay/android-developer/answer/10787469

## App access, rating and audience

No app login or reviewer credentials are required. Reviewers can use Test Alarm without Teams/Slack accounts. Real notification tests require selected source apps to deliver message previews. Complete content-rating questions truthfully: Sleep Guard supplies no curated social, violent, sexual, gambling, or news content; it displays user-device notification content. Do not invent a final rating; use IARC's result. Intended audience is adults using workplace messaging; choose the actual intended age groups in Console. Not designed as a children's app, health service, emergency alert guarantee, government service or financial product.

## Foreground service declaration draft

Type: mediaPlayback (FOREGROUND_SERVICE_MEDIA_PLAYBACK).
Core use: repeat user-configured alarm audio after a locally matched notification or an explicit Test Alarm.
User benefit: audible notification-mention alarm during chosen monitoring times.
Initiation: user enables monitoring and configures rules; matching notification triggers alarm. Test Alarm is a direct user action.
Visibility: ongoing alarm notification, STOP action, and alarm screen where permitted.
Ending: explicit STOP releases MediaPlayer, vibration, foreground state and wake locks. There is no permanent monitoring FGS or notification polling.
Deferral: would defeat the time-sensitive alarm purpose. Background start remains subject to Android restrictions and documented setup requirements.

Record a real demonstration: configure guard and keyword, send a matching Teams/Slack notification while locked, show the ongoing foreground alarm and STOP action. Also demonstrate Test Alarm. Supply a review-accessible video URL if Console requests it. Do not present a staged mockup as operating footage.

## Full-screen intent declaration draft

Sleep Guard's core purpose is a user-configured alarm triggered by selected notification keywords during scheduled monitoring. It uses full-screen intent only for this alarm interface, never ads or unsolicited promotion. User controls schedule, app selection, keywords, and Stop. Declare the actual notification-triggered alarm use case accurately; do not describe it as a scheduled exact AlarmManager alarm. Google determines whether it qualifies for pre-grant; there is no promise of approval. User-granted full-screen access remains subject to Android restrictions.

Reference: https://support.google.com/googleplay/android-developer/answer/13392821

## Required assets still dependent on a real run

- Store icon: 512 × 512 PNG, prepared from the app vector.
- Feature graphic: 1024 × 500 PNG, prepared.
- At least two accurate phone screenshots (for example dashboard, keywords/schedule, and alarm screen). Capture the app on a device/emulator; redact private real messages, use synthetic demo messages. Do not use fabricated UI as screenshots.
- A short permission/foreground-alarm demonstration video if requested by the declaration form.

## Submission order

Enroll and verify → create app → choose Play App Signing → upload signed bundle to internal test → complete app content, privacy, store assets and declarations → review automated/pre-launch checks → closed testing if required → production access application → submit production release for Google's review. Publication depends on account eligibility and Google's approval.
