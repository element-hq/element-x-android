Changes in Element X v0.6.5 (2024-10-09)
========================================

## What's Changed
### ✨ Features
* Add developer setting to hide images in the timeline by @bmarty in https://github.com/element-hq/element-x-android/pull/3592
* Warn the user when unverified user has changed their identity by @bmarty in https://github.com/element-hq/element-x-android/pull/3621
### 🙌 Improvements
* Handle no network error when starting Element Call. by @bmarty in https://github.com/element-hq/element-x-android/pull/3527
### 🐛 Bugfixes
* Fix room settings not treating unencrypted DMs as DMs by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3545
* Fix crash when aspectRatio is null. by @bmarty in https://github.com/element-hq/element-x-android/pull/3561
* Don't delete uploaded logs by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3540
* Don't display security banner for unknown RecoveryState by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3579
* Fix the logic of the room list banner state by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3615
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3560
* Sync Strings - import translations to Persian by @ElementBot in https://github.com/element-hq/element-x-android/pull/3612
### 🧱 Build
* Introduce ModulesConfig by @bmarty in https://github.com/element-hq/element-x-android/pull/3530
* Centralise the DI code generation logic by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3562
* Update Gradle impl module template with `setupAnvil()` call by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3563
* Use Anvil KSP instead of the Square KAPT one by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3564
* Upgrade the used JDK in the project to v21 by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3582
* Merge unit, screenshot tests and coverage in a single CI call by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3593
* Disable configuration cache in the CI by default by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3601
* Fix screenshot recording in CI by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3607
* Ensure the CI compile and execute all the unit tests. by @bmarty in https://github.com/element-hq/element-x-android/pull/3617
### Dependency upgrades
* Update dependency androidx.compose:compose-bom to v2024.09.00 by @renovate in https://github.com/element-hq/element-x-android/pull/3399
* Update dependency androidx.compose:compose-bom to v2024.09.02 by @renovate in https://github.com/element-hq/element-x-android/pull/3544
* Update dependency io.element.android:compound-android to v0.1.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3524
* Update dependency com.google.firebase:firebase-bom to v33.3.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3549
* Update dependency org.maplibre.gl:android-sdk to v11.5.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3550
* Update dependency org.maplibre.gl:android-plugin-annotation-v9 to v3.0.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3505
* Update dependency androidx.webkit:webkit to v1.12.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3520
* Update dependency com.posthog:posthog-android to v3.7.5 by @renovate in https://github.com/element-hq/element-x-android/pull/3546
* Update gradle-update/update-gradle-wrapper-action action to v2 by @renovate in https://github.com/element-hq/element-x-android/pull/3551
* Update dependency com.lemonappdev:konsist to v0.16.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3371
* Update android.gradle.plugin to v8.6.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3504
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.49 by @renovate in https://github.com/element-hq/element-x-android/pull/3553
* Update lifecycle to v2.8.6 by @renovate in https://github.com/element-hq/element-x-android/pull/3398
* Update dependency com.google.accompanist:accompanist-permissions to v0.36.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3400
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.50 by @renovate in https://github.com/element-hq/element-x-android/pull/3565
* Update dependency com.google.firebase:firebase-bom to v33.4.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3578
* Update android.gradle.plugin to v8.7.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3577
* Update dependency com.posthog:posthog-android to v3.8.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3591
* dependency: Bump rust sdk to 0.2.51 by @BillCarsonFr in https://github.com/element-hq/element-x-android/pull/3602
* chore(deps): update dependencyanalysis to v2.1.3 by @renovate in https://github.com/element-hq/element-x-android/pull/3559
* Update wysiwyg to v2.37.13 by @renovate in https://github.com/element-hq/element-x-android/pull/3596
* fix(deps): update dependency io.nlopez.compose.rules:detekt to v0.4.15 by @renovate in https://github.com/element-hq/element-x-android/pull/3595
* fix(deps): update dependency com.google.testparameterinjector:test-parameter-injector to v1.18 by @renovate in https://github.com/element-hq/element-x-android/pull/3606
* fix(deps): update dependency com.squareup:kotlinpoet-ksp to v1.18.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3580
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.52 by @renovate in https://github.com/element-hq/element-x-android/pull/3619
* SDK 0.2.53 19b9a73ecc3e31d502dbf0c5850bfdfaddf02afe by @bmarty in https://github.com/element-hq/element-x-android/pull/3622
* Update dependency org.maplibre.gl:android-sdk to v11.5.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3608
### Others
* rename invisible flag to onlySignedDeviceIsolation flag by @BillCarsonFr in https://github.com/element-hq/element-x-android/pull/3542
* Fix image viewer glitch by @ganfra in https://github.com/element-hq/element-x-android/pull/3537
* Prefix message sent by the current user by `You` instead of the sender name. by @bmarty in https://github.com/element-hq/element-x-android/pull/3547
* timeline : remove animateItem by @ganfra in https://github.com/element-hq/element-x-android/pull/3548
* Fix a couple of build-time warnings in Gradle output by @frebib in https://github.com/element-hq/element-x-android/pull/3349
* Use MSC2530 filename when loading media by @frebib in https://github.com/element-hq/element-x-android/pull/3567
* Prevent crash with duplicate room suggestion by @frebib in https://github.com/element-hq/element-x-android/pull/3576
* Add unit tests on TimelineItemsSubscriber by @bmarty in https://github.com/element-hq/element-x-android/pull/3554
* Fix tests on develop by @bmarty in https://github.com/element-hq/element-x-android/pull/3585
* Timeline better jump to behaviours by @ganfra in https://github.com/element-hq/element-x-android/pull/3597
* Fix building the app using a local SDK. by @bmarty in https://github.com/element-hq/element-x-android/pull/3604
* crypto: Use OnlySigned isolation flag to setup decryption trust req. by @BillCarsonFr in https://github.com/element-hq/element-x-android/pull/3569
* Fix black-on-black status bars with hidden media by @frebib in https://github.com/element-hq/element-x-android/pull/3611
* Remove supportSlidingSync boolean. by @bmarty in https://github.com/element-hq/element-x-android/pull/3609
* Ensure that `Presenter`s do not depend on other presenters. by @bmarty in https://github.com/element-hq/element-x-android/pull/3618
* Do not render pin violation in clear rooms. by @bmarty in https://github.com/element-hq/element-x-android/pull/3630

Changes in Element X v0.6.4 (2024-09-25)
========================================

### 🙌 Improvements
* Pinned messages : add pin icon in timeline for pinned events. by @ganfra in https://github.com/element-hq/element-x-android/pull/3500
* Include inviter in the notification for invitation by @bmarty in https://github.com/element-hq/element-x-android/pull/3503

### 🐛 Bugfixes
* Fix crash when session is deleted on another client by @bmarty in https://github.com/element-hq/element-x-android/pull/3515
* Fix pinned events banner reappearing when loading by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3519
* Fix various crashes by @bmarty in https://github.com/element-hq/element-x-android/pull/3533
* Perform the migration, even if the current version is not known. by @bmarty in https://github.com/element-hq/element-x-android/pull/3535
* timeline : makes sure to emit empty list if initial reset has no item. by @ganfra in https://github.com/element-hq/element-x-android/pull/3538

### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3513
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3517

### Dependency upgrades
* Update dependency io.nlopez.compose.rules:detekt to v0.4.12 by @renovate in https://github.com/element-hq/element-x-android/pull/3436
* Update dependency com.posthog:posthog-android to v3.7.3 by @renovate in https://github.com/element-hq/element-x-android/pull/3443
* Update dependency com.otaliastudios:transcoder to v0.11.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3440
* Update dependency org.maplibre.gl:android-sdk to v11.4.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3408
* Update dependencyAnalysis to v2.0.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3508
* Update dependency org.maplibre.gl:android-sdk-ktx-v7 to v3.0.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3507
* Update dependencyAnalysis to v2.1.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3526
* Update dependency net.java.dev.jna:jna to v5.15.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3525
* Update dependency androidx.startup:startup-runtime to v1.2.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3516
* dependencies : update rust sdk to 0.2.48 by @ganfra in https://github.com/element-hq/element-x-android/pull/3532

### Others
* Change ElementBot mail to android@element.io by @bmarty in https://github.com/element-hq/element-x-android/pull/3497
* Test RustMatrixClient and other classes in the matrix module by @bmarty in https://github.com/element-hq/element-x-android/pull/3501
* Pinned messages analytics by @ganfra in https://github.com/element-hq/element-x-android/pull/3523
* Remove ability to configure default log level by @bmarty in https://github.com/element-hq/element-x-android/pull/3531

Changes in Element X v0.6.3 (2024-09-19)
========================================

## What's Changed
### 🙌 Improvements
* Iterate send failure verification by @ganfra in https://github.com/element-hq/element-x-android/pull/3485
### 🐛 Bugfixes
* Make sure the logout action doesn't cause a crash by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3480
* Distinguish between roomId and roomAlias. by @bmarty in https://github.com/element-hq/element-x-android/pull/3486
* Fix sliding sync proxy login not working after native SS failure by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3489
### Dependency upgrades
* SDK 0.2.47 by @ganfra in https://github.com/element-hq/element-x-android/pull/3490
### Others
* Add tests on AccountDeactivationView by @bmarty in https://github.com/element-hq/element-x-android/pull/3481
* Cleanup and fixtures for SDK classes. by @bmarty in https://github.com/element-hq/element-x-android/pull/3488
* Timeline related improvements by @ganfra in https://github.com/element-hq/element-x-android/pull/3487
* Room list : debounce subscribe to visible rooms. by @ganfra in https://github.com/element-hq/element-x-android/pull/3491
* Improve code coverage metrics by @bmarty in https://github.com/element-hq/element-x-android/pull/3450

### ✨ Features
* Account deactivation. by @bmarty in https://github.com/element-hq/element-x-android/pull/3479

Changes in Element X v0.6.1 (2024-09-17)
========================================

### ✨ Features
* Add forced logout flow when the proxy is no longer available by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3458
* Temporary account creation using Element Web. by @bmarty in https://github.com/element-hq/element-x-android/pull/3467

### 🙌 Improvements
* Feature/valere/invisible crypto feature flag by @BillCarsonFr in https://github.com/element-hq/element-x-android/pull/3451
* Require acknowledgement to send to a verified user if their identity changed or if a device is unverified. by @ganfra in https://github.com/element-hq/element-x-android/pull/3461
* Update pinned message actions by @ganfra in https://github.com/element-hq/element-x-android/pull/3438

### 🐛 Bugfixes
* Fix events blinking at the beginning of DM by @bmarty in https://github.com/element-hq/element-x-android/pull/3449
* Fix not being able to decline an invite from the room list by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3466

### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3464
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3469
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3476
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3477

### Others
* Upgrade Rust sdk to 0.2.45 by @bmarty in https://github.com/element-hq/element-x-android/pull/3472
* SDK 0.2.46 by @bmarty in https://github.com/element-hq/element-x-android/pull/3475

Changes in Element X v0.6.0 (2024-09-12)
========================================

### 🙌 Improvements
* Enables pinned messages feature by default. by @ganfra in https://github.com/element-hq/element-x-android/pull/3439
* Pinned messages list : hide reactions by @ganfra in https://github.com/element-hq/element-x-android/pull/3430

### 🐛 Bugfixes
* Feature/fga/pinned messages fix timeline provider by @ganfra in https://github.com/element-hq/element-x-android/pull/3432

### Dependency upgrades
* Update activity to v1.9.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3397
* Update peter-evans/create-pull-request action to v7 by @renovate in https://github.com/element-hq/element-x-android/pull/3383
* Rust sdk upgrade to 0.2.43 by @bmarty in https://github.com/element-hq/element-x-android/pull/3446

### Others
* DeviceId and cleanup. by @bmarty in https://github.com/element-hq/element-x-android/pull/3442
* Update application store assets by @bmarty in https://github.com/element-hq/element-x-android/pull/3441

Changes in Element X v0.5.3 (2024-09-10)
========================================

### ✨ Features
* Add banner for optional migration to simplified sliding sync by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3429

### 🙌 Improvements
* Timeline : remove the encrypted history banner by @ganfra in https://github.com/element-hq/element-x-android/pull/3410

### 🐛 Bugfixes
* Fix new logins with Simplified SS using the proxy by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3417
* Ensure Call is not hang up when user is asked to grant system permissions by @bmarty in https://github.com/element-hq/element-x-android/pull/3419
* Wait for a room with joined state in `/sync` after creating it by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3421
* [Bugfix] : fix self verification flow by @ganfra in https://github.com/element-hq/element-x-android/pull/3426

### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3425

### 🚧 In development 🚧
* [Feature] Pinned messages list by @ganfra in https://github.com/element-hq/element-x-android/pull/3392
* Pinned messages banner : adjust indicator to match design. by @ganfra in https://github.com/element-hq/element-x-android/pull/3415

### Dependency upgrades
* Update plugin dependencycheck to v10.0.4 by @renovate in https://github.com/element-hq/element-x-android/pull/3372
* Update plugin detekt to v1.23.7 by @renovate in https://github.com/element-hq/element-x-android/pull/3424

### Others
* Delete old log files by @bmarty in https://github.com/element-hq/element-x-android/pull/3413
* Recovery key formatting and wording iteration by @bmarty in https://github.com/element-hq/element-x-android/pull/3409
* Change license to AGPL by @bmarty in https://github.com/element-hq/element-x-android/pull/3422
* Remove Wait list screen by @bmarty in https://github.com/element-hq/element-x-android/pull/3428

Changes in Element X v0.5.2 (2024-09-05)
=========================================

### 🙌 Improvements
* [Identity reset] Remove instruction to reset identity on another client. by @bmarty in https://github.com/element-hq/element-x-android/pull/3355
* Redact message on displayed notification by @bmarty in https://github.com/element-hq/element-x-android/pull/3320
* Add a way to sign out when the user is asked to verify the session. by @bmarty in https://github.com/element-hq/element-x-android/pull/3359
* Add banner entry point to set up recovery by @bmarty in https://github.com/element-hq/element-x-android/pull/3360
* Replace OSS licenses plugin with Licensee and some manually done UI. by @bmarty in https://github.com/element-hq/element-x-android/pull/3381

### 🐛 Bugfixes
* Small fixes around logging out. by @bmarty in https://github.com/element-hq/element-x-android/pull/3356
* Ensure starting PinUnlockActivity does not crash the application. by @bmarty in https://github.com/element-hq/element-x-android/pull/3369
* Use the right colors for `@room` mention pills by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3376
* Fix avatar sometimes not loading by @bmarty in https://github.com/element-hq/element-x-android/pull/3366
* Make pinned events required state in SlidingSync by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3385
* Make sure to save the tokens the Client might return when its session is restored by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3378
* Fix Element Call closing automatically on API 34 by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3402

### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3373

### 🧱 Build
* Try adding a memory limit for the kotlin compiler by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3377

### Dependency upgrades
* Update dependency com.google.testparameterinjector:test-parameter-injector to v1.17 by @renovate in https://github.com/element-hq/element-x-android/pull/3357
* Update dependencyAnalysis to v2.0.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3362
* Update android.gradle.plugin to v8.6.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3363
* Update dependency io.nlopez.compose.rules:detekt to v0.4.11 by @renovate in https://github.com/element-hq/element-x-android/pull/3364
* Update dependency com.posthog:posthog-android to v3.6.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3358
* Update mobile-dev-inc/action-maestro-cloud action to v1.9.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3367
* Update dependency com.posthog:posthog-android to v3.6.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3368
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.41 by @renovate in https://github.com/element-hq/element-x-android/pull/3384
* Rust sdk : update to 0.2.42 by @ganfra in https://github.com/element-hq/element-x-android/pull/3393
* Update dependency com.android.tools:desugar_jdk_libs to v2.1.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3350
* Update dependency com.sigpwned:emoji4j-core to v15.1.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3396

### Others
* Release : use a different concurrency group for enterprise build by @ganfra in https://github.com/element-hq/element-x-android/pull/3351
* Provide distinct cache directory to the Rust SDK. by @bmarty in https://github.com/element-hq/element-x-android/pull/3370
* Remove the migration screen by @bmarty in https://github.com/element-hq/element-x-android/pull/3389
* Unified push endpoint: do not fallback to default endpoint in case of failure and add troubleshoot test. by @bmarty in https://github.com/element-hq/element-x-android/pull/3388
* Skip device verification screen when creating a new account using OIDC by @bmarty in https://github.com/element-hq/element-x-android/pull/3395
* Big emoji-only messages by @frebib in https://github.com/element-hq/element-x-android/pull/3295

Changes in Element X v0.5.1 (2024-08-28)
=========================================

### ✨ Features
* Add simplified sliding sync toggle to developer options by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3222
* Feature: identity reset by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3298
* Timeline UI | MessageShield Support by @BillCarsonFr in https://github.com/element-hq/element-x-android/pull/3240
* Suggestion for room alias (disabled for now) by @bmarty in https://github.com/element-hq/element-x-android/pull/3322
* Allow `PictureInPicture` mode for Element Call. by @bmarty in https://github.com/element-hq/element-x-android/pull/3345

### 🙌 Improvements
* Join Room : allow to join by alias (and getPreview) by @ganfra in https://github.com/element-hq/element-x-android/pull/3241
* [Feature] Pinned message : render m.room.pinned events in timeline by @ganfra in https://github.com/element-hq/element-x-android/pull/3276
* Enable sync on push feature flag to partially sync when notifications arrive by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3290
* Improve the text for mentions and replies in notifications by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3328
* Use new functions exposed by Element Call about PiP by @bmarty in https://github.com/element-hq/element-x-android/pull/3334

### 🐛 Bugfixes
* Ensure sessionPath is not reused for different homeserver. Fixes not loading media issue. by @bmarty in https://github.com/element-hq/element-x-android/pull/3299
* Fix reset identity with password stuck in loading state. by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3317

### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3252
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3267
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3297
* Sync Strings - New language: Dutch. by @ElementBot in https://github.com/element-hq/element-x-android/pull/3308
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3339

### 🧱 Build
* Update sonarcloud project key by @guillaumevillemont in https://github.com/element-hq/element-x-android/pull/3264
* Fix `build_rust_sdk.sh` script to work on linux by @erikjohnston in https://github.com/element-hq/element-x-android/pull/3291
* Fix proguard config for nightly and release builds by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3294
* Gradle update action: Use JDK 17 and skip early in forks. by @bmarty in https://github.com/element-hq/element-x-android/pull/3311
* Gradle update action: add label and use other token. by @bmarty in https://github.com/element-hq/element-x-android/pull/3313
* Update Gradle Wrapper from 8.9 to 8.10 by @ElementBot in https://github.com/element-hq/element-x-android/pull/3314

### 🚧 In development 🚧
* WIP Pinned events : add feature flag and pin/unpin actions by @ganfra in https://github.com/element-hq/element-x-android/pull/3255
* WIP Pinned events : start creating the banner ui, no logic. by @ganfra in https://github.com/element-hq/element-x-android/pull/3259
* WIP Pinned events : banner logic by @ganfra in https://github.com/element-hq/element-x-android/pull/3275

### Dependency upgrades
* Update dependency org.maplibre.gl:android-sdk to v11.1.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3244
* Update activity to v1.9.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3242
* Update media3 to v1.4.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3247
* Update dependency androidx.annotation:annotation-jvm to v1.8.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3243
* Update dependencyAnalysis to v1.33.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3250
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.35 by @renovate in https://github.com/element-hq/element-x-android/pull/3249
* Update dependency io.sentry:sentry-android to v7.12.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3246
* Update dependency io.nlopez.compose.rules:detekt to v0.4.8 by @renovate in https://github.com/element-hq/element-x-android/pull/3254
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.36 by @renovate in https://github.com/element-hq/element-x-android/pull/3269
* Update wysiwyg to v2.37.8 by @renovate in https://github.com/element-hq/element-x-android/pull/3263
* Update dependency io.sentry:sentry-android to v7.13.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3258
* Update dependency io.nlopez.compose.rules:detekt to v0.4.9 by @renovate in https://github.com/element-hq/element-x-android/pull/3277
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.38 by @renovate in https://github.com/element-hq/element-x-android/pull/3280
* Update dependency androidx.annotation:annotation-jvm to v1.8.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3282
* Update kotlin by @renovate in https://github.com/element-hq/element-x-android/pull/2990
* Update dependency io.nlopez.compose.rules:detekt to v0.4.10 by @renovate in https://github.com/element-hq/element-x-android/pull/3281
* Update dependency com.posthog:posthog-android to v3.5.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3287
* Update wysiwyg to v2.37.8 by @renovate in https://github.com/element-hq/element-x-android/pull/3284
* Update the SDK bindings to `v0.2.39` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3288
* Update gradle/actions action to v4 by @renovate in https://github.com/element-hq/element-x-android/pull/3265
* Update android.gradle.plugin to v8.5.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3004
* Update dependency io.sentry:sentry-android to v7.13.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3285
* Update dependency io.sentry:sentry-android to v7.14.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3302
* Update dependency androidx.test:runner to v1.6.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3304
* Update dependency com.otaliastudios:transcoder to v0.11.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3306
* Update lifecycle to v2.8.0 by @renovate in https://github.com/element-hq/element-x-android/pull/2848
* Update lifecycle to v2.8.4 by @renovate in https://github.com/element-hq/element-x-android/pull/3315
* Update dagger to v2.52 by @renovate in https://github.com/element-hq/element-x-android/pull/3270
* Update telephoto to v0.13.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3325
* Update dependency androidx.compose:compose-bom to v2024.08.00 by @renovate in https://github.com/element-hq/element-x-android/pull/3323
* Update dependency com.google.firebase:firebase-bom to v33.2.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3331
* Update dependency com.posthog:posthog-android to v3.5.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3340
* Update dependency com.android.tools:desugar_jdk_libs to v2.1.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3341
* Update dependencyAnalysis to v2 (major) by @renovate in https://github.com/element-hq/element-x-android/pull/3346
* Update dependency org.maplibre.gl:android-sdk to v11.2.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3347
* Update media3 to v1.4.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3344
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.40 by @renovate in https://github.com/element-hq/element-x-android/pull/3343

### Others
* Feature/fga/push subscribe to room by @ganfra in https://github.com/element-hq/element-x-android/pull/3257
* Feature/fga/start sync on push by @ganfra in https://github.com/element-hq/element-x-android/pull/3260
* Cleanup and add unit test for DefaultPinnedMessagesBannerFormatter by @bmarty in https://github.com/element-hq/element-x-android/pull/3307
* Add test on function name which may start or end with spaces by @bmarty in https://github.com/element-hq/element-x-android/pull/3318
* Fix broken direct room member for rooms with old users that left by @networkException in https://github.com/element-hq/element-x-android/pull/3324
* Add unit test on MatrixRoom extension by @bmarty in https://github.com/element-hq/element-x-android/pull/3327
* Fix login navigation getting stuck when the app was compiled with no-op analytics provider by @SpiritCroc in https://github.com/element-hq/element-x-android/pull/3337

Changes in Element X v0.5.0 (2024-07-24)
=========================================

### 🙌 Improvements
* Add icon for "Mark as read" and "Mark as unread" actions. by @bmarty in https://github.com/element-hq/element-x-android/pull/3144
* Add support for Picture In Picture for Element Call by @bmarty in https://github.com/element-hq/element-x-android/pull/3159
* Set pin grace period to 2 minutes by @bmarty in https://github.com/element-hq/element-x-android/pull/3172
* Unify the way we decide whether a room is a DM or a group room by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3100
* Subscribe to `RoomListItems` in the visible range by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3169
* Improve pip and add feature flag. by @bmarty in https://github.com/element-hq/element-x-android/pull/3199
* Open Source licenses: add color for links. by @bmarty in https://github.com/element-hq/element-x-android/pull/3215
* Cancel ringing call notification on call cancellation by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3047

### 🐛 Bugfixes
* Fix `MainActionButton` layout for long texts by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3158
* Always follow the desired theme for Pin, Incoming Call and Element Call screens by @bmarty in https://github.com/element-hq/element-x-android/pull/3165
* Fix empty screen issue after clearing the cache by @bmarty in https://github.com/element-hq/element-x-android/pull/3163
* Restore intentional mentions in the markdown/plain text editor by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3193
* Fix crash in the room list after a forced log out in background by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3180
* Clear existing notification when a room is marked as read by @bmarty in https://github.com/element-hq/element-x-android/pull/3203
* Fix crash when Pin code screen is displayed by @bmarty in https://github.com/element-hq/element-x-android/pull/3205
* Fix pillification not working for non formatted message bodies by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3201
* Update grammar on Matrix Ids to be more spec compliant and render error instead of infinite loading in room member list screen by @bmarty in https://github.com/element-hq/element-x-android/pull/3206
* Reduce the risk of text truncation in buttons. by @bmarty in https://github.com/element-hq/element-x-android/pull/3209
* Ensure that the manual dark theme is rendering correctly regarding -night resource and keyboard by @bmarty in https://github.com/element-hq/element-x-android/pull/3216
* Fix rendering issue of SunsetPage in dark mode by @bmarty in https://github.com/element-hq/element-x-android/pull/3217
* Fix linkification not working for `Spanned` strings in text messages by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3233
* Edit : fallback to room.edit when timeline item is not found. by @ganfra in https://github.com/element-hq/element-x-android/pull/3239

### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3156
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3192
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3232

### 🧱 Build
* Remove Showkase processor not found warning from Danger by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3148
* Set targetSDK to 34 by @bmarty in https://github.com/element-hq/element-x-android/pull/3149
* Add a local copy of `inplace-fix.py` and `fix-pg-map-id.py` by @bmarty in https://github.com/element-hq/element-x-android/pull/3167
* Only add private SSH keys and clone submodules in the original repo by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3225
* Fix CI for forks by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3226

### Dependency upgrades
* Update dependency io.element.android:compound-android to v0.0.7 by @renovate in https://github.com/element-hq/element-x-android/pull/3143
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.31 by @renovate in https://github.com/element-hq/element-x-android/pull/3145
* Update dependency com.squareup:kotlinpoet to v1.18.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3150
* Update dependency org.robolectric:robolectric to v4.13 by @renovate in https://github.com/element-hq/element-x-android/pull/3157
* Update plugin dependencycheck to v10.0.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3154
* Update wysiwyg to v2.37.5 by @renovate in https://github.com/element-hq/element-x-android/pull/3162
* Update plugin sonarqube to v5.1.0.4882 by @renovate in https://github.com/element-hq/element-x-android/pull/3139
* Update dependency org.jsoup:jsoup to v1.18.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3171
* Update dependency com.google.firebase:firebase-bom to v33.1.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3178
* Update telephoto to v0.12.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3191
* Update dependency com.google.truth:truth to v1.4.4 by @renovate in https://github.com/element-hq/element-x-android/pull/3187
* Update dependency com.squareup:kotlinpoet to v1.18.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3194
* Update dependency io.mockk:mockk to v1.13.12 by @renovate in https://github.com/element-hq/element-x-android/pull/3198
* Update dependency io.sentry:sentry-android to v7.12.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3200
* Update plugin dependencycheck to v10.0.3 by @renovate in https://github.com/element-hq/element-x-android/pull/3204
* Update dependency gradle to v8.9 by @renovate in https://github.com/element-hq/element-x-android/pull/3177
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.32 by @renovate in https://github.com/element-hq/element-x-android/pull/3202
* Update coil to v2.7.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3210
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.33 by @renovate in https://github.com/element-hq/element-x-android/pull/3220
* Update wysiwyg to v2.37.7 by @renovate in https://github.com/element-hq/element-x-android/pull/3218
* Update telephoto to v0.12.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3230
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.34 by @renovate in https://github.com/element-hq/element-x-android/pull/3237

### Others
* Reduce delay when selecting room list filters by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3160
* Add `--alignment-preserved true` when signing APK for F-Droid. by @bmarty in https://github.com/element-hq/element-x-android/pull/3161
* Ensure that all callback plugins are invoked. by @bmarty in https://github.com/element-hq/element-x-android/pull/3146
* Add generated screen to show open source licenses in Gplay variant by @bmarty in https://github.com/element-hq/element-x-android/pull/3207
* Performance : improve time to open a room. by @ganfra in https://github.com/element-hq/element-x-android/pull/3186
* Add logging to help debug forced logout issues by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3208
* Use the right filename for log files so they're sorted in rageshakes by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3219
* Compose : add immutability to some Reaction classes by @ganfra in https://github.com/element-hq/element-x-android/pull/3224
* Fix stickers display text on room summary by @surakin in https://github.com/element-hq/element-x-android/pull/3221
* Rework FakeMatrixRoom so that it contains only lambdas. by @bmarty in https://github.com/element-hq/element-x-android/pull/3229

Changes in Element X v0.4.16 (2024-07-05)
=========================================

### ✨ Features
* Avatar cluster for DM by @bmarty in https://github.com/element-hq/element-x-android/pull/3069
* Feature : Draft support by @ganfra in https://github.com/element-hq/element-x-android/pull/3099
* Timeline : re-enable edition of local echo by @ganfra in https://github.com/element-hq/element-x-android/pull/3126
* Draft : add volatile storage when moving to edit mode. by @ganfra in https://github.com/element-hq/element-x-android/pull/3132

### 🙌 Improvements
* Give locale and theme to Element Call by @bmarty in https://github.com/element-hq/element-x-android/pull/3118
* Let the SDK retrieve and parse Element well known content by @bmarty in https://github.com/element-hq/element-x-android/pull/3127

### 🐛 Bugfixes
* Let role and permissions screens works for invited room members too. by @bmarty in https://github.com/element-hq/element-x-android/pull/3081
* Fix image rendering after clear cache by @bmarty in https://github.com/element-hq/element-x-android/pull/3082
* Replace the 'answer' PendingIntent in ringing call notifications by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3093
* Use IO dispatcher for cleanup in bug reporter by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3092
* Fix `@room` mentions crashing in debug builds by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3107
* Auth : fix restore session when there is no network. by @ganfra in https://github.com/element-hq/element-x-android/pull/3109
* Alert for incoming call even if notifications are disabled - WAITING FOR FINAL PRODUCT DECISION by @bmarty in https://github.com/element-hq/element-x-android/pull/3053
* Fix incorrect 'device verified' screen when app was opened with no network connection by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3110
* Draft : also clear draft when composer is blank by @ganfra in https://github.com/element-hq/element-x-android/pull/3115
* Timeline : fix text item not refreshed when content change by @ganfra in https://github.com/element-hq/element-x-android/pull/3123
* FFs can now be toggled in release builds too by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3101
* Fix crash when getting the system ringtone for ringing calls by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3131
* Bugfix : avoid potential NPE on verification service. by @ganfra in https://github.com/element-hq/element-x-android/pull/3140

### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3114
* Sync Strings - Add Greek translations by @ElementBot in https://github.com/element-hq/element-x-android/pull/3133

### 🧱 Build
* Let GitHub generates the release notes by @bmarty in https://github.com/element-hq/element-x-android/pull/3105
* Fix F-Droid reproducible build. by @bmarty in https://github.com/element-hq/element-x-android/pull/3106
* Element enterprise (EE) foundations by @bmarty in https://github.com/element-hq/element-x-android/pull/3025
* Fix Element Enterprise nightly build and publication using App Distribution by @bmarty in https://github.com/element-hq/element-x-android/pull/3130
* Improve screenshot testing with ComposablePreviewScanner by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3125

### Dependency upgrades
* Update dependency com.posthog:posthog-android to v3.4.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3060
* Update danger/danger-js action to v12.3.3 by @renovate in https://github.com/element-hq/element-x-android/pull/3059
* Update dependency com.freeletics.flowredux:compose to v1.2.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3067
* Update dependency com.google.firebase:firebase-bom to v33.1.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3062
* Update dependency androidx.test.ext:junit to v1.2.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3088
* Update test.core to v1.6.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3090
* Remove dependencies androidx.test.espresso:espresso-core and androidx.appcompat:appcompat by @renovate in https://github.com/element-hq/element-x-android/pull/3087
* Update wysiwyg to v2.37.4 by @renovate in https://github.com/element-hq/element-x-android/pull/3094
* Update dependency androidx.test:runner to v1.6.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3089
* Update test.core to v1.6.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3104
* Update dependency androidx.test:runner to v1.6.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3103
* Update dependency androidx.test.ext:junit to v1.2.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3102
* Update dependency com.google.truth:truth to v1.4.3 by @renovate in https://github.com/element-hq/element-x-android/pull/3108
* Update dependency com.posthog:posthog-android to v3.4.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3111
* Update dependency io.nlopez.compose.rules:detekt to v0.4.5 by @renovate in https://github.com/element-hq/element-x-android/pull/3116
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.29 by @renovate in https://github.com/element-hq/element-x-android/pull/3119
* Update plugin dependencycheck to v10 by @renovate in https://github.com/element-hq/element-x-android/pull/3128
* Update plugin dependencycheck to v10.0.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3129
* Update dependency io.sentry:sentry-android to v7.11.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3122
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.30 by @renovate in https://github.com/element-hq/element-x-android/pull/3138

### Others
* Feature/fga/sending queue iteration by @ganfra in https://github.com/element-hq/element-x-android/pull/3054
* Use full date format for day dividers in timeline by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3057
* Let Dms use other member color. by @bmarty in https://github.com/element-hq/element-x-android/pull/3058
* Resolve display names in mentions in real time by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3051
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3077
* Improve the way we cut the bubble layout to give space for the sender Avatar by @bmarty in https://github.com/element-hq/element-x-android/pull/3080
* Upgrade build tools and fix `pg-map-id` for F-Droid by @bmarty in https://github.com/element-hq/element-x-android/pull/3084
* Improve room filtering behavior. by @bmarty in https://github.com/element-hq/element-x-android/pull/3083
* Adapt our code to the new authentication APIs in the Rust SDK by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3068
* Add temporary icon for Element Enterprise by @bmarty in https://github.com/element-hq/element-x-android/pull/3134
* Improve click behavior on room timeline title by @bmarty in https://github.com/element-hq/element-x-android/pull/3064

Changes in Element X v0.4.15 (2024-06-19)
=========================================

Features ✨
----------
 - Ringing call notifications and full screen ringing screen for DMs when the device is locked. ([#2894](https://github.com/element-hq/element-x-android/issues/2894))

Bugfixes 🐛
----------
 - Improve UX on notification setting changes. ([#1647](https://github.com/element-hq/element-x-android/issues/1647))
 - Fix tracing configuration in debug and nightlies:
 - Debug will now write the logs to disk too.
 - Nightly will be able to customise tracing filters.
 - Improved the configure tracing and bug report screens. ([#3016](https://github.com/element-hq/element-x-android/issues/3016))

Other changes
-------------
 - Allow cancelling jump to event in timeline. ([#2876](https://github.com/element-hq/element-x-android/issues/2876))
 - Make Element Call widget URL configurable ([#3009](https://github.com/element-hq/element-x-android/issues/3009))
 - Enable hidden access to developer options in release mode apps. ([#3020](https://github.com/element-hq/element-x-android/issues/3020))
 - Improve how active calls work by also taking into account external url calls and waiting for the sync process to start before sending the `m.call.notify` event. ([#3029](https://github.com/element-hq/element-x-android/issues/3029))


Changes in Element X v0.4.14 (2024-06-07)
=========================================

Features ✨
----------
 - Add support for incoming share (text or files) from other apps ([#1980](https://github.com/element-hq/element-x-android/issues/1980))

Bugfixes 🐛
----------
 - Render selected/deselected room list filters on top ([#2809](https://github.com/element-hq/element-x-android/issues/2809))
 - Set auto captilization, multiline and autocompletion flags for the markdown EditText. ([#2896](https://github.com/element-hq/element-x-android/issues/2896))
 - Restore Markdown text input contents when returning to the room screen. ([#2898](https://github.com/element-hq/element-x-android/issues/2898))
 - Fixed sending rich content from android keyboards on the markdown text input ([#2917](https://github.com/element-hq/element-x-android/issues/2917))
 - Fix crash when restoring the selection values in the plain text editor. ([#2959](https://github.com/element-hq/element-x-android/issues/2959))

Other changes
-------------
 - BugReporting | Add public device keys to rageshakes ([#2893](https://github.com/element-hq/element-x-android/issues/2893))
 - Move push provider setting to the "Notifications" screen and display it only when several push provider are available. ([#2912](https://github.com/element-hq/element-x-android/issues/2912))
 - Simplify notifications by removing the custom persistence layer.
 - Bump minSdk to 24 (Android 7). ([#2924](https://github.com/element-hq/element-x-android/issues/2924))
 - Add a feature flag ShowBlockedUsersDetails, disabled by default to render display name and avatar of blocked users in the blocked users list. ([#2930](https://github.com/element-hq/element-x-android/issues/2930))
 - Be more specific with the widget permissions ([#2932](https://github.com/element-hq/element-x-android/issues/2932))
 - Analytics | Add support for SuperProperties ([#2953](https://github.com/element-hq/element-x-android/issues/2953))
 - Track when the user starts a room call and when they enable formatting options on the message composer ([#2969](https://github.com/element-hq/element-x-android/issues/2969))


Changes in Element X v0.4.13 (2024-05-22)
=========================================

Features ✨
----------
 - Add plain text editor based on Markdown input. ([#2840](https://github.com/element-hq/element-x-android/issues/2840))

Bugfixes 🐛
----------
 - Use members display names for their membership state events. ([#2286](https://github.com/element-hq/element-x-android/issues/2286))
 - Make sure explicit links in messages take priority over links found by linkification (urls, emails, phone numbers, etc.) ([#2291](https://github.com/element-hq/element-x-android/issues/2291))
 - Fix modal contents overlapping screen lock pin. ([#2692](https://github.com/element-hq/element-x-android/issues/2692))
 - Fix a crash when trying to create an `EncryptedFile` in Android 6. ([#2846](https://github.com/element-hq/element-x-android/issues/2846))
 - Session falsely displayed as 'verified' with no internet connection. ([#2884](https://github.com/element-hq/element-x-android/issues/2884))

Other changes
-------------
 - Allow configuring push notification provider ([#2340](https://github.com/element-hq/element-x-android/issues/2340))
 - UX cleanup: reorder text composer actions to prioritise camera ones. ([#2803](https://github.com/element-hq/element-x-android/issues/2803))
 - Translation added into Portuguese and Simplified Chinese ([#2834](https://github.com/element-hq/element-x-android/issues/2834))
 - Use via parameters when joining a room from permalink. ([#2843](https://github.com/element-hq/element-x-android/issues/2843))


Changes in Element X v0.4.12 (2024-05-13)
=========================================

Features ✨
----------
- Add support for expected decryption errors due to membership (UX and analytics). ([#2754](https://github.com/element-hq/element-x-android/issues/2754))
- Handle permalink navigation to Events. ([#2759](https://github.com/element-hq/element-x-android/issues/2759))
- Pretty-print event JSON in debug viewer ([#2771](https://github.com/element-hq/element-x-android/issues/2771))
- Add support for external permalinks. ([#2776](https://github.com/element-hq/element-x-android/issues/2776))
- Enable support for Android per-app language preferences ([#2795](https://github.com/element-hq/element-x-android/issues/2795))

Bugfixes 🐛
----------
- Fix session verification being asked again for already verified users. ([#2718](https://github.com/element-hq/element-x-android/issues/2718))
- Instead of displaying 'create new recovery key' on the session verification screen when there is no other session active, display it always under the 'enter recovery key' screen. ([#2740](https://github.com/element-hq/element-x-android/issues/2740))
- Adjust the typography used in the selected user component so a user's display name fits better. ([#2760](https://github.com/element-hq/element-x-android/issues/2760))
- User display name overflows in timeline messages when it's way too long. ([#2761](https://github.com/element-hq/element-x-android/issues/2761))
- Ensure the application open the room when a notification is clicked. ([#2778](https://github.com/element-hq/element-x-android/issues/2778))
- Enforce mandatory session verification only for new logins. ([#2810](https://github.com/element-hq/element-x-android/issues/2810))
- Make log less verbose, make sure we upload as many log files as possible before reaching the request size limit of the bug reporting service, discard older logs if they don't fit. ([#2825](https://github.com/element-hq/element-x-android/issues/2825))
- Remove 'Join' button in room directory search results. ([#2827](https://github.com/element-hq/element-x-android/issues/2827))
- Add missing `app_id` and `Version` properties to bug reports. ([#2829](https://github.com/element-hq/element-x-android/issues/2829))

Other changes
-------------
- RoomMember screen: fallback to userProfile data, if the member is not a user of the room. ([#2721](https://github.com/element-hq/element-x-android/issues/2721))
- Migrate application data. ([#2749](https://github.com/element-hq/element-x-android/issues/2749))
- Let the SDK manage the file log cleanup, and keep one week of log. ([#2758](https://github.com/element-hq/element-x-android/issues/2758))
- UX cleanup: reorder options in the main settings screen. ([#2801](https://github.com/element-hq/element-x-android/issues/2801))
- Analytics: Add support to report current session verification and recovery state ([#2806](https://github.com/element-hq/element-x-android/issues/2806))
- UX cleanup: room details screen, add new CTA buttons for Invite and Call actions. ([#2814](https://github.com/element-hq/element-x-android/issues/2814))
- UX cleanup: user profile. Move send DM to a call to action button, add 'Call' CTA too. ([#2818](https://github.com/element-hq/element-x-android/issues/2818))
- Add room badges to room details screen. ([#2822](https://github.com/element-hq/element-x-android/issues/2822))

Security
-------------
- Bump the Rust SDK to `v0.2.18` to remediate [CVE-2024-34353 / GHSA-9ggc-845v-gcgv](https://github.com/matrix-org/matrix-rust-sdk/security/advisories/GHSA-9ggc-845v-gcgv).

Changes in Element X v0.4.10 (2024-04-17)
=========================================

Matrix Rust SDK 0.2.14

Features ✨
----------
- Rework room navigation to handle unknown room and prepare work on permalink. ([#2695](https://github.com/element-hq/element-x-android/issues/2695))

Other changes
-------------
- Encrypt new session data with a passphrase ([#2703](https://github.com/element-hq/element-x-android/issues/2703))
- Use sdk API to build permalinks ([#2708](https://github.com/element-hq/element-x-android/issues/2708))
- Parse permalink using parseMatrixEntityFrom from the SDK ([#2709](https://github.com/element-hq/element-x-android/issues/2709))
- Fix compile for forks that use the `noop` analytics module ([#2698](https://github.com/element-hq/element-x-android/issues/2698))


Changes in Element X v0.4.9 (2024-04-12)
========================================

- Synchronize Localazy Strings.

Security
----------
- Fix crash while processing a room message containing a malformed pill.

Changes in Element X v0.4.8 (2024-04-10)
========================================

Features ✨
----------
- Move session recovery to the login flow. ([#2579](https://github.com/element-hq/element-x-android/issues/2579))
- Move session verification to the after login flow and make it mandatory. ([#2580](https://github.com/element-hq/element-x-android/issues/2580))
- Add a notification troubleshoot screen ([#2601](https://github.com/element-hq/element-x-android/issues/2601))
- Add action to copy permalink ([#2650](https://github.com/element-hq/element-x-android/issues/2650))

Bugfixes 🐛
----------
- Fix analytics issue around room considered as space by mistake. ([#2612](https://github.com/element-hq/element-x-android/issues/2612))
- Fix crash observed when going back to the room list. ([#2619](https://github.com/element-hq/element-x-android/issues/2619))
- Hide Event org.matrix.msc3401.call.member on the timeline. ([#2625](https://github.com/element-hq/element-x-android/issues/2625))
- Fall back to name-based generated avatars when image avatars don't load. ([#2667](https://github.com/element-hq/element-x-android/issues/2667))

Other changes
-------------
- Improve UI for notification permission screen in onboarding. ([#2581](https://github.com/element-hq/element-x-android/issues/2581))
- Categorise members by role in change roles screen. ([#2593](https://github.com/element-hq/element-x-android/issues/2593))
- Make completed poll more clearly visible ([#2608](https://github.com/element-hq/element-x-android/issues/2608))
- Show users from last visited DM as suggestion when starting a Chat or when creating a Room. ([#2634](https://github.com/element-hq/element-x-android/issues/2634))
- Enable room moderation feature. ([#2678](https://github.com/element-hq/element-x-android/issues/2678))
- Improve analytics opt-in screen UI. ([#2684](https://github.com/element-hq/element-x-android/issues/2684))


Changes in Element X v0.4.7 (2024-03-26)
========================================

Features ✨
----------
- Enable the feature "RoomList filters". ([#2603](https://github.com/element-hq/element-x-android/issues/2603))
- Enable the feature "Mark as unread" ([#2261](https://github.com/element-hq/element-x-android/issues/2261))
- Implement MSC2530 (Body field as media caption) ([#2521](https://github.com/element-hq/element-x-android/issues/2521))

Bugfixes 🐛
----------
- Use user avatar from cache if available. ([#2488](https://github.com/element-hq/element-x-android/issues/2488))
- Update member list after changing member roles and when the room member list is opened. ([#2590](https://github.com/element-hq/element-x-android/issues/2590))

Other changes
-------------
- Compound: add `BigIcon`, `BigCheckmark` and `PageTitle` components. ([#2574](https://github.com/element-hq/element-x-android/issues/2574))
- Remove Welcome screen from the FTUE. ([#2584](https://github.com/element-hq/element-x-android/issues/2584))


Changes in Element X v0.4.6 (2024-03-15)
========================================

Features ✨
----------
- Admins can now change user roles in rooms. ([#2257](https://github.com/element-hq/element-x-android/issues/2257))
- Room member moderation: remove, ban and unban users from a room. ([#2258](https://github.com/element-hq/element-x-android/issues/2258))
- Change a room's permissions power levels. ([#2259](https://github.com/element-hq/element-x-android/issues/2259))
- Add state timeline events and notifications for legacy call invites. ([#2485](https://github.com/element-hq/element-x-android/issues/2485))

Bugfixes 🐛
----------
- Added empty state to banned member list. ([#+add-empty-state-to-banned-members-list](https://github.com/element-hq/element-x-android/issues/+add-empty-state-to-banned-members-list))
- Prevent sending empty messages. ([#995](https://github.com/element-hq/element-x-android/issues/995))
- Use the display name only once in display name change events. The user should be referenced by `userId` instead. ([#2125](https://github.com/element-hq/element-x-android/issues/2125))
- Hide blocked users list when there are no blocked users. ([#2198](https://github.com/element-hq/element-x-android/issues/2198))
- Fix timeline not showing sender info when room is marked as direct but not a 1:1 room. ([#2530](https://github.com/element-hq/element-x-android/issues/2530))

Other changes
-------------
- Add `local_time`, `utc_time` and `sdk_sha` params to bug reports so they're easier to investigate. ([#+add-time-and-sdk-sha-params-to-bugreports](https://github.com/element-hq/element-x-android/issues/+add-time-and-sdk-sha-params-to-bugreports))
- Improve room member list loading times, increase chunk size ([#2322](https://github.com/element-hq/element-x-android/issues/2322))
- Improve room member list loading UX. ([#2452](https://github.com/element-hq/element-x-android/issues/2452))
- Remove the special log level for the Rust SDK read receipts. ([#2511](https://github.com/element-hq/element-x-android/issues/2511))
- Track UTD errors. ([#2544](https://github.com/element-hq/element-x-android/issues/2544))


Changes in Element X v0.4.5 (2024-02-28)
========================================

Features ✨
----------
- Mark a room or dm as favourite. ([#2208](https://github.com/element-hq/element-x-android/issues/2208))
- Add moderation to rooms:
    - Sort member in room member list by powerlevel, display their roles.
    - Display banner users in room member list for users with enough power level to ban/unban. ([#2256](https://github.com/element-hq/element-x-android/issues/2256))
- MediaViewer : introduce fullscreen and flick to dismiss behavior. ([#2390](https://github.com/element-hq/element-x-android/issues/2390))
- Allow user-installed certificates to be used by the HTTP client ([#2992](https://github.com/element-hq/element-x-android/issues/2992))

Bugfixes 🐛
----------
- Do not display empty room list state before the loading one when we still don't have any items ([#+do-not-display-empty-state-before-loading-roomlist](https://github.com/element-hq/element-x-android/issues/+do-not-display-empty-state-before-loading-roomlist))
- Improve how Talkback works with the timeline. Sadly, it's still not 100% working, but there is some issue with the `LazyColumn` using `reverseLayout` that only Google can fix. ([#+improve-accessibility-in-timeline](https://github.com/element-hq/element-x-android/issues/+improve-accessibility-in-timeline))
- Add ability to enter a recovery key to verify the session. Also fixes some refresh issues with the verification session state. ([#2421](https://github.com/element-hq/element-x-android/issues/2421))

Other changes
-------------
- Provide the current system proxy setting to the Rust SDK. ([#2420](https://github.com/element-hq/element-x-android/issues/2420))


Changes in Element X v0.4.4 (2024-02-15)
========================================

Bugfixes 🐛
----------

- Fix decryption of previous messages after session verification not working.

Changes in Element X v0.4.3 (2024-02-14)
========================================

Features ✨
----------
- Change "Read receipts" advanced setting used to send private Read Receipt to "Share presence" settings. When disabled, private Read Receipts will be sent, and no typing notification will be sent. Also Read Receipts and typing notifications will not be rendered in the timeline. ([#2241](https://github.com/element-hq/element-x-android/issues/2241))
- Render typing notifications. ([#2242](https://github.com/element-hq/element-x-android/issues/2242))
- Manually mark a room as unread. ([#2261](https://github.com/element-hq/element-x-android/issues/2261))
- Add empty state to the room list. ([#2330](https://github.com/element-hq/element-x-android/issues/2330))
- Allow joining unencrypted video calls in non encrypted rooms. ([#2333](https://github.com/element-hq/element-x-android/issues/2333))

Bugfixes 🐛
----------
- Fix crash after unregistering UnifiedPush distributor ([#2304](https://github.com/element-hq/element-x-android/issues/2304))
- Add missing device id to settings screen. ([#2316](https://github.com/element-hq/element-x-android/issues/2316))
- Open the keyboard (and keep it opened) when creating a poll. ([#2329](https://github.com/element-hq/element-x-android/issues/2329))
- Fix message forwarding after SDK API change related to Timeline intitialization.

Other changes
-------------
- Adjusted the login flow buttons so the continue button is always at the same height ([#825](https://github.com/element-hq/element-x-android/issues/825))
- Move migration screen to within the room list ([#2310](https://github.com/element-hq/element-x-android/issues/2310))
- Render correctly in reply to data when Event cannot be decrypted or has been redacted ([#2318](https://github.com/element-hq/element-x-android/issues/2318))
- Remove Compose Foundation version pinning workaround. This was done to avoid a bug introduced in the default foundation version used by the material3 library, but that has already been fixed.
- Remove `FilterHiddenStateEventsProcessor`, as this is already handled by the Rust SDK.
- Remove session preferences on user log out.

Breaking changes 🚨
-------------------
- Update Compound icons in the project. Since the icon prefix changed to `ic_compound_` and the `CompoundIcons` helper now contains the vector icons as composable functions.

Changes in Element X v0.4.2 (2024-01-31)
========================================

Matrix SDK 🦀 v0.1.95

Features ✨
----------
- Add 'send private read receipts' option in advanced settings ([#2204](https://github.com/element-hq/element-x-android/issues/2204))
- Send typing notification ([#2240](https://github.com/element-hq/element-x-android/issues/2240)). Disabling the sending of typing notification and rendering typing notification will come soon.

Bugfixes 🐛
----------
- Make the room settings screen update automatically when new room info (name, avatar, topic) is available. ([#921](https://github.com/element-hq/element-x-android/issues/921))
- Update timeline items' read receipts when the room members info is loaded. ([#2176](https://github.com/element-hq/element-x-android/issues/2176))
- Edited text message bubbles should resize when edited ([#2260](https://github.com/element-hq/element-x-android/issues/2260))
- Ensure login and password exclude `\n` ([#2263](https://github.com/element-hq/element-x-android/issues/2263))
- Room list Ensure the indicators stay grey if the global setting is set to mention only and a regular message is received. ([#2282](https://github.com/element-hq/element-x-android/issues/2282))

Other changes
-------------
- Add a special logging configuration for nightlies so we can get more detailed info for existing issues. ([#+add-special-tracing-configuration-for-nightlies](https://github.com/element-hq/element-x-android/issues/+add-special-tracing-configuration-for-nightlies))
- Try mitigating unexpected logouts by making getting/storing session data use a Mutex for synchronization.
  Also added some more logs so we can understand exactly where it's failing. ([#+try-mitigating-unexpected-logouts](https://github.com/element-hq/element-x-android/issues/+try-mitigating-unexpected-logouts))
- Upgrade Material3 Compose to `1.2.0-beta02`.
  There is also a constraint on a transitive Compose Foundation dependency version (1.6.0-beta02) that fixes the timeline scrolling issue. ([#0-beta02](https://github.com/element-hq/element-x-android/issues/0-beta02))
- Disambiguate display name in the timeline. ([#2215](https://github.com/element-hq/element-x-android/issues/2215))
- Disambiguate display name in notifications ([#2224](https://github.com/element-hq/element-x-android/issues/2224))
- Remove room creation, self-join of room creator and 'this is the beginning of X' timeline items for DMs. ([#2217](https://github.com/element-hq/element-x-android/issues/2217))
- Encrypt databases used by the Rust SDK on Nightly and Debug builds. ([#2219](https://github.com/element-hq/element-x-android/issues/2219))
- Fallback to UnifiedPush (if available) if the PlayServices are not installed on the device. ([#2248](https://github.com/element-hq/element-x-android/issues/2248))
- Add "Report a problem" button to the onboarding screen ([#2275](https://github.com/element-hq/element-x-android/issues/2275))
- Add in app logs viewer to the "Report a problem" screen. ([#2276](https://github.com/element-hq/element-x-android/issues/2276))


Changes in Element X v0.4.1 (2024-01-17)
========================================

Features ✨
----------
- Render m.sticker events ([#1949](https://github.com/element-hq/element-x-android/issues/1949))
- Add support for sending images from the keyboard ([#1977](https://github.com/element-hq/element-x-android/issues/1977))
- Added support for MSC4027 (render custom images in reactions) ([#2159](https://github.com/element-hq/element-x-android/issues/2159))

Bugfixes 🐛
----------
- Fix crash sending image with latest Posthog because of an usage of an internal Android method. ([#+crash-sending-image-with-latest-posthog](https://github.com/element-hq/element-x-android/issues/+crash-sending-image-with-latest-posthog))
- Make sure the media viewer tries the main url first (if not empty) then the thumbnail url and then not open if both are missing instead of failing with an error dialog ([#1949](https://github.com/element-hq/element-x-android/issues/1949))
- Fix room transition animation happens twice. ([#2084](https://github.com/element-hq/element-x-android/issues/2084))
- Disable ability to send reaction if the user does not have the permission to. ([#2093](https://github.com/element-hq/element-x-android/issues/2093))
- Trim whitespace at the end of messages to ensure we render the right content. ([#2099](https://github.com/element-hq/element-x-android/issues/2099))
- Fix crashes in room list when the last message for a room was an extremely long one (several thousands of characters) with no line breaks. ([#2105](https://github.com/element-hq/element-x-android/issues/2105))
- Disable rasterisation of Vector XMLs, which was causing crashes on API 23. ([#2124](https://github.com/element-hq/element-x-android/issues/2124))
- Use `SubomposeLayout` for `ContentAvoidingLayout` to prevent wrong measurements in the layout process, leading to cut-off text messages in the timeline. ([#2155](https://github.com/element-hq/element-x-android/issues/2155))
- Improve rendering of voice messages in the timeline in large displays ([#2156](https://github.com/element-hq/element-x-android/issues/2156))
- Fix no indication that user list is loading when inviting to room. ([#2172](https://github.com/element-hq/element-x-android/issues/2172))
- Hide keyboard when tapping on a message in the timeline. ([#2182](https://github.com/element-hq/element-x-android/issues/2182))
- Mention selector gets stuck when quickly deleting the prompt. ([#2192](https://github.com/element-hq/element-x-android/issues/2192))
- Hide verbose state events from the timeline ([#2216](https://github.com/element-hq/element-x-android/issues/2216))

Other changes
-------------
- Only apply `com.autonomousapps.dependency-analysis` plugin in those modules that need it. ([#+only-apply-dependency-analysis-plugin-where-needed](https://github.com/element-hq/element-x-android/issues/+only-apply-dependency-analysis-plugin-where-needed))
- Migrate to Kover 0.7.X ([#1782](https://github.com/element-hq/element-x-android/issues/1782))
- Remove extra logout screen. ([#2072](https://github.com/element-hq/element-x-android/issues/2072))
- Handle `MembershipChange.NONE` rendering in the timeline. ([#2102](https://github.com/element-hq/element-x-android/issues/2102))
- Remove extra previews for timestamp view with 'document' case ([#2127](https://github.com/element-hq/element-x-android/issues/2127))
- Bump AGP version to 8.2.0 ([#2142](https://github.com/element-hq/element-x-android/issues/2142))
- Replace 'leave room' text with 'leave conversation' for DMs. ([#2218](https://github.com/element-hq/element-x-android/issues/2218))


Changes in Element X v0.4.0 (2023-12-22)
========================================

Features ✨
----------
- Use the RTE library `TextView` to render text events in the timeline. Add support for mention pills - with no interaction yet. ([#1433](https://github.com/element-hq/element-x-android/issues/1433))
- Tapping on a user mention pill opens their profile. ([#1448](https://github.com/element-hq/element-x-android/issues/1448))
- Display different notifications for mentions. ([#1451](https://github.com/element-hq/element-x-android/issues/1451))
- Reply to a poll ([#1848](https://github.com/element-hq/element-x-android/issues/1848))
- Add plain text representation of messages ([#1850](https://github.com/element-hq/element-x-android/issues/1850))
- Allow polls to be edited when they have not been voted on ([#1869](https://github.com/element-hq/element-x-android/issues/1869))
- Scroll to end of timeline when sending a new message. ([#1877](https://github.com/element-hq/element-x-android/issues/1877))
- Confirm back navigation when editing a poll only if the poll was changed ([#1886](https://github.com/element-hq/element-x-android/issues/1886))
- Add option to delete a poll while editing the poll ([#1895](https://github.com/element-hq/element-x-android/issues/1895))
- Open room member avatar when you click on it inside the member details screen. ([#1907](https://github.com/element-hq/element-x-android/issues/1907))
- Poll history of a room is now accessible from the room details screen. ([#2014](https://github.com/element-hq/element-x-android/issues/2014))
- Always close the invite list screen when there is no more invite. ([#2022](https://github.com/element-hq/element-x-android/issues/2022))

Bugfixes 🐛
----------
- Fix see room in the room list after leaving it. ([#1006](https://github.com/element-hq/element-x-android/issues/1006))
- Adjust mention pills font weight and horizontal padding ([#1449](https://github.com/element-hq/element-x-android/issues/1449))
- Font size in 'All Chats' header was changing mid-animation. ([#1572](https://github.com/element-hq/element-x-android/issues/1572))
- Accessibility: do not read initial used for avatar out loud. ([#1864](https://github.com/element-hq/element-x-android/issues/1864))
- Use the right avatar for DMs in DM rooms ([#1912](https://github.com/element-hq/element-x-android/issues/1912))
- Fix scaling of timeline images: don't crop, don't set min/max aspect ratio values. ([#1940](https://github.com/element-hq/element-x-android/issues/1940))
- Fix rendering of user name with vertical text by clipping the text. ([#1950](https://github.com/element-hq/element-x-android/issues/1950))
- Do not render `roomId` if the room has no canonical alias. ([#1970](https://github.com/element-hq/element-x-android/issues/1970))
- Fix avatar not displayed in notification when the app is not in background ([#1991](https://github.com/element-hq/element-x-android/issues/1991))
- Fix wording in room invite members view: `Send` -> `Invite`. ([#2037](https://github.com/element-hq/element-x-android/issues/2037))
- Timestamp positioning was broken, specially for edited messages. ([#2060](https://github.com/element-hq/element-x-android/issues/2060))
- Emojis in custom reaction bottom sheet are too tiny. ([#2066](https://github.com/element-hq/element-x-android/issues/2066))
- Set a default power level to join calls. Also, create new rooms taking this power level into account.

Other changes
-------------
- Add a warning for 'mentions and keywords only' notification option if your homeserver does not support it ([#1749](https://github.com/element-hq/element-x-android/issues/1749))
- Remove `:libraries:theme` module, extract theme and tokens to [Compound Android](https://github.com/element-hq/compound-android). ([#1833](https://github.com/element-hq/element-x-android/issues/1833))
- Update poll icons from Compound ([#1849](https://github.com/element-hq/element-x-android/issues/1849))
- Add ability to see the room avatar in the media viewer. ([#1918](https://github.com/element-hq/element-x-android/issues/1918))
- RoomList: introduce incremental loading to improve performances. ([#1920](https://github.com/element-hq/element-x-android/issues/1920))
- Add toggle in the notification settings to disable notifications for room invites. ([#1944](https://github.com/element-hq/element-x-android/issues/1944))
- Update rendering of Emojis displayed during verification. ([#1965](https://github.com/element-hq/element-x-android/issues/1965))
- Hide sender info in direct rooms ([#1979](https://github.com/element-hq/element-x-android/issues/1979))
- Render images in Notification ([#1991](https://github.com/element-hq/element-x-android/issues/1991))
- Only process content.json from Localazy. ([#2031](https://github.com/element-hq/element-x-android/issues/2031))
- Always show user avatar in message action sheet ([#2032](https://github.com/element-hq/element-x-android/issues/2032))
- Hide room list dropdown menu. ([#2062](https://github.com/element-hq/element-x-android/issues/2062))
- Enable Chat backup, Mentions and Read Receipt in release. ([#2087](https://github.com/element-hq/element-x-android/issues/2087))
- Make most code used in Compose from `:libraries:matrix` and derived classes Immutable or Stable.

Changes in Element X v0.3.2 (2023-11-22)
========================================

Features ✨
----------
- Add ongoing call indicator to rooms lists items. ([#1158](https://github.com/element-hq/element-x-android/issues/1158))
- Add support for typing mentions in the message composer. ([#1453](https://github.com/element-hq/element-x-android/issues/1453))
- Add intentional mentions to messages. This needs to be enabled in developer options since it's disabled by default. ([#1591](https://github.com/element-hq/element-x-android/issues/1591))
- Update voice message recording behaviour. Instead of holding the record button, users can now tap the record button to start recording and tap again to stop recording. ([#1784](https://github.com/element-hq/element-x-android/issues/1784))

Bugfixes 🐛
----------
- Always ensure media temp dir exists ([#1790](https://github.com/element-hq/element-x-android/issues/1790))

Other changes
-------------
- Update icons and move away from `PreferenceText` components. ([#1718](https://github.com/element-hq/element-x-android/issues/1718))
- Add item "This is the beginning of..." at the beginning of the timeline. ([#1801](https://github.com/element-hq/element-x-android/issues/1801))
- LockScreen : rework LoggedInFlowNode and back management when locked. ([#1806](https://github.com/element-hq/element-x-android/issues/1806))
- Suppress usage of removeTimeline method. ([#1824](https://github.com/element-hq/element-x-android/issues/1824))
- Remove Element Call feature flag, it's now always enabled.
- Reverted the EC base URL to `https://call.element.io`.
- Moved the option to override this URL to developer settings from advanced settings.


Changes in Element X v0.3.1 (2023-11-09)
========================================

Features ✨
----------
- Chat backup is still under a feature flag, but when enabled, user can enter their recovery key (it's also possible to input a passphrase) to unlock the encrypted room history. ([#1770](https://github.com/element-hq/element-x-android/pull/1770))

Bugfixes 🐛
----------
- Improve confusing text in the 'ready to start verification' screen. ([#879](https://github.com/element-hq/element-x-android/issues/879))
- Message composer wasn't resized when selecting a several lines message to reply to, then a single line one. ([#1560](https://github.com/element-hq/element-x-android/issues/1560))

Other changes
-------------
- PIN: Set lock grace period to 0. ([#1732](https://github.com/element-hq/element-x-android/issues/1732))


Changes in Element X v0.3.0 (2023-10-31)
========================================

Features ✨
----------
- Element Call: change the 'join call' button in a chat room when there's an active call. ([#1158](https://github.com/element-hq/element-x-android/issues/1158))
- Mentions: add mentions suggestion view in RTE ([#1452](https://github.com/element-hq/element-x-android/issues/1452))
- Record and send voice messages ([#1596](https://github.com/element-hq/element-x-android/issues/1596))
- Enable voice messages for all users ([#1669](https://github.com/element-hq/element-x-android/issues/1669))
- Receive and play a voice message ([#2084](https://github.com/element-hq/element-x-android/issues/2084))
- Enable Element Call integration in rooms by default, fix several issues when creating or joining calls.

Bugfixes 🐛
----------
- Group fallback notification to avoid having plenty of them displayed. ([#994](https://github.com/element-hq/element-x-android/issues/994))
- Hide keyboard when exiting the chat room screen. ([#1375](https://github.com/element-hq/element-x-android/issues/1375))
- Always register the pusher when application starts ([#1481](https://github.com/element-hq/element-x-android/issues/1481))
- Ensure screen does not turn off when playing a video ([#1519](https://github.com/element-hq/element-x-android/issues/1519))
- Fix issue where text is cleared when cancelling a reply ([#1617](https://github.com/element-hq/element-x-android/issues/1617))

Other changes
-------------
- Remove usage of blocking methods. ([#1563](https://github.com/element-hq/element-x-android/issues/1563))


Changes in Element X v0.2.4 (2023-10-12)
========================================

Features ✨
----------
- [Rich text editor] Add full screen mode ([#1447](https://github.com/element-hq/element-x-android/issues/1447))
- Improve rendering of m.emote. ([#1497](https://github.com/element-hq/element-x-android/issues/1497))
- Improve deleted session behavior. ([#1520](https://github.com/element-hq/element-x-android/issues/1520))

Bugfixes 🐛
----------
- WebP images can't be sent as media. ([#1483](https://github.com/element-hq/element-x-android/issues/1483))
- Fix back button not working in bottom sheets. ([#1517](https://github.com/element-hq/element-x-android/issues/1517))
- Render body of unknown msgtype in the timeline and in the room list ([#1539](https://github.com/element-hq/element-x-android/issues/1539))

Other changes
-------------
- Room : makes subscribeToSync/unsubscribeFromSync suspendable. ([#1457](https://github.com/element-hq/element-x-android/issues/1457))
- Add some Konsist tests. ([#1526](https://github.com/element-hq/element-x-android/issues/1526))


Changes in Element X v0.2.3 (2023-09-27)
========================================

Features ✨
----------
- Handle installation of Apks from the media viewer. ([#1432](https://github.com/element-hq/element-x-android/pull/1432))
- Integrate SDK 0.1.58 ([#1437](https://github.com/element-hq/element-x-android/pull/1437))

Other changes
-------------
- Element call: add custom parameters to Element Call urls. ([#1434](https://github.com/element-hq/element-x-android/issues/1434))


Changes in Element X v0.2.2 (2023-09-21)
========================================

Bugfixes 🐛
----------
- Add animation when rendering the timeline to avoid glitches. ([#1323](https://github.com/element-hq/element-x-android/issues/1323))
- Fix crash when trying to take a photo or record a video. ([#1395](https://github.com/element-hq/element-x-android/issues/1395))


Changes in Element X v0.2.1 (2023-09-20)
========================================

Features ✨
----------
- Bump Rust SDK to `v0.1.56`
- [Rich text editor] Add link support to rich text editor ([#1309](https://github.com/element-hq/element-x-android/issues/1309))
- Let the SDK figure the best scheme given an homeserver URL (thus allowing HTTP homeservers) ([#1382](https://github.com/element-hq/element-x-android/issues/1382))

Bugfixes 🐛
----------
- Fix ANR on RoomList when notification settings change. ([#1370](https://github.com/element-hq/element-x-android/issues/1370))

Other changes
-------------
- Element Call: support scheme `io.element.call` ([#1377](https://github.com/element-hq/element-x-android/issues/1377))
- [DI] Rework how dagger components are created and provided. ([#1378](https://github.com/element-hq/element-x-android/issues/1378))
- Remove usage of async-uniffi as it leads to a deadlocks and memory leaks. ([#1381](https://github.com/element-hq/element-x-android/issues/1381))


Changes in Element X v0.2.0 (2023-09-18)
========================================

Features ✨
----------
- Bump Rust SDK to `v0.1.54`
- Add a "Mute" shortcut icon and a "Notifications" section in the room details screen ([#506](https://github.com/element-hq/element-x-android/issues/506))
- Add a notification permission screen to the initial flow. ([#897](https://github.com/element-hq/element-x-android/issues/897))
- Integrate Element Call into EX by embedding a call in a WebView. ([#1300](https://github.com/element-hq/element-x-android/issues/1300))
- Implement Bloom effect modifier. ([#1217](https://github.com/element-hq/element-x-android/issues/1217))
- Set color on display name and default avatar in the timeline. ([#1224](https://github.com/element-hq/element-x-android/issues/1224))
- Display a thread decorator in timeline so we know when a message is coming from a thread. ([#1236](https://github.com/element-hq/element-x-android/issues/1236))
- [Rich text editor] Integrate rich text editor library. Note that markdown is now not supported and further formatting support will be introduced through the rich text editor. ([#1172](https://github.com/element-hq/element-x-android/issues/1172))
- [Rich text editor] Add formatting menu (accessible via the '+' button) ([#1261](https://github.com/element-hq/element-x-android/issues/1261))
- [Rich text editor] Add feature flag for rich text editor. Markdown support can now be enabled by disabling the rich text editor. ([#1289](https://github.com/element-hq/element-x-android/issues/1289))
- [Rich text editor] Update design ([#1332](https://github.com/element-hq/element-x-android/issues/1332))

Bugfixes 🐛
----------
- Make links in room topic clickable ([#612](https://github.com/element-hq/element-x-android/issues/612))
- Reply action: harmonize conditions in bottom sheet and swipe to reply. ([#1173](https://github.com/element-hq/element-x-android/issues/1173))
- Fix system bar color after login on light theme. ([#1222](https://github.com/element-hq/element-x-android/issues/1222))
- Fix long click on simple formatted messages ([#1232](https://github.com/element-hq/element-x-android/issues/1232))
- Enable polls in release build. ([#1241](https://github.com/element-hq/element-x-android/issues/1241))
- Fix top padding in room list when app is opened in offline mode. ([#1297](https://github.com/element-hq/element-x-android/issues/1297))
- [Rich text editor] Fix 'text formatting' option only partially visible ([#1335](https://github.com/element-hq/element-x-android/issues/1335))
- [Rich text editor] Ensure keyboard opens for reply and text formatting modes ([#1337](https://github.com/element-hq/element-x-android/issues/1337))
- [Rich text editor] Fix placeholder spilling onto multiple lines ([#1347](https://github.com/element-hq/element-x-android/issues/1347))

Other changes
-------------
- Add a sub-screen "Notifications" in the existing application Settings ([#510](https://github.com/element-hq/element-x-android/issues/510))
- Exclude some groups related to analytics to be included. ([#1191](https://github.com/element-hq/element-x-android/issues/1191))
- Use the new SyncIndicator API. ([#1244](https://github.com/element-hq/element-x-android/issues/1244))
- Improve RoomSummary mapping by using RoomInfo. ([#1251](https://github.com/element-hq/element-x-android/issues/1251))
- Ensure Posthog data are sent to "https://posthog.element.io" ([#1269](https://github.com/element-hq/element-x-android/issues/1269))
- New app icon, with monochrome support. ([#1363](https://github.com/element-hq/element-x-android/issues/1363))


Changes in Element X v0.1.6 (2023-09-04)
========================================

Features ✨
----------
- Enable the Polls feature. Allows to create, view, vote and end polls. ([#1196](https://github.com/element-hq/element-x-android/issues/1196))
- Create poll. ([#1143](https://github.com/element-hq/element-x-android/issues/1143))

Bugfixes 🐛
----------
- Ensure notification for Event from encrypted room get decrypted content. ([#1178](https://github.com/element-hq/element-x-android/issues/1178))
- Make sure Snackbars are only displayed once. ([#928](https://github.com/element-hq/element-x-android/issues/928))
- Fix the orientation of sent images. ([#1135](https://github.com/element-hq/element-x-android/issues/1135))
- Bug reporter crashes when 'send logs' is disabled. ([#1168](https://github.com/element-hq/element-x-android/issues/1168))
- Add missing link to the terms on the analytics setting screen. ([#1177](https://github.com/element-hq/element-x-android/issues/1177))
- Re-enable `SyncService.withEncryptionSync` to improve decryption of notifications. ([#1198](https://github.com/element-hq/element-x-android/issues/1198))
- Crash with `aspectRatio` modifier when `Float.NaN` was used as input. ([#1995](https://github.com/element-hq/element-x-android/issues/1995))

Other changes
-------------
- Remove unnecessary year in copyright mention. ([#1187](https://github.com/element-hq/element-x-android/issues/1187))


Changes in Element X v0.1.5 (2023-08-28)
========================================

Bugfixes 🐛
----------
- Fix crash when opening any room. ([#1160](https://github.com/element-hq/element-x-android/issues/1160))


Changes in Element X v0.1.4 (2023-08-28)
========================================

Features ✨
----------
- Allow cancelling media upload ([#769](https://github.com/element-hq/element-x-android/issues/769))
- Enable OIDC support. ([#1127](https://github.com/element-hq/element-x-android/issues/1127))
- Add a "Setting up account" screen, displayed the first time the user logs in to the app (per account). ([#1149](https://github.com/element-hq/element-x-android/issues/1149))

Bugfixes 🐛
----------
- Videos sent from the app were cropped in some cases. ([#862](https://github.com/element-hq/element-x-android/issues/862))
- Timeline: sender names are now displayed in one single line. ([#1033](https://github.com/element-hq/element-x-android/issues/1033))
- Fix `TextButtons` being displayed in black. ([#1077](https://github.com/element-hq/element-x-android/issues/1077))
- Linkify links in HTML contents. ([#1079](https://github.com/element-hq/element-x-android/issues/1079))
- Fix bug reporter failing after not finding some log files. ([#1082](https://github.com/element-hq/element-x-android/issues/1082))
- Fix rendering of inline elements in list items. ([#1090](https://github.com/element-hq/element-x-android/issues/1090))
- Fix crash RuntimeException "No matching key found for the ciphertext in the stream" ([#1101](https://github.com/element-hq/element-x-android/issues/1101))
- Make links in messages clickable again. ([#1111](https://github.com/element-hq/element-x-android/issues/1111))
- When event has no id, just cancel parsing the latest room message for a room. ([#1125](https://github.com/element-hq/element-x-android/issues/1125))
- Only display verification prompt after initial sync is done. ([#1131](https://github.com/element-hq/element-x-android/issues/1131))

In development 🚧
----------------
- [Poll] Add feature flag in developer options ([#1064](https://github.com/element-hq/element-x-android/issues/1064))
- [Polls] Improve UI and render ended state ([#1113](https://github.com/element-hq/element-x-android/issues/1113))

Other changes
-------------
- Compound: add `ListItem` and `ListSectionHeader` components. ([#990](https://github.com/element-hq/element-x-android/issues/990))
- Migrate `object` to `data object` in sealed interface / class #1135 ([#1135](https://github.com/element-hq/element-x-android/issues/1135))


Changes in Element X v0.1.2 (2023-08-16)
========================================

Bugfixes 🐛
----------
- Filter push notifications using push rules. ([#640](https://github.com/element-hq/element-x-android/issues/640))
- Use `for` instead of `forEach` in `DefaultDiffCacheInvalidator` to improve performance. ([#1035](https://github.com/element-hq/element-x-android/issues/1035))

In development 🚧
----------------
- [Poll] Render start event in the timeline ([#1031](https://github.com/element-hq/element-x-android/issues/1031))

Other changes
-------------
- Add Button component based on Compound designs ([#1021](https://github.com/element-hq/element-x-android/issues/1021))
- Compound: implement dialogs. ([#1043](https://github.com/element-hq/element-x-android/issues/1043))
- Compound: customise `IconButton` component. ([#1049](https://github.com/element-hq/element-x-android/issues/1049))
- Compound: implement `DropdownMenu` customisations. ([#1050](https://github.com/element-hq/element-x-android/issues/1050))
- Compound: implement Snackbar component. ([#1054](https://github.com/element-hq/element-x-android/issues/1054))


Changes in Element X v0.1.0 (2023-07-19)
========================================

First release of Element X 🚀!
