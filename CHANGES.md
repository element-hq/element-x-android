Changes in Element X v25.11.3
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.11.3 -->

## What's Changed
### 🙌 Improvements
* Improve rendering notification for multi account by @bmarty in https://github.com/element-hq/element-x-android/pull/5645
* Change : roles and permissions by @ganfra in https://github.com/element-hq/element-x-android/pull/5685
* Improve account provider selection during the login flow by @bmarty in https://github.com/element-hq/element-x-android/pull/5692
* Let notifications use avatar fallback. by @bmarty in https://github.com/element-hq/element-x-android/pull/5721
* Changes : member list improvements by @ganfra in https://github.com/element-hq/element-x-android/pull/5728
### 🐛 Bugfixes
* Do not use the bestDescription but the caption for images, when available by @bmarty in https://github.com/element-hq/element-x-android/pull/5684
* Add the user certificate if any when creating Matrix Client. by @bmarty in https://github.com/element-hq/element-x-android/pull/5686
* Ensure the form data are not lost when opening the log viewer. by @bmarty in https://github.com/element-hq/element-x-android/pull/5695
* Fix password flow when using a login link by @bmarty in https://github.com/element-hq/element-x-android/pull/5693
* Fix layout issue in text composer by @bmarty in https://github.com/element-hq/element-x-android/pull/5710
* Fix navigation stack overflow when sharing media by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5724
* Notification robustness by @bmarty in https://github.com/element-hq/element-x-android/pull/5726
* Send read receipts using the current timeline, not the live timeline by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5731
* Render Owner in the horizontal list when editing Admins. by @bmarty in https://github.com/element-hq/element-x-android/pull/5736
* Stop overriding the homeserver when restoring a `Client` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5753
* Revert "Stop overriding the homeserver when restoring a `Client`" by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5754
* Try fixing forced dark mode issues on MIUI on Android 10 by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5708
* Fix crash at startup by @bmarty in https://github.com/element-hq/element-x-android/pull/5761
* Fix null pointer exception on room notification settings.  by @bmarty in https://github.com/element-hq/element-x-android/pull/5758
* Fix crash when viewing Pinned events by @bmarty in https://github.com/element-hq/element-x-android/pull/5764
* Fix crash when pressing back from the showkase Activity by @bmarty in https://github.com/element-hq/element-x-android/pull/5772
* Fix navigation issue once incoming share is handled by @bmarty in https://github.com/element-hq/element-x-android/pull/5773
* Fix crash in work manager by @bmarty in https://github.com/element-hq/element-x-android/pull/5768
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5704
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5747
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5782
### 🧱 Build
* Module cleanup by @bmarty in https://github.com/element-hq/element-x-android/pull/5722
* Add `NIGHTLY` env for Sentry by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5779
### 🚧 In development 🚧
* Space : prepare Space Settings screen by @ganfra in https://github.com/element-hq/element-x-android/pull/5668
### Dependency upgrades
* fix(deps): update dependency androidx.core:core-splashscreen to v1.2.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5687
* fix(deps): update dependency com.posthog:posthog-android to v3.26.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5696
* fix(deps): update metro to v0.7.5 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5697
* Update dependency org.matrix.rustcomponents:sdk-android to v25.11.11 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5716
* Update plugin ktlint to v14 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5713
* Update plugin dependencycheck to v12.1.9 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5717
* Update dependency org.maplibre.gl:android-sdk to v12.1.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5714
* Update dependency io.sentry:sentry-android to v8.26.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5720
* Update sqldelight to v2.2.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5730
* fix(deps): update dependency com.squareup.okhttp3:okhttp-bom to v5.3.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5746
* fix(deps): update dependency com.google.firebase:firebase-bom to v34.6.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5737
* fix(deps): update metro to v0.7.6 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5752
* fix(deps): update dependency org.maplibre.gl:android-sdk to v12.1.3 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5743
* Update dependency com.squareup.okhttp3:okhttp-bom to v5.3.2 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5757
* fix(deps): update dependency com.pinterest.ktlint:ktlint-cli to v1.8.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5738
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.11.19 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5762
* fix(deps): update dependencyanalysis to v3.5.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5776
### Others
* Extract save change dialog by @bmarty in https://github.com/element-hq/element-x-android/pull/5679
* Use the dedicated subdomain for the bug report URL by default by @benbz in https://github.com/element-hq/element-x-android/pull/5689
* Convert `ComposerAlertMolecule` to use alert levels. by @kaylendog in https://github.com/element-hq/element-x-android/pull/5691
* Improve composer alert molecule by @bmarty in https://github.com/element-hq/element-x-android/pull/5701
* Code consistency around view event handling by @bmarty in https://github.com/element-hq/element-x-android/pull/5698
* Update copyright holders by @bmarty in https://github.com/element-hq/element-x-android/pull/5706
* Fix rendering notifications after receiving redundant push by @SpiritCroc in https://github.com/element-hq/element-x-android/pull/5711
* Fix push gateway with some push provider (Sunup/autopush) by @p1gp1g in https://github.com/element-hq/element-x-android/pull/5741
* Use new notification sound in release. by @bmarty in https://github.com/element-hq/element-x-android/pull/5748
* Fix issue on brand color override by @bmarty in https://github.com/element-hq/element-x-android/pull/5626
* Add media retention policy by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5749
* Enable logging OkHttp traffic based on the current log level by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5750
* Remove unused `slidingSyncProxy` from DB. by @bmarty in https://github.com/element-hq/element-x-android/pull/5755
* Add some performance metrics for Sentry by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5760

## New Contributors
* @benbz made their first contribution in https://github.com/element-hq/element-x-android/pull/5689
* @kaylendog made their first contribution in https://github.com/element-hq/element-x-android/pull/5691

**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.11.2...v25.11.3

Changes in Element X v25.11.2
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.11.2 -->

## What's Changed
### ✨ Features
* Enable access to security and privacy by @bmarty in https://github.com/element-hq/element-x-android/pull/5566
* Add ability to forward a media from the media viewer and the gallery by @bmarty in https://github.com/element-hq/element-x-android/pull/5622
* Split notifications for messages in threads by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5595
### 🙌 Improvements
* Enable `SyncNotificationsWithWorkManager` in nightly and debug builds by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5573
* Confirm exit without saving change in room details edit screen by @bmarty in https://github.com/element-hq/element-x-android/pull/5618
* Space : add view members entry by @ganfra in https://github.com/element-hq/element-x-android/pull/5619
* Update notification sound by @bmarty in https://github.com/element-hq/element-x-android/pull/5667
* Use the new notification sound only on debug and nightly build by @bmarty in https://github.com/element-hq/element-x-android/pull/5673
* Make sure we know the session verification state before showing the options to verify the session by @bmarty in https://github.com/element-hq/element-x-android/pull/5677
### 🐛 Bugfixes
* Improve how brand color is applied. by @bmarty in https://github.com/element-hq/element-x-android/pull/5584
* Improve wellknown retrieval API by @bmarty in https://github.com/element-hq/element-x-android/pull/5587
* Clearing the room list search clears the search term too by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5603
* Delete pin code only when the last session is deleted by @bmarty in https://github.com/element-hq/element-x-android/pull/5600
* Fix issues with WorkManager on Android 12 and below by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5606
* Fix marking a room as read re-instantiates its timeline by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5628
* Display only valid emojis in recent emoji list by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5612
* Fix navigation issue. by @bmarty in https://github.com/element-hq/element-x-android/pull/5666
* Fix forward events from media viewer from pinned media timeline by @bmarty in https://github.com/element-hq/element-x-android/pull/5669
* Try fixing 'Timeline Event object has already been destroyed' by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5675
* Use the SDK Client to check whether a homeserver is compatible by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5664
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5610
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5662
### 🧱 Build
* Remove `@Inject`, not necessary anymore when class is annotated with `@ContributesBinding` by @bmarty in https://github.com/element-hq/element-x-android/pull/5589
* Upgrade ktlint to 1.7.1 and ensure Renovate will upgrade the version by @bmarty in https://github.com/element-hq/element-x-android/pull/5638
* Improve architecture around Nodes by @bmarty in https://github.com/element-hq/element-x-android/pull/5641
* Move dependencies block out of the android block. by @bmarty in https://github.com/element-hq/element-x-android/pull/5674
* Always use the handleEvent(s) function the same way. by @bmarty in https://github.com/element-hq/element-x-android/pull/5672
### Dependency upgrades
* fix(deps): update metro to v0.7.2 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5576
* fix(deps): update dependencyanalysis to v3.2.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5577
* fix(deps): update dependency io.sentry:sentry-android to v8.24.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5586
* fix(deps): update dependency androidx.work:work-runtime-ktx to v2.11.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5590
* fix(deps): update dependency com.posthog:posthog-android to v3.25.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5594
* fix(deps): update dependency com.google.crypto.tink:tink-android to v1.19.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5572
* Update plugin sonarqube to v7.0.1.6134 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5605
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.10.28 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5620
* fix(deps): update dependencyanalysis to v3.3.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5602
* fix(deps): update dependency com.github.matrix-org:matrix-analytics-events to v0.29.2 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5621
* fix(deps): update dependencyanalysis to v3.4.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5624
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.10.29 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5625
* fix(deps): update dependency io.sentry:sentry-android to v8.25.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5629
* fix(deps): update dependencyanalysis to v3.4.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5642
* fix(deps): update dependency com.squareup.okhttp3:okhttp-bom to v5.3.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5644
* chore(deps): update danger/danger-js action to v13.0.5 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5652
* fix(deps): update dependency com.google.firebase:firebase-bom to v34.5.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5643
* fix(deps): update firebaseappdistribution to v5.2.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5640
* fix(deps): update metro to v0.7.3 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5663
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.10.31 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5657
* Update GitHub Artifact Actions (major) by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5609
* Update dependency io.element.android:element-call-embedded to v0.16.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5598
* Update roborazzi to v1.51.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5676
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.11.4 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5681
* fix(deps): update metro to v0.7.4 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5683
### Others
* Improve code around Element .well-known configuration by @bmarty in https://github.com/element-hq/element-x-android/pull/5565
* misc: display offline banner for all LoggedIn screens by @ganfra in https://github.com/element-hq/element-x-android/pull/5574
* Remove icon preview duplicate by @bmarty in https://github.com/element-hq/element-x-android/pull/5588
* Remove application navigation state usage in the push module by @bmarty in https://github.com/element-hq/element-x-android/pull/5596
* Design : update Home TopBar and RoomList Filters by @ganfra in https://github.com/element-hq/element-x-android/pull/5599
* Add missing tests on the analytic modules by @bmarty in https://github.com/element-hq/element-x-android/pull/5604
* design(space): let SpaceRoomItemView divider be full width by @ganfra in https://github.com/element-hq/element-x-android/pull/5597
* Update notification style by @bmarty in https://github.com/element-hq/element-x-android/pull/5607
* Improve how data is handled for the WorkManager. by @bmarty in https://github.com/element-hq/element-x-android/pull/5592
* Revert "Make sure declining a call stops observing the ringing call state" by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5615
* Misc : space flow inject room by @ganfra in https://github.com/element-hq/element-x-android/pull/5614
* Enable `SyncNotificationsWithWorkManager` by default in release mode apps too by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5646
* Revert "Update notification sound" by @bmarty in https://github.com/element-hq/element-x-android/pull/5671
* Introduce new query to count accounts by @bmarty in https://github.com/element-hq/element-x-android/pull/5678


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.11.0...v25.11.2

Changes in Element X v25.11.0
=============================

Hotfix release.

Includes https://github.com/element-hq/element-x-android/pull/5615, which fixes an issue that prevented Element Call notifications from being displayed sometimes.

**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.10.1...v25.11.0

Changes in Element X v25.10.1
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.10.1 -->

## What's Changed
### ✨ Features
* Sync notifications using WorkManager by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5545
### 🙌 Improvements
* Sort feature flags by @bmarty in https://github.com/element-hq/element-x-android/pull/5557
### 🐛 Bugfixes
* Makes sure images are loaded when cancelling multiaccount flow  by @ganfra in https://github.com/element-hq/element-x-android/pull/5502
* Fix 'test push loop back' notification check by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5541
* Display 'join anyway' button on room preview when the state can't be loaded by @ShadowRZ in https://github.com/element-hq/element-x-android/pull/5514
* Fix media viewer not being dismissed with reduced motion enabled by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5555
* Keep the cursor position in room list search when going back by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5570
* Make sure declining a call stops observing the ringing call state by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5563
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5515
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5562
### 🧱 Build
* Do some cleanup on our immutable annotation usage by @bmarty in https://github.com/element-hq/element-x-android/pull/5503
* `interface TestParameterValuesProvider` is deprecated. by @bmarty in https://github.com/element-hq/element-x-android/pull/5568
### Dependency upgrades
* fix(deps): update metro to v0.6.9 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5480
* fix(deps): update dependency org.unifiedpush.android:connector to v3.1.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5443
* fix(deps): update wysiwyg to v2.40.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5400
* fix(deps): update dependency io.github.sergio-sastre.composablepreviewscanner:android to v0.7.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5510
* fix(deps): update camera to v1.5.1 - autoclosed by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5509
* chore(deps): update plugin dependencycheck to v12.1.7 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5518
* chore(deps): update plugin licensee to v1.14.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5477
* chore(deps): update dependency python to 3.14 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5475
* fix(deps): update metro to v0.6.10 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5520
* fix(deps): update dependency org.unifiedpush.android:connector to v3.1.2 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5519
* chore(deps): update plugin gms_google_services to v4.4.4 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5507
* fix(deps): update dependency com.google.firebase:firebase-bom to v34.4.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5522
* fix(deps): update dependency com.squareup.okhttp3:okhttp-bom to v5.2.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5524
* fix(deps): update dependency net.zetetic:sqlcipher-android to v4.11.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5525
* fix(deps): update dependencyanalysis to v3.1.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5523
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.10.13 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5527
* chore(deps): update plugin dependencycheck to v12.1.8 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5531
* chore(deps): update rnkdsh/action-upload-diawi action to v1.5.12 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5533
* fix(deps): update dependency org.maplibre.gl:android-sdk to v12.0.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5548
* fix(deps): update metro to v0.7.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5554
* fix(deps): update dependency com.posthog:posthog-android to v3.24.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5564
* chore(deps): update plugin sonarqube to v7 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5535
### Others
* Import Compound tokens - fixed icons by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5506
* Replace Uri by String in States that are used in Composable function. by @bmarty in https://github.com/element-hq/element-x-android/pull/5508
* Let room filters follow the design. by @bmarty in https://github.com/element-hq/element-x-android/pull/5526
* Allow uploading notification push rules in bug reports by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5538
* Add number of accounts info in the rageshake data. by @bmarty in https://github.com/element-hq/element-x-android/pull/5532
* design(space): match figma for Space views by @ganfra in https://github.com/element-hq/element-x-android/pull/5540
* Extract console message logger and mutualize instance of Json by @bmarty in https://github.com/element-hq/element-x-android/pull/5552
* Improve colors customization by @bmarty in https://github.com/element-hq/element-x-android/pull/5542
* Fix test warning by @bmarty in https://github.com/element-hq/element-x-android/pull/5558


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.10.0...v25.10.1

Changes in Element X v25.10.0
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.10.0 -->

## What's Changed
### ✨ Features
* Use shared recent emoji reactions from account data by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5402
* Follow permalinks to and from threads by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5414
* Add support for Spaces by @bmarty in https://github.com/element-hq/element-x-android/pull/5462
* Add Labs screen for beta testing of public features by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5465
### 🙌 Improvements
* Update the strings for the device verification flow by @andybalaam in https://github.com/element-hq/element-x-android/pull/5419
* Set a notification sound by @bmarty in https://github.com/element-hq/element-x-android/pull/5469
* Improve current push provider test: give info about the distributor. by @bmarty in https://github.com/element-hq/element-x-android/pull/5471
* Improve AnnouncementService. by @bmarty in https://github.com/element-hq/element-x-android/pull/5482
### 🐛 Bugfixes
* Improvement and bugfix on incoming verification request screen by @bmarty in https://github.com/element-hq/element-x-android/pull/5426
* Space : makes sure to use room heroes for avatar by @ganfra in https://github.com/element-hq/element-x-android/pull/5488
* Filter out direct room in the leave space screen. by @bmarty in https://github.com/element-hq/element-x-android/pull/5498
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5427
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5460
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5486
### 🧱 Build
* Remove unused dependency on `javax.inject:javax.inject` by @bmarty in https://github.com/element-hq/element-x-android/pull/5445
* Internalize compound-android by @bmarty in https://github.com/element-hq/element-x-android/pull/5457
### 🚧 In development 🚧
* Sdk : use latest apis for space by @ganfra in https://github.com/element-hq/element-x-android/pull/5404
* Multi accounts - experimental first implementation by @bmarty in https://github.com/element-hq/element-x-android/pull/5285
* Leave space - UI by @bmarty in https://github.com/element-hq/element-x-android/pull/5354
* Leave spave: iteration on string value. by @bmarty in https://github.com/element-hq/element-x-android/pull/5425
* Feature : space list join action by @ganfra in https://github.com/element-hq/element-x-android/pull/5431
* Room list space invite by @ganfra in https://github.com/element-hq/element-x-android/pull/5449
* Leave space: use SDK API. by @bmarty in https://github.com/element-hq/element-x-android/pull/5432
* Space annoucement by @bmarty in https://github.com/element-hq/element-x-android/pull/5451
* feature(space) : keep space children in the presenter by @ganfra in https://github.com/element-hq/element-x-android/pull/5456
* Spaces : some tweaks around ui by @ganfra in https://github.com/element-hq/element-x-android/pull/5468
* Use "BETA" word from Localazy and ensure layout is correct by @bmarty in https://github.com/element-hq/element-x-android/pull/5470
* Disable avatar cluster for now by @bmarty in https://github.com/element-hq/element-x-android/pull/5492
### Dependency upgrades
* Update dependency com.posthog:posthog-android to v3.21.3 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5360
* Update dependency io.element.android:element-call-embedded to v0.16.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5408
* Update dependency net.java.dev.jna:jna to v5.18.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5398
* Update plugin dependencycheck to v12.1.6 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5405
* Update dependency org.matrix.rustcomponents:sdk-android to v25.9.25 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5412
* Update dependency androidx.sqlite:sqlite-ktx to v2.6.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5409
* Update kotlin by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5317
* Update metro to v0.6.7 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5416
* Update dependency app.cash.molecule:molecule-runtime to v2.2.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5413
* Update dependency com.posthog:posthog-android to v3.22.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5415
* Update metro to v0.6.8 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5422
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.10.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5438
* fix(deps): update dependency net.java.dev.jna:jna to v5.18.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5437
* fix(deps): update dependency io.mockk:mockk to v1.14.6 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5441
* Update gradle/actions action to v5 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5444
* fix(deps): update dependency io.sentry:sentry-android to v8.23.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5442
* fix(deps): update dependency org.maplibre.gl:android-sdk to v12 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5455
* fix(deps): update dependency com.posthog:posthog-android to v3.23.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5463
* fix(deps): update roborazzi to v1.50.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5464
* fix(deps): update telephoto to v0.18.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5459
### Others
* Ensure Metro `@AssistedInject` is used. by @bmarty in https://github.com/element-hq/element-x-android/pull/5420
* Misc : destroy SpaceRoomList by @ganfra in https://github.com/element-hq/element-x-android/pull/5436
* Remove CurrentSessionIdHolder and inject SessionId instead. by @bmarty in https://github.com/element-hq/element-x-android/pull/5440
* Only offer to verify if a cross-signed device is available by @uhoreg in https://github.com/element-hq/element-x-android/pull/5433
* Replace fun by val in MatrixClient by @bmarty in https://github.com/element-hq/element-x-android/pull/5466
* Space : makes sure to use SpaceRoom.displayName from sdk by @ganfra in https://github.com/element-hq/element-x-android/pull/5476
* Add preview with all icons in the Showkase browser by @bmarty in https://github.com/element-hq/element-x-android/pull/5485
* Ensure that we are using Immutable instead of Persistent by @bmarty in https://github.com/element-hq/element-x-android/pull/5490
* Reduce number of Previews for Avatar. by @bmarty in https://github.com/element-hq/element-x-android/pull/5495
* Fix error when attempting to verify with recovery key with missing backup key by @uhoreg in https://github.com/element-hq/element-x-android/pull/5314
* Sync strings by @bmarty in https://github.com/element-hq/element-x-android/pull/5499
* feature(space): make sure to handle topic properly by @ganfra in https://github.com/element-hq/element-x-android/pull/5493

## New Contributors
* @uhoreg made their first contribution in https://github.com/element-hq/element-x-android/pull/5433

**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.09.2...v25.10.0

Changes in Element X v25.09.2
=============================

## What's Changed
### ✨ Features
* Show progress dialog while we are sending invites in a room by @richvdh in https://github.com/element-hq/element-x-android/pull/5342
* Call: RTC decline event support by @BillCarsonFr in https://github.com/element-hq/element-x-android/pull/5305
* Add room info to the thread's top app bar by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5374
### 🙌 Improvements
* Use the new RtcNotification event instead of the now deprecated CallNotify by @BillCarsonFr in https://github.com/element-hq/element-x-android/pull/5357
### 🐛 Bugfixes
* Increase Element Call audio init delay ensuring the right audio device is used by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5315
* Do not center the dialog title text for dialogs with no icon by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5332
* Media viewer: release the `ExoPlayers` when the hosting composables are disposed by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5351
* Make PushData.clientSecret mandatory. by @bmarty in https://github.com/element-hq/element-x-android/pull/5369
* Cleanup ftue code and ensure verification confirmation is displayed by @bmarty in https://github.com/element-hq/element-x-android/pull/5379
* Change in clear cache behavior by @bmarty in https://github.com/element-hq/element-x-android/pull/5388
* fix (room navigation) : fix navigation when leaving room/space by @ganfra in https://github.com/element-hq/element-x-android/pull/5376
* fix (timeline) : forward pagination regression by @ganfra in https://github.com/element-hq/element-x-android/pull/5389
* When joining a call, wait for the `content_loaded` action by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5399
* Ensure the thread summary sender's display name won't wrap to the next line by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5403
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5349
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5385
### 🧱 Build
* Improve release script and the file Versions.kt by @bmarty in https://github.com/element-hq/element-x-android/pull/5318
* Dependency: extract the Matrix SDK and add instructions for upgrading the library by @bmarty in https://github.com/element-hq/element-x-android/pull/5363
* Add test on DefaultSpaceEntryPoint by @bmarty in https://github.com/element-hq/element-x-android/pull/5343
### 🚧 In development 🚧
* Space list by @bmarty in https://github.com/element-hq/element-x-android/pull/5320
* Feature : Join Space (WIP) by @ganfra in https://github.com/element-hq/element-x-android/pull/5378
### Dependency upgrades
* Update activity to v1.11.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5324
* Update dependency com.google.truth:truth to v1.4.5 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5322
* Update dependency io.sentry:sentry-android to v8.21.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5310
* Update dependency org.matrix.rustcomponents:sdk-android to v25.9.10 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5323
* Update dependency androidx.sqlite:sqlite-ktx to v2.6.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5337
* Update camera to v1.5.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5336
* Update dependency com.posthog:posthog-android to v3.21.2 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5333
* Update dependency com.google.testparameterinjector:test-parameter-injector to v1.19 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5341
* Upgrade Rust SDK bindings to v25.09.15 by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5353
* Update dependency org.matrix.rustcomponents:sdk-android to v25.9.16 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5359
* Update dependency org.matrix.rustcomponents:sdk-android to v25.9.18 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5365
* Update telephoto to v0.17.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5350
* Update dependency org.matrix.rustcomponents:sdk-android to v25.9.19 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5377
* Update dependency com.google.firebase:firebase-bom to v34.3.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5367
* Upgrade Element Call embedded dependency to `v0.16.0-rc.4` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5391
* Update dependencyAnalysis to v3 (major) by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5194
* Update dependency org.maplibre.gl:android-sdk to v11.13.5 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5381
* Update dependency org.matrix.rustcomponents:sdk-android to v25.9.23 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5396
* Update plugin dependencycheck to v12.1.5 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5382
* Update dependency io.sentry:sentry-android to v8.22.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5397
### Others
* Cleanup nodes by @bmarty in https://github.com/element-hq/element-x-android/pull/5358
* Complete test on MediaGalleryPresenter by @bmarty in https://github.com/element-hq/element-x-android/pull/5361
* Remove dead code by @bmarty in https://github.com/element-hq/element-x-android/pull/5306
* Introduce BugReportFlowNode, and remove NavTarget.ViewLogs from RootFlowNode by @bmarty in https://github.com/element-hq/element-x-android/pull/5370
* When logging out from Pin code screen, logout from all the sessions. by @bmarty in https://github.com/element-hq/element-x-android/pull/5372
* Clean MatrixAuthenticationService and SessionStore API by @bmarty in https://github.com/element-hq/element-x-android/pull/5371
* Add logs to detect duplicates in the room list by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5364
* Add troubleshoot notification test about blocked users by @bmarty in https://github.com/element-hq/element-x-android/pull/5394
* Add thread decoration with latest event details by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5355
* Rework on messages view top bars by @bmarty in https://github.com/element-hq/element-x-android/pull/5401
* Put developer settings at the end of the view by @p1gp1g in https://github.com/element-hq/element-x-android/pull/5387

## New Contributors
* @p1gp1g made their first contribution in https://github.com/element-hq/element-x-android/pull/5387

**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.09.1...v25.09.2

Changes in Element X v25.09.1
=============================

## What's Changed

We have migrated our DI libraries from Dagger and Anvil to Metro. If you need more details on the migration steps, please read the [documentation](https://github.com/element-hq/element-x-android/blob/develop/docs/migration_to_metro.md).

### ✨ Features
* Allow replying to a message with an attachment by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5261
* Add emoji search to the reaction emoji picker by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5255
### 🙌 Improvements
* Spelling correction in Update FeatureFlags.kt by @escix in https://github.com/element-hq/element-x-android/pull/5232
* [a11y] Add content descriptions to room list item indicators by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5236
* [a11y] Add click action to the message bottom sheet handle by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5228
### 🐛 Bugfixes
* Reload member list after moderation actions by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5268
* Restore view log code by @bmarty in https://github.com/element-hq/element-x-android/pull/5294
* Detect mime type when picking a file by @bmarty in https://github.com/element-hq/element-x-android/pull/5291
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5249
* Sync Strings - new translations to Korean by @ElementBot in https://github.com/element-hq/element-x-android/pull/5286
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5290
### 🧱 Build
* Iterate on build chain by @bmarty in https://github.com/element-hq/element-x-android/pull/5272
* Cleanup our DI solution and add documentation about the migration to Metro by @bmarty in https://github.com/element-hq/element-x-android/pull/5287
* Revert agp to 8.11 by @bmarty in https://github.com/element-hq/element-x-android/pull/5311
### 🚧 In development 🚧
* Space: add content in home screen by @bmarty in https://github.com/element-hq/element-x-android/pull/5273
* Hide the home navigation bar if the user is not a member of any Space. by @bmarty in https://github.com/element-hq/element-x-android/pull/5292
### Dependency upgrades
* Update dependency org.maplibre.gl:android-sdk to v11.13.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5239
* Update dependency com.google.firebase:firebase-bom to v34.2.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5245
* Update dependency com.posthog:posthog-android to v3.21.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5238
* Update dependency org.matrix.rustcomponents:sdk-android to v25.9.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5251
* Update plugin sonarqube to v6.3.1.5724 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5235
* Update android.gradle.plugin to v8.12.2 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5244
* Update dependency io.element.android:emojibase-bindings to v1.4.3 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5250
* Update actions/setup-python action to v6 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5270
* Update dependency com.posthog:posthog-android to v3.21.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5275
* Migrate Anvil KSP to Metro by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5253
* Update actions/github-script action to v8 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5284
* Update codecov/codecov-action action to v5.5.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5274
* Update dependency io.sentry:sentry-android to v8.21.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5293
### Others
* Remove LoginUserStory. by @bmarty in https://github.com/element-hq/element-x-android/pull/5237
* Update state in runUpdatingState when CancellationException occurs by @jbrenorv in https://github.com/element-hq/element-x-android/pull/5243
* Refactor: Move InMemorySessionStore to test module by @bmarty in https://github.com/element-hq/element-x-android/pull/5252
* Enable `largeHeap` option to have a larger max heap size by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5258
* Set a custom request config for the Client by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5266
* Set shortcut ID on received notifications to make them appear as a Conversation by @frebib in https://github.com/element-hq/element-x-android/pull/5192
* Improve management of shortcut ids. by @bmarty in https://github.com/element-hq/element-x-android/pull/5303

## New Contributors
* @escix made their first contribution in https://github.com/element-hq/element-x-android/pull/5232
* @jbrenorv made their first contribution in https://github.com/element-hq/element-x-android/pull/5243

**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.09.0...v25.09.1

Changes in Element X v25.09.0
=============================

This release is the same as `25.08.4` but it includes performance fixes for the timeline load times, included in the Rust SDK version upgrade and internal changes for Element Call.

## What's Changed
### 🧱 Build
* Revert "Try following KSP incremental best practices on `anvilcodegen`" by @bmarty in https://github.com/element-hq/element-x-android/pull/5233
### Dependency upgrades
* Update dependency io.element.android:element-call-embedded to v0.15.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5229
* Update dependency org.matrix.rustcomponents:sdk-android to v25.8.26 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5230
* Downgrade sonar scanner gradle plugin to `v6.2.0.5505` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5234


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.08.4...v25.09.0

Changes in Element X v25.08.4
=============================

## What's Changed
### ✨ Features
* Threads  - first iteration by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5165
* Add shortcut suggestions for rooms, remove then when leaving by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5180
* Allow replying to any remote message in a thread by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5201
### 🙌 Improvements
* Create room flow rework by @bmarty in https://github.com/element-hq/element-x-android/pull/5166
### 🐛 Bugfixes
* Fix bitrate value used for video transcoding by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5183
* Fix sending videos in Android 11 and lower by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5186
* Ensure that only one DataStore is active for the same file. by @bmarty in https://github.com/element-hq/element-x-android/pull/5198
* Handle preference stores corruption by clearing them by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5086
* Use variable bitrate mode when transcoding to ensure compatibility with old devices by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5223
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5178
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5211
### 🧱 Build
* Build release with the latest build tools 36.0.0 by @bmarty in https://github.com/element-hq/element-x-android/pull/5173
* Try following KSP incremental best practices on `anvilcodegen` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5205
* Split deeplink module and remove setupAnvil from api modules by @bmarty in https://github.com/element-hq/element-x-android/pull/5210
* Introduce a11y screenshot test by @bmarty in https://github.com/element-hq/element-x-android/pull/5214
* Custom logo on on boarding screen. by @bmarty in https://github.com/element-hq/element-x-android/pull/5217
### 🚧 In development 🚧
* Space UI component by @bmarty in https://github.com/element-hq/element-x-android/pull/5197
* Add UI components for spaces. by @bmarty in https://github.com/element-hq/element-x-android/pull/5207
### Dependency upgrades
* Update core to v1.17.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5168
* Update kotlin by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5169
* Update dependency org.matrix.rustcomponents:sdk-android to v25.8.18 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5182
* Update android.gradle.plugin to v8.12.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5184
* Update dagger to v2.57.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5193
* Update actions/setup-java action to v5 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5196
* Update codecov/codecov-action action to v5.5.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5191
* Update plugin ktlint to v13.1.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5204
* Update dependency com.posthog:posthog-android to v3.20.3 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5206
* Update dependency org.jsoup:jsoup to v1.21.2 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5212
* Update dependency com.posthog:posthog-android to v3.20.4 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5213
* Update plugin sonarqube to v6.3.0.5676 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5220
* Update dependency io.sentry:sentry-android to v8.20.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5216
* Update dependency org.matrix.rustcomponents:sdk-android to v25.8.25 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5219
### Others
* Iterate on invite people UI by @bmarty in https://github.com/element-hq/element-x-android/pull/5185
* AnalyticsOptInStateProvider does not need to have an injected constructor by @bmarty in https://github.com/element-hq/element-x-android/pull/5215
* Add extra logs for sending media by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5218
* Rename custom_logo to onboarding_logo by @bmarty in https://github.com/element-hq/element-x-android/pull/5226
* Add unit test on VideoCompressorHelper by @bmarty in https://github.com/element-hq/element-x-android/pull/5227


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.08.3...v25.08.4

Changes in Element X v25.08.3
=============================

## What's Changed
### ✨ Features
* Add media file limit size warning and media quality selection by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5131
### 🐛 Bugfixes
* Fix cursor position in room list search by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5138
* Fix leaving the room not always dismissing the room screen by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5089
* Do not automatically initialize `DefaultVideoMetadataExtractor`'s data source by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5157
* Provide calculated server names when opening a room from another by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5155
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5146
### 🧱 Build
* Compile and target sdk36 by @bmarty in https://github.com/element-hq/element-x-android/pull/5150
* Fix Maestro regression when coming back from room to the search screen by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5156
### Dependency upgrades
* Update android.gradle.plugin to v8.12.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5106
* Update wysiwyg to v2.39.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5080
* Update dependency python to 3.13 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5144
* Update rnkdsh/action-upload-diawi action to v1.5.11 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5141
* Update dependency io.github.sergio-sastre.ComposablePreviewScanner:android to v0.7.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5143
* Update actions/checkout action to v5 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5148
* Update dependency io.sentry:sentry-android to v8.19.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5149
* Update dependency io.sentry:sentry-android to v8.19.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5158
* Update dependency androidx.browser:browser to v1.9.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5096
* Update Compose bom to 2025.07.00 by @bmarty in https://github.com/element-hq/element-x-android/pull/5164
* Update showkase to v1.0.5 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5117
* Update haze to v1.6.10 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5167
### Others
* Let enterprise build be able to override (or disable) the bug report URL. by @bmarty in https://github.com/element-hq/element-x-android/pull/5139
* Hide the recovery key while we are entering it by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5147
* Remove old feature flags by @bmarty in https://github.com/element-hq/element-x-android/pull/5160
* Move push history entry point from notification settings to developer settings by @bmarty in https://github.com/element-hq/element-x-android/pull/5161


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.08.2...v25.08.3

Changes in Element X v25.08.2
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.08.2 -->

## What's Changed
### 🐛 Bugfixes
* When mapping an invalid notification event, only drop that one by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5137
### Dependency upgrades
* Update dependency io.nlopez.compose.rules:detekt to v0.4.27 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5123
* Update actions/download-artifact action to v5 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5122
* Update dependency net.zetetic:sqlcipher-android to v4.10.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5121
* Update dependency com.posthog:posthog-android to v3.20.2 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5133
* Update dependency com.google.firebase:firebase-bom to v34.1.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5136
### Others
* [a11y] Open context menu with the keyboard by @bmarty in https://github.com/element-hq/element-x-android/pull/5120
* Let enterprise build store the logs in a dedicated subfolder by @bmarty in https://github.com/element-hq/element-x-android/pull/5132
* Redirect FOSS user to Element Pro according to element .well-known file by @bmarty in https://github.com/element-hq/element-x-android/pull/5126


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.08.1...v25.08.2

Changes in Element X v25.08.1
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.08.1 -->

## What's Changed
### 🙌 Improvements
* Force last owner of a room to pass ownership when leaving by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5094
### 🐛 Bugfixes
* Reload room member list when active members count changes by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5129
* Delegate call notifications to Element Call, upgrade SDK and EC embedded by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5119
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5112
### Dependency upgrades
* Update media3 to v1.8.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5101


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.08.0...v25.08.1

Changes in Element X v25.08.0
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.08.0 -->

## What's Changed
### 🐛 Bugfixes
* Fix `toPlainText` where `<ol start='n'>` tags appear by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5044
* Remove the scaling added in `Player.Listener.onVideoSizeChanged` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5055
* Make sure we clean up the pre-processed and uploaded media by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5039
* Calculate video output size taking into account portrait mode by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5068
* Prevent loop when exiting the attachments preview screen by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5078
* Prevent crash caused by re-release of wakelock in calls by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5077
* Make sure we display errors when we create a recovery key and it fails by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5079
* Fix crash when trying to get active notifications by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5085
* Adapt 'change roles' screens to the new creator/owner role by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5076
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5021
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5054
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/5083
### 🧱 Build
* Disable Element Call Maestro tests for the time being by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5064
### 📄 Documentation
* Grammar fixes for docs and comments by @andybalaam in https://github.com/element-hq/element-x-android/pull/5043
* Note how to switch back to the published SDK after building locally by @andybalaam in https://github.com/element-hq/element-x-android/pull/5042
### Dependency upgrades
* Update dependency io.mockk:mockk to v1.14.5 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5037
* Update dependency androidx.lifecycle:lifecycle-process to v2.9.2 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5036
* Update dagger to v2.57 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5038
* Update haze to v1.6.9 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5045
* Update dependency io.nlopez.compose.rules:detekt to v0.4.24 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5053
* Update dependency io.nlopez.compose.rules:detekt to v0.4.25 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5058
* Update coil to v3.3.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5063
* Update dependency io.nlopez.compose.rules:detekt to v0.4.26 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5065
* Update dependency com.posthog:posthog-android to v3.20.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5067
* Update dependency com.google.firebase:firebase-bom to v34 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5061
* Update dependency org.matrix.rustcomponents:sdk-android to v25.7.23 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5073
* Update dependency com.posthog:posthog-android to v3.20.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5087
* Update dependency org.matrix.rustcomponents:sdk-android to v25.7.28 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5088
* Update dependency org.maplibre.gl:android-sdk to v11.13.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5093
* Update dependency androidx.test:runner to v1.7.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5102
* Update test.core to v1.7.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5104
* Update dependency androidx.test.ext:junit to v1.3.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5103
* Update dependency io.sentry:sentry-android to v8.18.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5097
### Others
* Iterate on FloatingActionButton shape and colors. by @bmarty in https://github.com/element-hq/element-x-android/pull/5033
* [a11y] Improve session verification screens by @bmarty in https://github.com/element-hq/element-x-android/pull/5017
* misc (room id) : add room id regex pattern to match new versions by @ganfra in https://github.com/element-hq/element-x-android/pull/5040
* Use lower level APIs to draw the message bubbles by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5056
* misc (store description) : update store description for fastlane by @ganfra in https://github.com/element-hq/element-x-android/pull/5060
* [a11y] Improve accessibility on avatar when creating a room. by @bmarty in https://github.com/element-hq/element-x-android/pull/5046
* Add fallback notifications from UTDs to the push history by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5047
* feature (media send queue) : enable send queue by default by @ganfra in https://github.com/element-hq/element-x-android/pull/5098
* misc : re-enable share pos by default by @ganfra in https://github.com/element-hq/element-x-android/pull/5108


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.07.1...v25.08.0

Changes in Element X v25.07.1
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.07.1 -->

## What's Changed
### 🐛 Bugfixes
* fix ( room list) : rebuild with filteredSummaries to avoid bad state by @ganfra in https://github.com/element-hq/element-x-android/pull/4993
* Keep video rotation metadata when transcoding by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5008
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4988
### 🧱 Build
* Update Gradle Wrapper from 8.14.2 to 8.14.3 by @ElementBot in https://github.com/element-hq/element-x-android/pull/4985
* Stop ignoring dependencies, but instead set `open-pull-requests-limit to 0 by @bmarty in https://github.com/element-hq/element-x-android/pull/5013
### 📄 Documentation
* Update to the status and clarifications with respect to the legacy app. by @mxandreas in https://github.com/element-hq/element-x-android/pull/5016
### 🚧 In development 🚧
* Home navigation bar fixes by @bmarty in https://github.com/element-hq/element-x-android/pull/4990
* Home screen iteration by @bmarty in https://github.com/element-hq/element-x-android/pull/5003
### Dependency upgrades
* Update dependency io.element.android:compound-android to v25.7.4 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/4984
* Update dependency org.matrix.rustcomponents:sdk-android to v25.7.7 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/4989
* Update plugin ktlint to v13 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/4992
* Update dependency org.jetbrains.kotlinx:kotlinx-datetime to v0.7.1-0.6.x-compat by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/4991
* Update haze to v1.6.7 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/4987
* Update dependency com.squareup.okhttp3:okhttp-bom to v5 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/4979
* Update dependency io.sentry:sentry-android to v8.17.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/4998
* Update dependency com.squareup.okhttp3:okhttp-bom to v5.1.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/4997
* Update dependency org.maplibre.gl:android-sdk to v11.12.0 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5001
* Update dependency com.posthog:posthog-android to v3.19.2 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5009
* Update dependency org.maplibre.gl:android-sdk to v11.12.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5006
* Update android.gradle.plugin to v8.11.1 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5014
* Update rnkdsh/action-upload-diawi action to v1.5.10 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5019
* Update wysiwyg to v2.38.5 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5025
* Update haze to v1.6.8 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5026
* Update dependency org.matrix.rustcomponents:sdk-android to v25.7.15 by @renovate[bot] in https://github.com/element-hq/element-x-android/pull/5011
### Others
* Remove bloom effect and replace by linear gradient by @bmarty in https://github.com/element-hq/element-x-android/pull/4926
* misc (a11y) : mark MainActionButton icon as decorative by @ganfra in https://github.com/element-hq/element-x-android/pull/4996
* Make `ContentAvoidingLayoutData` an immutable data class by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4999
* Remove unused composable and cleanup colors by @bmarty in https://github.com/element-hq/element-x-android/pull/5000
* Add a feature flag to reuse the last `pos` value for initial syncs by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5010
* [a11y] Fix several issues around accessibility by @bmarty in https://github.com/element-hq/element-x-android/pull/5007
* Replace video transcoder with Media3 Transformer by @jmartinesp in https://github.com/element-hq/element-x-android/pull/5018


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.07.0...v25.07.1

Changes in Element X v25.07.0
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.07.0 -->

## What's Changed
### 🙌 Improvements
* Change : handle invalid invite error  by @ganfra in https://github.com/element-hq/element-x-android/pull/4909
* Add ability to zoom on video. by @bmarty in https://github.com/element-hq/element-x-android/pull/4916
* Change : sync moderation and safety preferences with server by @ganfra in https://github.com/element-hq/element-x-android/pull/4962
### 🐛 Bugfixes
* Restore `MarkdownEditText.focusSearch` override by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4908
* Fix duplicate usage of a `modifier` variable in `TextInputBox` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4928
### 🗣 Translations
* Sync Strings - new translations to Danish by @ElementBot in https://github.com/element-hq/element-x-android/pull/4913
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4983
### 🧱 Build
* a11y: Add scripts to enable and disable the talkback service by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4906
* Update min api level to 33 for Element enterprise by @bmarty in https://github.com/element-hq/element-x-android/pull/4960
### 🚧 In development 🚧
* Rename module roomlist to home by @bmarty in https://github.com/element-hq/element-x-android/pull/4955
* Home navigation bar by @bmarty in https://github.com/element-hq/element-x-android/pull/4964
### Dependency upgrades
* fix(deps): update dependency org.unifiedpush.android:connector to v3.0.10 by @renovate in https://github.com/element-hq/element-x-android/pull/4871
* fix(deps): update dependency io.sentry:sentry-android to v8.14.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4892
* fix(deps): update dependency com.google.crypto.tink:tink-android to v1.18.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4897
* fix(deps): update wysiwyg to v2.38.4 by @renovate in https://github.com/element-hq/element-x-android/pull/4907
* fix(deps): update dependency org.robolectric:robolectric to v4.15 by @renovate in https://github.com/element-hq/element-x-android/pull/4901
* fix(deps): update dependency androidx.sqlite:sqlite-ktx to v2.5.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4898
* fix(deps): update dependency io.mockk:mockk to v1.14.4 by @renovate in https://github.com/element-hq/element-x-android/pull/4912
* fix(deps): update dependency org.robolectric:robolectric to v4.15.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4911
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.6.23 by @renovate in https://github.com/element-hq/element-x-android/pull/4917
* fix(deps): update dependencyanalysis to v2.19.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4932
* fix(deps): update dependency org.jsoup:jsoup to v1.21.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4914
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.6.25 by @renovate in https://github.com/element-hq/element-x-android/pull/4936
* fix(deps): update dependency io.sentry:sentry-android to v8.15.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4938
* fix(deps): update dependency org.maplibre.gl:android-sdk to v11.11.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4939
* fix(deps): update dependency com.google.firebase:firebase-bom to v33.16.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4945
* fix(deps): update dependency io.sentry:sentry-android to v8.16.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4941
* Update sdk to version 25.7.1 by @bmarty in https://github.com/element-hq/element-x-android/pull/4966
* Update haze to v1.6.6 by @renovate in https://github.com/element-hq/element-x-android/pull/4968
* Update dependency com.google.gms:google-services to v4.4.3 by @renovate in https://github.com/element-hq/element-x-android/pull/4946
* Update android.gradle.plugin to v8.11.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4931
* Update dependency io.element.android:element-call-embedded to v0.13.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4969
* Update dependency org.matrix.rustcomponents:sdk-android to v25.7.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4967
* Upgrade compose bom to 2025.06.01 by @bmarty in https://github.com/element-hq/element-x-android/pull/4970
* Update kotlin by @renovate in https://github.com/element-hq/element-x-android/pull/4918
* Update dependency io.element.android:element-call-embedded to v0.13.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4977
* Update dependency org.matrix.rustcomponents:sdk-android to v25.7.3 by @ganfra in https://github.com/element-hq/element-x-android/pull/4976
### Others
* a11y: Make isTalkbackActive() live. by @bmarty in https://github.com/element-hq/element-x-android/pull/4903
* a11y: improve accessibility on grouped state events header. by @bmarty in https://github.com/element-hq/element-x-android/pull/4902
* Room debug info by @bmarty in https://github.com/element-hq/element-x-android/pull/4904
* [a11y] Improve accessibility of message composer by @bmarty in https://github.com/element-hq/element-x-android/pull/4900
* refactor: Migrate SQLCipher Android to new API by @ShadowRZ in https://github.com/element-hq/element-x-android/pull/4874
* Iterate on avatar to be able to render Space avatar. by @bmarty in https://github.com/element-hq/element-x-android/pull/4921
* Simplify syncing the room list when receiving a push by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4915
* Add unit test on ChooseAccountProviderState so that the coverage is above 90% by @bmarty in https://github.com/element-hq/element-x-android/pull/4924
* Iterate on avatar to be able to render Space avatar Part2 by @bmarty in https://github.com/element-hq/element-x-android/pull/4923
* Introduce SessionEnterpriseService. by @bmarty in https://github.com/element-hq/element-x-android/pull/4925
* Simplify message composer layout by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4884
* Display error dialog if Element Call can't be joined by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4919
* misc : simplify timeline diff logic by @ganfra in https://github.com/element-hq/element-x-android/pull/4930
* Navigation bar component by @bmarty in https://github.com/element-hq/element-x-android/pull/4940
* a11y: improve content description of the close buttons by @bmarty in https://github.com/element-hq/element-x-android/pull/4943
* Element Call: remove top app bar and add it inside the webview instead by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4927
* Replace the Report a problem button with the app's version on the on boading screen. by @bmarty in https://github.com/element-hq/element-x-android/pull/4944
* Split RoomListPresenter and introduce HomePresenter by @bmarty in https://github.com/element-hq/element-x-android/pull/4958
* Add "View avatar" content description to all clickable Avatar that will open the avatar preview. by @bmarty in https://github.com/element-hq/element-x-android/pull/4948
* [a11y] Ensure that the focus is not lost when the send button state change by @bmarty in https://github.com/element-hq/element-x-android/pull/4975
* [a11y] add missing heading() qualifier on screen titles and other headers by @bmarty in https://github.com/element-hq/element-x-android/pull/4980
* misc (tracing) : add new TraceLogPack.Notification by @ganfra in https://github.com/element-hq/element-x-android/pull/4981


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.06.3...v25.07.0

Changes in Element X v25.06.3
=============================

## What's Changed
### ✨ Features
* Feature : room version upgrade by @ganfra in https://github.com/element-hq/element-x-android/pull/4862
* Add a developer option for history sharing on invite by @richvdh in https://github.com/element-hq/element-x-android/pull/4821
### 🙌 Improvements
* Change : add tombstoned room decoration  by @ganfra in https://github.com/element-hq/element-x-android/pull/4891
* Show generic notification when Event cannot be resolved by @bmarty in https://github.com/element-hq/element-x-android/pull/4889
### 🐛 Bugfixes
* [a11y] Improve screen reader on polls by @bmarty in https://github.com/element-hq/element-x-android/pull/4875
* fix (event action): allow to edit only if permission to send message by @ganfra in https://github.com/element-hq/element-x-android/pull/4895
* fix (room upgrade) : room predecessor banner on DM room by @ganfra in https://github.com/element-hq/element-x-android/pull/4896
* fix (join room) : do not navigate up when join is successful by @ganfra in https://github.com/element-hq/element-x-android/pull/4899
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4842
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4881
### Dependency upgrades
* chore(deps): update plugin dependencycheck to v12.1.3 by @renovate in https://github.com/element-hq/element-x-android/pull/4856
* fix(deps): update dependency org.maplibre.gl:android-sdk to v11.10.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4858
* fix(deps): update kotlin to v2.1.21-2.0.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4850
* fix(deps): update dependency app.cash.turbine:turbine to v1.2.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4865
* Update dependency com.posthog:posthog-android to v3.18.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4873
* Update dependency org.maplibre.gl:android-sdk to v11.10.3 by @renovate in https://github.com/element-hq/element-x-android/pull/4879
* fix(deps): update dependency com.posthog:posthog-android to v3.19.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4882
* fix(deps): update dependency io.sentry:sentry-android to v8.13.3 by @renovate in https://github.com/element-hq/element-x-android/pull/4870
* fix(deps): update showkase to v1.0.4 by @renovate in https://github.com/element-hq/element-x-android/pull/4878
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.6.18 by @renovate in https://github.com/element-hq/element-x-android/pull/4894
### Others
* Annotate Composable functions with `@ReadOnlyComposable` where it's possible by @bmarty in https://github.com/element-hq/element-x-android/pull/4859
* Add documentation on WebViewPipController by @bmarty in https://github.com/element-hq/element-x-android/pull/4861
* Small cleanup around log tag. by @bmarty in https://github.com/element-hq/element-x-android/pull/4860
* Another cleanup by @bmarty in https://github.com/element-hq/element-x-android/pull/4869
* Disable BT audio devices for Element Call on Android < 12 by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4876
* Add a banner to ask the user to disable battery optimization when Event cannot be resolved from Push by @bmarty in https://github.com/element-hq/element-x-android/pull/4845
* a11y: improve accessibility on rich text editor options. by @bmarty in https://github.com/element-hq/element-x-android/pull/4886
* A11Y: improve accessibility on event reactions. by @bmarty in https://github.com/element-hq/element-x-android/pull/4877


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.06.2...v25.06.3

Changes in Element X v25.06.2
=============================

## What's Changed
### 🐛 Bugfixes
* Fix crash when using Element Call on API <= 30 by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4847
* Element Call: add delay before selecting the default audio device by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4854
* Fix for message composer losing focus in Compose 1.8.0 by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4853
### Dependency upgrades
* chore(deps): update plugin dependencycheck to v12.1.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4840
* deps (matrix rust sdk) : bump version to 25.06.10 by @ganfra in https://github.com/element-hq/element-x-android/pull/4855
### Others
* feat: Support matrix: links by @ShadowRZ in https://github.com/element-hq/element-x-android/pull/4839


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.06.1...v25.06.2

## What's Changed
### ✨ Features
* Enable support for Android Auto. by @bmarty in https://github.com/element-hq/element-x-android/pull/4818
* Element Call: Add audio output selector handled by Android by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4663
### 🙌 Improvements
* Oidc: Fallback to external browser instead of using Webview by @bmarty in https://github.com/element-hq/element-x-android/pull/4808
* change (room member moderation) : update icon to match figma by @ganfra in https://github.com/element-hq/element-x-android/pull/4837
### 🐛 Bugfixes
* Fix login flow by @bmarty in https://github.com/element-hq/element-x-android/pull/4813
* fix: When sending media as files use the `octet-stream` type by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4815
* fix: Make `Client.findDM` return a `Result` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4816
* Mark room as fully read when user goes back to the room list. by @bmarty in https://github.com/element-hq/element-x-android/pull/2687
* fix (identity change) :  RoomMemberIdentityStateChange in non encrypted room by @ganfra in https://github.com/element-hq/element-x-android/pull/4824
* Fix room and user avatar downloaded with a `.bin` extension. by @bmarty in https://github.com/element-hq/element-x-android/pull/4830
* Log the push resolving failure reason if available by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4835
### 🧱 Build
* Update Gradle Wrapper from 8.14.1 to 8.14.2 by @ElementBot in https://github.com/element-hq/element-x-android/pull/4831
### Dependency upgrades
* fix(deps): update dependency androidx.compose:compose-bom to v2025.04.01 by @renovate in https://github.com/element-hq/element-x-android/pull/4631
* fix(deps): update dependency androidx.compose:compose-bom to v2025.05.01 by @renovate in https://github.com/element-hq/element-x-android/pull/4814
* fix(deps): update dependency io.sentry:sentry-android to v8.13.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4780
* fix(deps): update appyx to v1.7.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4672
* fix(deps): update telephoto to v0.16.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4749
* fix(deps): update coil to v3.2.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4712
* fix(deps): update dependency androidx.webkit:webkit to v1.14.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4823
* fix(deps): update dependency com.posthog:posthog-android to v3.17.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4827
* fix(deps): update dependency io.element.android:element-call-embedded to v0.12.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4832
* fix(deps): update dependency com.google.firebase:firebase-bom to v33.15.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4833
* fix(deps): update dependency org.maplibre.gl:android-sdk to v11.10.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4825
* fix(deps): update lifecycle to v2.9.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4822
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.6.6 by @renovate in https://github.com/element-hq/element-x-android/pull/4834
* fix(deps): update dependency io.element.android:opusencoder to v1.2.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4836
### Others
* Add `catchingExceptions` method to replace `runCatching` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4797
* Rename classes overriding classes from the FFI layer. by @bmarty in https://github.com/element-hq/element-x-android/pull/4817
* Fix coroutine scope by @bmarty in https://github.com/element-hq/element-x-android/pull/4820
* Add extra logs the 'send call notification' flow by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4819
* misc (matrix) : use innerClient.subscribeToRoomInfo sdk method by @ganfra in https://github.com/element-hq/element-x-android/pull/4838


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.06.0...v25.06.1

Changes in Element X v25.06.0
=============================

Rust SDK: https://github.com/matrix-org/matrix-rust-sdk/releases/tag/matrix-sdk-ffi%2F20250603

## What's Changed
### ✨ Features
* Add support for login link by @bmarty in https://github.com/element-hq/element-x-android/pull/4752
### 🙌 Improvements
* On boarding flow: add a screen to select account provider among a fixed list by @bmarty in https://github.com/element-hq/element-x-android/pull/4769
* Change : RoomMember moderation by @ganfra in https://github.com/element-hq/element-x-android/pull/4779
### 🐛 Bugfixes
* Fix left room membership change by @ganfra in https://github.com/element-hq/element-x-android/pull/4765
* fix: exclude more domains from being backed up by the system by @lucasmz-dev in https://github.com/element-hq/element-x-android/pull/4773
* Make sure HeaderFooterPage contents can be scrolled by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4704
* Fix mobile link by @bmarty in https://github.com/element-hq/element-x-android/pull/4805
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4775
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4804
### 🧱 Build
* Maestro: fix MAS and EC breaking the tests by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4762
* Update Gradle Wrapper from 8.14 to 8.14.1 by @ElementBot in https://github.com/element-hq/element-x-android/pull/4766
* Stronger lambda error by @bmarty in https://github.com/element-hq/element-x-android/pull/4771
* Use Localazy's `langAliases` for Indonesian language by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4801
### Dependency upgrades
* fix(deps): update datastore to v1.1.7 by @renovate in https://github.com/element-hq/element-x-android/pull/4754
* fix(deps): update dependency org.maplibre.gl:android-sdk to v11.8.8 by @renovate in https://github.com/element-hq/element-x-android/pull/4721
* chore(deps): update plugin ktlint to v12.3.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4767
* fix(deps): update dependency com.google.firebase:firebase-bom to v33.14.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4755
* Update UnifiedPush library by @bmarty in https://github.com/element-hq/element-x-android/pull/4358
* fix(deps): update sqldelight to v2.1.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4735
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.5.26 by @renovate in https://github.com/element-hq/element-x-android/pull/4781
* fix(deps): update dependency com.posthog:posthog-android to v3.15.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4787
* fix(deps): update dependency com.posthog:posthog-android to v3.16.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4789
* fix(deps): update dependency io.element.android:element-call-embedded to v0.12.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4743
* fix(deps): update dependencyanalysis to v2.18.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4796
* fix(deps): update android.gradle.plugin to v8.10.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4795
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.5.29 by @renovate in https://github.com/element-hq/element-x-android/pull/4799
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.6.3 by @renovate in https://github.com/element-hq/element-x-android/pull/4810
### Others
* fix(deps): update media3 to v1.7.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4733
* fix: Ignore global proxy settings if system thinks there's none by @ShadowRZ in https://github.com/element-hq/element-x-android/pull/4744
* Add `ActiveRoomHolder` to manage the active room for a session by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4758
* Notification events resolving and rendering in batches by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4722
* Hide Element Call entry point if Element Call service is not available. by @bmarty in https://github.com/element-hq/element-x-android/pull/4783
* Fix dependencies on test by @bmarty in https://github.com/element-hq/element-x-android/pull/4790
* Update _developer_onboarding.md by @lex-neufeld in https://github.com/element-hq/element-x-android/pull/4570

## New Contributors
* @lucasmz-dev made their first contribution in https://github.com/element-hq/element-x-android/pull/4773
* @lex-neufeld made their first contribution in https://github.com/element-hq/element-x-android/pull/4570

**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.05.4...v25.06.0

<!-- Release notes generated using configuration in .github/release.yml at v25.05.4 -->

Changes in Element X v25.05.4
=============================

Rust SDK: https://github.com/matrix-org/matrix-rust-sdk/releases/tag/matrix-sdk-ffi%2F20250521

## What's Changed
### 🙌 Improvements
* Change (report room) : check if server supports the report room api by @ganfra in https://github.com/element-hq/element-x-android/pull/4718
### 🐛 Bugfixes
* Improve audio focus management by @bmarty in https://github.com/element-hq/element-x-android/pull/4707
* When transcoding a video fails, send it as a file by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4257
* Disable mutliple click (parallel or serial) on a room by @bmarty in https://github.com/element-hq/element-x-android/pull/4683
* Fix generic mime type used when externally sharing several files by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4715
* Fix issues on JoinedRoom / BaseRoom by @bmarty in https://github.com/element-hq/element-x-android/pull/4724
* Use the right live timeline instance in `RustRoomFactory` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4745
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4739
### 🧱 Build
* Ensure the CI is marked as failed when Maestro test is failing by @bmarty in https://github.com/element-hq/element-x-android/pull/4700
* Trigger pipeline build when a release tag is pushed by @bmarty in https://github.com/element-hq/element-x-android/pull/4741
* Fix compilation issues. by @bmarty in https://github.com/element-hq/element-x-android/pull/4750
### 📄 Documentation
* README.md: fix broken link by @richvdh in https://github.com/element-hq/element-x-android/pull/4728
### Dependency upgrades
* chore(config): migrate renovate config by @renovate in https://github.com/element-hq/element-x-android/pull/4688
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.5.13 by @renovate in https://github.com/element-hq/element-x-android/pull/4716
* fix(deps): update dependency io.sentry:sentry-android to v8.12.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4717
* chore(deps): update plugin sonarqube to v6.2.0.5505 by @renovate in https://github.com/element-hq/element-x-android/pull/4725
* fix(deps): update dependency com.posthog:posthog-android to v3.15.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4723
* fix(deps): update dependency com.squareup.retrofit2:retrofit-bom to v2.12.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4727
* chore(deps): update codecov/codecov-action action to v5.4.3 by @renovate in https://github.com/element-hq/element-x-android/pull/4730
* fix(deps): update kotlin by @renovate in https://github.com/element-hq/element-x-android/pull/4713
* fix(deps): update dependency com.squareup.retrofit2:retrofit-bom to v3 by @renovate in https://github.com/element-hq/element-x-android/pull/4729
* fix(deps): update kotlinpoet to v2.2.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4732
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.5.21 by @renovate in https://github.com/element-hq/element-x-android/pull/4759
### Others
* Remove event cache feature flag by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4719
* Check homeserver when login using qr code by @bmarty in https://github.com/element-hq/element-x-android/pull/4708
* Merge on boarding module to login module by @bmarty in https://github.com/element-hq/element-x-android/pull/4746
* Allow configuration to provide multiple account providers. by @bmarty in https://github.com/element-hq/element-x-android/pull/4742
* Reduce API of JoinedRoom, caller must use the Timeline API from liveTimeline instead by @bmarty in https://github.com/element-hq/element-x-android/pull/4731


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.05.3...v25.05.4

Changes in Element X v25.05.3
=============================

Version 25.05.2 was skipped.

## What's Changed
### 🐛 Bugfixes
* Disable Continue button when the login field is cleared. by @bmarty in https://github.com/element-hq/element-x-android/pull/4699
* Revert "fix(deps): update dependency io.element.android:element-call-embedded to v0.10.0", which caused an issue with to-device events in the latest version by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4706
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4703
### 🧱 Build
* Update Gradle Wrapper from 8.13 to 8.14 by @ElementBot in https://github.com/element-hq/element-x-android/pull/4645
### Dependency upgrades
* fix(deps): update datastore to v1.1.6 by @renovate in https://github.com/element-hq/element-x-android/pull/4630
* fix(deps): update lifecycle to v2.9.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4693
* fix(deps): update dependency androidx.sqlite:sqlite-ktx to v2.5.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4692
### Others
* Update "Learn more" link by @bmarty in https://github.com/element-hq/element-x-android/pull/4686
* Keep call notification ringing while a call is present in the room by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4634


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.05.1...v25.05.3

Changes in Element X v25.05.1
=============================

## What's Changed
### 🐛 Bugfixes
* Fix broken Element Call in 25.05.0 by @bmarty in https://github.com/element-hq/element-x-android/pull/4694
### Dependency upgrades
* fix(deps): update android.gradle.plugin to v8.10.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4687
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.5.8 by @renovate in https://github.com/element-hq/element-x-android/pull/4696
### Others
* Let EnterpriseService prevent usage of homeserver by @bmarty in https://github.com/element-hq/element-x-android/pull/4682


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.05.0...v25.05.1

Changes in Element X v25.05.0
=============================

## What's Changed
### ✨ Features
* Feature :  Report room  by @ganfra in https://github.com/element-hq/element-x-android/pull/4654
### 🙌 Improvements
* Render kick and ban reason in the timeline when available by @bmarty in https://github.com/element-hq/element-x-android/pull/4642
### 🐛 Bugfixes
* Accessibility: improve behavior of list items by @bmarty in https://github.com/element-hq/element-x-android/pull/4626
* Render caller avatar on Incoming call screen by @bmarty in https://github.com/element-hq/element-x-android/pull/4635
* Fix `Client.getJoinedRoom` crash when a room doesn't exist locally by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4656
* Fix wrong member count in join room screen for invitation by @bmarty in https://github.com/element-hq/element-x-android/pull/4651
* Make sure any `JoinedRustRoom` is destroyed after being used by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4678
* Fix read receipt behavior when the timeline is opened. by @bmarty in https://github.com/element-hq/element-x-android/pull/4679
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4648
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4677
### 🧱 Build
* OIDC configuration by @bmarty in https://github.com/element-hq/element-x-android/pull/4623
* Pin commit sha on GitHub actions by @bmarty in https://github.com/element-hq/element-x-android/pull/4653
### Dependency upgrades
* fix(deps): update dependency io.sentry:sentry-android to v8.9.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4624
* fix(deps): update dependency com.posthog:posthog-android to v3.14.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4628
* fix(deps): update dependency androidx.exifinterface:exifinterface to v1.4.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4632
* fix(deps): update dependencyanalysis to v2.17.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4638
* fix(deps): update dependency com.google.firebase:firebase-bom to v33.13.0 - autoclosed by @renovate in https://github.com/element-hq/element-x-android/pull/4637
* fix(deps): update dependency io.sentry:sentry-android to v8.10.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4644
* fix(deps): update dependency org.jsoup:jsoup to v1.20.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4655
* fix(deps): update dependency com.google.accompanist:accompanist-permissions to v0.37.3 by @renovate in https://github.com/element-hq/element-x-android/pull/4649
* fix(deps): update dependency io.sentry:sentry-android to v8.11.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4660
* fix(deps): update dependency io.mockk:mockk to v1.14.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4658
* fix(deps): update dependency io.github.sergio-sastre.composablepreviewscanner:android to v0.6.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4647
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.4.30 by @renovate in https://github.com/element-hq/element-x-android/pull/4665
* fix(deps): update kotlin to v2.1.20-2.0.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4670
* fix(deps): update dependency io.sentry:sentry-android to v8.11.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4668
* fix(deps): update dependency io.element.android:element-call-embedded to v0.10.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4667
* chore(deps): update rnkdsh/action-upload-diawi action to v1.5.9 by @renovate in https://github.com/element-hq/element-x-android/pull/4674
* fix(deps): update dependency org.maplibre.gl:android-sdk to v11.8.7 by @renovate in https://github.com/element-hq/element-x-android/pull/4673
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.5.6 by @renovate in https://github.com/element-hq/element-x-android/pull/4681
### Others
* Split `MatrixRoom` into `MatrixRoom` and `JoinedMatrixRoom` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4561
* Cleanup element call and UI by @bmarty in https://github.com/element-hq/element-x-android/pull/4641
* Take change of screen_change_server_error_no_sliding_sync_message into account by @bmarty in https://github.com/element-hq/element-x-android/pull/4650
* Improve the callback uri format and customization. by @bmarty in https://github.com/element-hq/element-x-android/pull/4664


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.04.3...v25.05.0

Changes in Element X v25.04.3
=============================

### 🙌 Improvements
* Use PreferenceDropdown for appearance by @ganfra in https://github.com/element-hq/element-x-android/pull/4581
### 🐛 Bugfixes
* Use in-call volume and mode for EC by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4481
* Send SVG images as files by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4595
* Fetch the initial ignored user list manually when subscribing by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4598
* Fix audio output selection for Element Call by @bmarty in https://github.com/element-hq/element-x-android/pull/4602
* [a11y] Make more items focusable by @bmarty in https://github.com/element-hq/element-x-android/pull/4605
* Fix ringing calls not stopping when the other user cancels the call by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4613
* Ensure that pinning an event makes the pinned messages banner appear by @bmarty in https://github.com/element-hq/element-x-android/pull/4606
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4590
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4612
### 📄 Documentation
* Improve onboarding docs: by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4578
### Dependency upgrades
* Upgrade Rust bindings to `v25.04.11` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4580
* fix(deps): update dependency androidx.sqlite:sqlite-ktx to v2.5.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4568
* fix(deps): update dependency app.cash.molecule:molecule-runtime to v2.1.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4585
* fix(deps): update core to v1.16.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4564
* Upate datastore to 1.1.4 by @bmarty in https://github.com/element-hq/element-x-android/pull/4551
* fix(deps): update media3 to v1.6.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4592
* chore(deps): update danger/danger-js action to v13 by @renovate in https://github.com/element-hq/element-x-android/pull/4596
* fix(deps): update dependency io.element.android:emojibase-bindings to v1.4.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4591
* fix(deps): update dagger to v2.56.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4603
* fix(deps): update dependency io.sentry:sentry-android to v8.8.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4557
* fix(deps): update dependency androidx.compose:compose-bom to v2025.04.00 - autoclosed by @renovate in https://github.com/element-hq/element-x-android/pull/4565
* fix(deps): update dependency com.posthog:posthog-android to v3.14.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4616
* fix(deps): update android.gradle.plugin to v8.9.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4615
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.4.22 by @renovate in https://github.com/element-hq/element-x-android/pull/4622
### Others
* Improve accessibility of the timeline by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4579
* Push: improve Push history screen, log and stored data by @bmarty in https://github.com/element-hq/element-x-android/pull/4601
* Push gateway config by @bmarty in https://github.com/element-hq/element-x-android/pull/4608


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.04.2...v25.04.3

Changes in Element X v25.04.2
=============================

Security fixes 🔐
-----------------
- Fix for [GHSA-m5px-pwq3-4p5m](https://github.com/element-hq/element-x-android/security/advisories/GHSA-m5px-pwq3-4p5m) / [CVE-2025-27599](https://www.cve.org/CVERecord?id=CVE-2025-27599)

Changes in Element X v25.04.1
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.04.1 -->

## What's Changed
### ✨ Features
* Introduce PushHistoryService to store data about the received push by @bmarty in https://github.com/element-hq/element-x-android/pull/4573
### 🙌 Improvements
* change (preferences) : new moderation and safety settings by @ganfra in https://github.com/element-hq/element-x-android/pull/4574
### 🐛 Bugfixes
* Ensure that we have only one single instance of SeenInviteStore per session by @bmarty in https://github.com/element-hq/element-x-android/pull/4577
### Dependency upgrades
* fix(deps): update dependencyanalysis to v2.15.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4558
* fix(deps): update dependency io.mockk:mockk to v1.14.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4562
* fix(deps): update kotlin by @renovate in https://github.com/element-hq/element-x-android/pull/4552
* fix(deps): update dependency org.maplibre.gl:android-sdk to v11.8.6 by @renovate in https://github.com/element-hq/element-x-android/pull/4567
* fix(deps): update dependencyanalysis to v2.16.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4575


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.04.0...v25.04.1

Changes in Element X v25.04.0
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.04.0 -->

## What's Changed
### ✨ Features
* Enable Rust trace log packs by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4514
* Allow using a hardware keyboard to unlock the app using a pin code by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4530
### 🙌 Improvements
* Change (mention span) : rework and add more cases by @ganfra in https://github.com/element-hq/element-x-android/pull/4476
* Add kick (remove) confirmation and reason by @bmarty in https://github.com/element-hq/element-x-android/pull/4507
* Remove the green badge on a pending invite after a first preview by @bmarty in https://github.com/element-hq/element-x-android/pull/4532
### 🐛 Bugfixes
* Improve touch indicators for the user info UI in the timeline by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4482
* Limit the text length in the 'in reply to' preview by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4491
* Timeline header: ensure that the decoration is clickable by @bmarty in https://github.com/element-hq/element-x-android/pull/4495
* Add video autoplay to media gallery by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4499
* Add `WakeLock` to dismiss ringing call screen when call is cancelled by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4478
* Make sure the live timeline is destroyed before clearing a room's cache by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4515
* Fix bullet points not having leading margin on timeline items by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4536
* Fix the share location URI by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4544
* Add a inderminate progress bar when loging out and in Waiting state. by @bmarty in https://github.com/element-hq/element-x-android/pull/4538
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4506
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4543
### 🧱 Build
* Element config by @bmarty in https://github.com/element-hq/element-x-android/pull/4471
* Check if Manifest.permission.REQUEST_INSTALL_PACKAGES is in the manifest by @bmarty in https://github.com/element-hq/element-x-android/pull/4490
* Remove nightly_enterprise.yml. by @bmarty in https://github.com/element-hq/element-x-android/pull/4492
* Log the packageId which is currently built. by @bmarty in https://github.com/element-hq/element-x-android/pull/4494
* Use handy buildConfigFieldStr. by @bmarty in https://github.com/element-hq/element-x-android/pull/4501
* Fix warnings in InMemoryAppPreferencesStore by @bmarty in https://github.com/element-hq/element-x-android/pull/4523
### Dependency upgrades
* fix(deps): update camera to v1.4.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4483
* fix(deps): update dependency org.maplibre.gl:android-sdk to v11.8.5 by @renovate in https://github.com/element-hq/element-x-android/pull/4487
* fix(deps): update dependency com.posthog:posthog-android to v3.13.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4469
* fix(deps): update dependency androidx.compose:compose-bom to v2025.03.01 by @renovate in https://github.com/element-hq/element-x-android/pull/4484
* fix(deps): update dependencyanalysis to v2.13.3 by @renovate in https://github.com/element-hq/element-x-android/pull/4493
* fix(deps): update media3 to v1.6.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4488
* fix(deps): update dependency io.element.android:element-call-embedded to v0.9.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4498
* fix(deps): update dependency com.google.firebase:firebase-bom to v33.12.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4508
* fix(deps): update dependency com.posthog:posthog-android to v3.13.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4516
* fix(deps): update dependency io.sentry:sentry-android to v8.6.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4509
* fix(deps): update kotlin by @renovate in https://github.com/element-hq/element-x-android/pull/4444
* fix(deps): update kotlin by @renovate in https://github.com/element-hq/element-x-android/pull/4522
* fix(deps): update dependencyanalysis to v2.14.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4527
* fix(deps): update dependency io.element.android:compound-android to v25.4.4 by @renovate in https://github.com/element-hq/element-x-android/pull/4537
* chore(deps): update plugin dependencycheck to v12.1.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4540
* fix(deps): update appyx to v1.7.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4547
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.4.7 by @renovate in https://github.com/element-hq/element-x-android/pull/4548
### Others
* Update screenshots by @bmarty in https://github.com/element-hq/element-x-android/pull/4497
* Update store description. by @bmarty in https://github.com/element-hq/element-x-android/pull/4496
* Improve TextFieldDialog by @bmarty in https://github.com/element-hq/element-x-android/pull/4512
* Make `RustMatrixClient.close` asynchronous by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4513
* Replace OutlinedTextField by our TextField by @bmarty in https://github.com/element-hq/element-x-android/pull/4521
* Remove alias from room invite item by @bmarty in https://github.com/element-hq/element-x-android/pull/4531
* Remember flows by @bmarty in https://github.com/element-hq/element-x-android/pull/4533
* Use colors from compound for badges by @bmarty in https://github.com/element-hq/element-x-android/pull/4545
* Update app icon by @bmarty in https://github.com/element-hq/element-x-android/pull/4534
* Click on userId / room alias to copy value to clipboard. by @bmarty in https://github.com/element-hq/element-x-android/pull/4549
* Run the 'prevent blocked' workflow even if PR has conflicts by @robintown in https://github.com/element-hq/element-x-android/pull/4432
* Update wording for push provider support test. (#4079) by @bmarty in https://github.com/element-hq/element-x-android/pull/4553


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.03.4...v25.04.0

Changes in Element X v25.03.4
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.03.4 -->

## What's Changed
### 🙌 Improvements
* Change : composer suggestions by @ganfra in https://github.com/element-hq/element-x-android/pull/4485
### 🧱 Build
* Fix flaky incoming verification tests by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4479
### Dependency upgrades
* fix(deps): update dagger to v2.56.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4472
* fix(deps): update dependencyanalysis to v2.13.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4473
* Upgrade embedded EC version to `v0.9.0-rc.4` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4489


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.03.3...v25.03.4

Changes in Element X v25.03.3
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.03.3 -->

## What's Changed
### ✨ Features
* Add 'unencrypted room' badges and labels by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4445
* Use embedded version of Element Call by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4470
### 🐛 Bugfixes
* Fix 'unverified session' flow displayed when creating account by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4467
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4461
### 🧱 Build
* Let element enterprise be able to configure id for mapTiler. by @bmarty in https://github.com/element-hq/element-x-android/pull/4446
### Dependency upgrades
* chore(deps): update rnkdsh/action-upload-diawi action to v1.5.8 by @renovate in https://github.com/element-hq/element-x-android/pull/4457
* chore(deps): update plugin licensee to v1.13.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4447
* fix(deps): update dependency org.maplibre.gl:android-sdk to v11.8.4 by @renovate in https://github.com/element-hq/element-x-android/pull/4450
* fix(deps): update dependency com.google.firebase:firebase-bom to v33.11.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4448
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25.3.24 by @renovate in https://github.com/element-hq/element-x-android/pull/4394
* fix(deps): update dependencyanalysis to v2.13.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4464
* chore(deps): update plugin sonarqube to v6.1.0.5360 by @renovate in https://github.com/element-hq/element-x-android/pull/4468
* fix(deps): update android.gradle.plugin to v8.9.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4465
### Others
* Sync Strings - tweaks to identity change messages by @andybalaam in https://github.com/element-hq/element-x-android/pull/4454
* Check link click by @bmarty in https://github.com/element-hq/element-x-android/pull/4463


**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.03.2...v25.03.3

Changes in Element X v25.03.2
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.03.2 -->

## What's Changed
### ✨ Features
* Implement user verification by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4294
* Add user verification and verification state violation badges by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4392
* Open txt document inside the application by @bmarty in https://github.com/element-hq/element-x-android/pull/4414
* Add timeline item prefetching by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4399
### 🐛 Bugfixes
* fix(read receipt): track read receipts for focused timeline by @ganfra in https://github.com/element-hq/element-x-android/pull/4374
* Discard timed out verification requests by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4385
* Ensure the snackbar "No more media to show" is not rendered when opening the media viewer. by @bmarty in https://github.com/element-hq/element-x-android/pull/4397
* Disable click effect on Stickers by @bmarty in https://github.com/element-hq/element-x-android/pull/4401
* Ensure that a click on a media open the correct media. by @bmarty in https://github.com/element-hq/element-x-android/pull/4413
* Display user verification violation icon in DM rooms too by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4423
* Add a filter to avoid stack overflow when pressing the back button several times. by @bmarty in https://github.com/element-hq/element-x-android/pull/4430
* Make verification screens scrollable and emoji labels multiline by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4449
### 🗣 Translations
* Sync Strings - New translations in Basque by @ElementBot in https://github.com/element-hq/element-x-android/pull/4381
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4421
### 🧱 Build
* More PR checks by @bmarty in https://github.com/element-hq/element-x-android/pull/4384
* "Core Team" is a team of matrix-org. Use team "Vector Core" instead. by @bmarty in https://github.com/element-hq/element-x-android/pull/4393
* Fix warnings in tests for push provider modules by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4398
* Update Gradle Wrapper from 8.12.1 to 8.13 by @ElementBot in https://github.com/element-hq/element-x-android/pull/4308
* Revert agp to 8.8.1 by @bmarty in https://github.com/element-hq/element-x-android/pull/4451
### Dependency upgrades
* Update rnkdsh/action-upload-diawi action to v1.5.7 by @renovate in https://github.com/element-hq/element-x-android/pull/4354
* fix(deps): update dependency com.posthog:posthog-android to v3.12.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4387
* fix(deps): update dependencyanalysis to v2.11.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4395
* fix(deps): update dependency androidx.compose:compose-bom to v2025.03.00 by @renovate in https://github.com/element-hq/element-x-android/pull/4407
* fix(deps): update dependency androidx.webkit:webkit to v1.13.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4408
* fix(deps): update dependency net.java.dev.jna:jna to v5.17.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4419
* fix(deps): update dependencyanalysis to v2.12.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4409
* Add Google Tink dependency, replacing `androidx.security.crypto`  by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4405
* fix(deps): update dependency io.sentry:sentry-android to v8.4.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4411
* fix(deps): update dependency org.maplibre.gl:android-sdk to v11.8.3 by @renovate in https://github.com/element-hq/element-x-android/pull/4427
* chore(deps): update webfactory/ssh-agent action to v0.9.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4426
* fix(deps): update android.gradle.plugin to v8.9.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4320
* Update SDK version to `25.03.13` and fix breaking changes by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4406
* Update dagger to v2.56 by @renovate in https://github.com/element-hq/element-x-android/pull/4440
* Update dependency io.sentry:sentry-android to v8.5.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4433
* Update dependencyAnalysis to v2.13.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4442
* Update dependency com.google.crypto.tink:tink-android to v1.17.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4422
* deps(rust sdk) : update to 25.03.20 and fix api change by @ganfra in https://github.com/element-hq/element-x-android/pull/4452
### Others
* Migrate some icons to Compound icon by @bmarty in https://github.com/element-hq/element-x-android/pull/4375
* Long press link to copy URL to clipboard by @ShadowRZ in https://github.com/element-hq/element-x-android/pull/4376
* Use public icon from Compound by @bmarty in https://github.com/element-hq/element-x-android/pull/4386
* Be able to correctly render the UI with other colors. by @bmarty in https://github.com/element-hq/element-x-android/pull/4378
* Let EnterpriseService provides push gateways by @bmarty in https://github.com/element-hq/element-x-android/pull/4400
* Add feature flag to let the application prints logs to logcat in release builds. by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4402
* Hide "unencrypted" lock for redacted messages by @Xant3s in https://github.com/element-hq/element-x-android/pull/4410
* Hide unencrypted lock for redacted msgs by @bmarty in https://github.com/element-hq/element-x-android/pull/4429
* Clear SDK cache properly by @bmarty in https://github.com/element-hq/element-x-android/pull/4396

## New Contributors
* @ShadowRZ made their first contribution in https://github.com/element-hq/element-x-android/pull/4376
* @Xant3s made their first contribution in https://github.com/element-hq/element-x-android/pull/4410

**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.03.1...v25.03.2

Changes in Element X v25.03.1
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.03.1 -->

## What's Changed
### ✨ Features
* Enable the Event cache by default. by @bmarty in https://github.com/element-hq/element-x-android/pull/4373
### 🙌 Improvements
* change(create room) : use history visibility "invited" by @ganfra in https://github.com/element-hq/element-x-android/pull/4335
* change(room directory) : move the the room directory entry  by @ganfra in https://github.com/element-hq/element-x-android/pull/4348
* [Change] Invited state room preview by @ganfra in https://github.com/element-hq/element-x-android/pull/4353
* change(left room snackbar) : manage cancel knock and decline invite by @ganfra in https://github.com/element-hq/element-x-android/pull/4360
### 🐛 Bugfixes
* Restore manual `Client` cleanup on session logout by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4333
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4346
### 🧱 Build
* Fix typo on job name. by @bmarty in https://github.com/element-hq/element-x-android/pull/4352
### Dependency upgrades
* chore(deps): update plugin ktlint to v12.2.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4338
* fix(deps): update dependency org.maplibre.gl:android-sdk to v11.8.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4340
* fix(deps): update dependency io.mockk:mockk to v1.13.17 by @renovate in https://github.com/element-hq/element-x-android/pull/4334
* fix(deps): update kotlin to v2.1.10-1.0.31 by @renovate in https://github.com/element-hq/element-x-android/pull/4337
* fix(deps): update dependency com.google.firebase:firebase-bom to v33.10.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4339
* Migrate to coil3 by @bmarty in https://github.com/element-hq/element-x-android/pull/4347
* fix(deps): update dependency org.jsoup:jsoup to v1.19.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4351
* deps(rust sdk) : update to 25.03.05 by @ganfra in https://github.com/element-hq/element-x-android/pull/4370
* Update dependency org.matrix.rustcomponents:sdk-android to v25.3.6 by @renovate in https://github.com/element-hq/element-x-android/pull/4371
### Others
* Prevent PRs with the X-Blocked label from being merged by @robintown in https://github.com/element-hq/element-x-android/pull/4350
* Fix some icon colors by @bmarty in https://github.com/element-hq/element-x-android/pull/4365
* Remove PreferenceText, replace by ListItem. by @bmarty in https://github.com/element-hq/element-x-android/pull/4369
* Show error screens in group calls by @robintown in https://github.com/element-hq/element-x-android/pull/4297

## New Contributors
* @robintown made their first contribution in https://github.com/element-hq/element-x-android/pull/4350

**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.03.0...v25.03.1

Changes in Element X v25.03.0
=============================

<!-- Release notes generated using configuration in .github/release.yml at v25.03.0 -->

## What's Changed
### ✨ Features
* Create `SyncOrchestrator` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4176
* feature(crypto): verification violation handling and block sending by @BillCarsonFr in https://github.com/element-hq/element-x-android/pull/4126
* Update Matrix Room API and allow media swipe on pinned event only. by @bmarty in https://github.com/element-hq/element-x-android/pull/4274
* Feature : join room by address by @ganfra in https://github.com/element-hq/element-x-android/pull/4302
### 🙌 Improvements
* Change : Room Preview by @ganfra in https://github.com/element-hq/element-x-android/pull/4250
### 🐛 Bugfixes
* SyncOrchestrator: restore the initial sync step by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4242
* When an emoji is used as the 'initial' for an avatar, use the whole emoji by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4277
* Try avoiding trailing punctuation inside linkified URLs by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4214
* Preload account urls by @bmarty in https://github.com/element-hq/element-x-android/pull/4301
* Fix issues due to multiple ntfy applications with the same name. by @bmarty in https://github.com/element-hq/element-x-android/pull/4312
* Use `Settings.System.DEFAULT_RINGTONE_URI` for ringing notifications by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4310
### 🗣 Translations
* Sync Strings - New translations to turkish by @ElementBot in https://github.com/element-hq/element-x-android/pull/4253
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4298
### 🧱 Build
* Fix nightly reports by @bmarty in https://github.com/element-hq/element-x-android/pull/4235
* Fix nightly reports - next step by @bmarty in https://github.com/element-hq/element-x-android/pull/4239
* Prepare application for being configurable by @bmarty in https://github.com/element-hq/element-x-android/pull/4285
* runQualityChecks task shouldn't fail fast by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4309
* Get library ComposablePreviewScanner from maven and update to the latest version by @bmarty in https://github.com/element-hq/element-x-android/pull/4327
### Dependency upgrades
* Update dependency com.posthog:posthog-android to v3.11.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4230
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.78 by @renovate in https://github.com/element-hq/element-x-android/pull/4234
* Update dependency org.maplibre.gl:android-sdk to v11.8.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4245
* fix(deps): update dependency org.jetbrains.kotlinx:kotlinx-datetime to v0.6.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4258
* fix(deps): update dependency io.sentry:sentry-android to v8.2.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4262
* fix(deps): update telephoto to v0.15.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4270
* fix(deps): update dependency com.google.firebase:firebase-bom to v33.9.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4249
* chore(deps): update danger/danger-js action to v12.3.4 by @renovate in https://github.com/element-hq/element-x-android/pull/4259
* fix(deps): update android.gradle.plugin to v8.8.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4263
* chore(deps): update plugin dependencycheck to v12.1.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4272
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v25 by @renovate in https://github.com/element-hq/element-x-android/pull/4273
* fix(deps): update dependency androidx.compose:compose-bom to v2025.02.00 by @renovate in https://github.com/element-hq/element-x-android/pull/4261
* fix(deps): update kotlin to v2.1.10-1.0.30 by @renovate in https://github.com/element-hq/element-x-android/pull/4265
* fix(deps): update dependency io.github.zxing-cpp:android to v2.3.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4282
* fix(deps): update firebaseappdistribution to v5.1.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4246
* fix(deps): update dependencyanalysis to v2.8.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4251
* fix(deps): update dependency com.google.accompanist:accompanist-permissions to v0.37.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4283
* fix(deps): update dependency com.google.accompanist:accompanist-permissions to v0.37.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4287
* fix(deps): update dependencyanalysis to v2.10.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4288
* fix(deps): update dependencyanalysis to v2.10.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4295
* Upgrade SDK version to 25.02.26 by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4305
* fix(deps): update kotlinpoet to v2.1.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4304
* Update compound by @bmarty in https://github.com/element-hq/element-x-android/pull/4319
* fix(deps): update dependency androidx.constraintlayout:constraintlayout-compose to v1.1.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4324
* fix(deps): update activity to v1.10.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4321
* fix(deps): update dependency androidx.exifinterface:exifinterface to v1.4.0 - autoclosed by @renovate in https://github.com/element-hq/element-x-android/pull/4325
* fix(deps): update dependency androidx.constraintlayout:constraintlayout to v2.2.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4322
* fix(deps): update dependency io.sentry:sentry-android to v8.3.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4316
* fix(deps): update dependency com.posthog:posthog-android to v3.11.3 by @renovate in https://github.com/element-hq/element-x-android/pull/4313
* fix(deps): update dependency com.android.tools:desugar_jdk_libs to v2.1.5 by @renovate in https://github.com/element-hq/element-x-android/pull/4299
* chore(deps): update plugin detekt to v1.23.8 by @renovate in https://github.com/element-hq/element-x-android/pull/4292
### Others
* Update incoming call notification content to "📹 Incoming call" by @bmarty in https://github.com/element-hq/element-x-android/pull/4231
* Display a bottom sheet to let user confirm the DM creation by @bmarty in https://github.com/element-hq/element-x-android/pull/4233
* Open chat links in regular browser tabs by @cbs228 in https://github.com/element-hq/element-x-android/pull/4198
* Theme override by @bmarty in https://github.com/element-hq/element-x-android/pull/4226
* Allow user certificate in production builds. by @bmarty in https://github.com/element-hq/element-x-android/pull/4275
* Replace Material icons with Compound icons wherever it's possible by @bmarty in https://github.com/element-hq/element-x-android/pull/4323

## New Contributors
* @cbs228 made their first contribution in https://github.com/element-hq/element-x-android/pull/4198

**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v25.02.0...v25.03.0

Changes in Element X v25.02.0 (2025-02-04)
==========================================

<!-- Release notes generated using configuration in .github/release.yml at v25.02.0 -->

## What's Changed
### ✨ Features
* Media navigation with swipe gesture by @bmarty in https://github.com/element-hq/element-x-android/pull/4161
* Add ability to swipe between media when opened from the timeline. by @bmarty in https://github.com/element-hq/element-x-android/pull/4205
### 🙌 Improvements
* change(design) : use ElementTheme.typography.fontBodyLgMedium by @ganfra in https://github.com/element-hq/element-x-android/pull/4145
* change(design) : New component Announcement by @ganfra in https://github.com/element-hq/element-x-android/pull/4140
* update rust sdk 0.2.75 by @ganfra in https://github.com/element-hq/element-x-android/pull/4158
### 🐛 Bugfixes
* Fix dm avatar rtl by @bmarty in https://github.com/element-hq/element-x-android/pull/4103
* Unified push gateway resolver improvement by @bmarty in https://github.com/element-hq/element-x-android/pull/4101
* Close the media preview screen ASAP with sending queue enabled by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4089
* fix(coroutine) : make sure to switch coroutine context by @ganfra in https://github.com/element-hq/element-x-android/pull/4146
* Fix snack bar not displayed in MediaViewer by @bmarty in https://github.com/element-hq/element-x-android/pull/4195
* Let the SDK provide the "network is available information"  by @bmarty in https://github.com/element-hq/element-x-android/pull/4215
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4088
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4100
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4114
* Fix import of en-US translations. by @bmarty in https://github.com/element-hq/element-x-android/pull/4135
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4139
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4172
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4199
* Sync Strings - new (partial) language: Norwegian by @ElementBot in https://github.com/element-hq/element-x-android/pull/4227
### 🧱 Build
* Update Gradle Wrapper from 8.11.1 to 8.12 by @ElementBot in https://github.com/element-hq/element-x-android/pull/4085
* Test using Maestro CLI + emulator instead of Cloud by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4092
* Make Maestro run for each PR push by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4121
* Migrate to CalVer like versioning by @bmarty in https://github.com/element-hq/element-x-android/pull/4187
* Kover: include back :libraries:matrix:impl module. by @bmarty in https://github.com/element-hq/element-x-android/pull/4193
* Update Gradle Wrapper from 8.12 to 8.12.1 by @ElementBot in https://github.com/element-hq/element-x-android/pull/4196
* Use secret Sentry DSN value by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4210
* Use Sentry breadcrumbs instead of logging new events by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4223
### 🚧 In development 🚧
* Media Viewer: show snackbar when reaching end of timeline. by @bmarty in https://github.com/element-hq/element-x-android/pull/4201
* Feature : room settings - security and privacy by @ganfra in https://github.com/element-hq/element-x-android/pull/4212
### Dependency upgrades
* Update dependency io.mockk:mockk to v1.13.14 by @renovate in https://github.com/element-hq/element-x-android/pull/4083
* Update dependency net.java.dev.jna:jna to v5.16.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4087
* Update kotlin to v1.10.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4073
* Update dagger to v2.54 by @renovate in https://github.com/element-hq/element-x-android/pull/4084
* Update dependency io.sentry:sentry-android to v7.19.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4090
* Update dependency com.android.tools:desugar_jdk_libs to v2.1.4 by @renovate in https://github.com/element-hq/element-x-android/pull/4077
* Update dependency com.posthog:posthog-android to v3.10.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4120
* Update appyx to v1.6.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4129
* Update dagger to v2.55 by @renovate in https://github.com/element-hq/element-x-android/pull/4131
* Update android.gradle.plugin to v8.8.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4130
* Update dependency org.maplibre.gl:android-sdk to v11.8.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4132
* Update dependency io.mockk:mockk to v1.13.16 by @renovate in https://github.com/element-hq/element-x-android/pull/4134
* Update dependencyAnalysis to v2.7.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4136
* Update anvil to v0.4.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4144
* Update kotlin by @renovate in https://github.com/element-hq/element-x-android/pull/4117
* Update plugin dependencycheck to v12 by @renovate in https://github.com/element-hq/element-x-android/pull/4137
* Update dependency io.sentry:sentry-android to v7.20.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4107
* Update wysiwyg to v2.38.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4104
* Update dependency androidx.recyclerview:recyclerview to v1.4.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4154
* Update activity to v1.10.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4152
* Update firebaseAppDistribution to v5.1.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4159
* Update dependency com.google.firebase:firebase-bom to v33.8.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4160
* Update dependency androidx.compose:compose-bom to v2025 by @renovate in https://github.com/element-hq/element-x-android/pull/4155
* Update dependency io.sentry:sentry-android to v7.20.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4178
* Update dependency io.sentry:sentry-android to v8 by @renovate in https://github.com/element-hq/element-x-android/pull/4180
* Update wysiwyg to v2.38.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4177
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.76 by @renovate in https://github.com/element-hq/element-x-android/pull/4183
* Update wysiwyg to v2.38.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4186
* Update dependency com.posthog:posthog-android to v3.11.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4204
* Update kotlin by @renovate in https://github.com/element-hq/element-x-android/pull/4200
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.77 by @renovate in https://github.com/element-hq/element-x-android/pull/4228
* Update dependency com.posthog:posthog-android to v3.11.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4222
* Update dependency io.element.android:emojibase-bindings to v1.3.4 by @renovate in https://github.com/element-hq/element-x-android/pull/4213
* Update dependencyAnalysis to v2.8.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4218
* Update dependency androidx.compose:compose-bom to v2025.01.01 by @renovate in https://github.com/element-hq/element-x-android/pull/4217
* Update dependency io.sentry:sentry-android to v8.1.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4221
* Update rnkdsh/action-upload-diawi action to v1.5.6 by @renovate in https://github.com/element-hq/element-x-android/pull/4173
* Update plugin dependencycheck to v12.0.2 by @renovate in https://github.com/element-hq/element-x-android/pull/4170
### Others
* Improve gallery loading state by @bmarty in https://github.com/element-hq/element-x-android/pull/4080
* Show more detail about the error when pusher registration fails. by @bmarty in https://github.com/element-hq/element-x-android/pull/4081
* Update pull request template and CI automation by @bmarty in https://github.com/element-hq/element-x-android/pull/4037
* Add a log function for handling complex values to the WebView client. by @Half-Shot in https://github.com/element-hq/element-x-android/pull/4098
* design : CounterAtom  by @ganfra in https://github.com/element-hq/element-x-android/pull/4108
* Change sticker mimetype fallback to image by @surakin in https://github.com/element-hq/element-x-android/pull/4111
* Dual licensing: AGPL + Element Commercial by @bmarty in https://github.com/element-hq/element-x-android/pull/4118
* Replace the InfoListOrganism default bg color by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4091
* Ignore dependency that are not third-party licenses to us. by @bmarty in https://github.com/element-hq/element-x-android/pull/4122
* misc(send queue) : do not disable send queue when Network is Offline by @ganfra in https://github.com/element-hq/element-x-android/pull/4105
* Remove or replace unnecessary `BackHandler` calls by @jmartinesp in https://github.com/element-hq/element-x-android/pull/4148
* Replace our firstIfSingle extension with singleOrNull from the Kotlin library by @bmarty in https://github.com/element-hq/element-x-android/pull/4184
* Remove log. by @bmarty in https://github.com/element-hq/element-x-android/pull/4203
* Remove unused types / code. by @bmarty in https://github.com/element-hq/element-x-android/pull/4185
* Consider that the topic of a room has been removed when it's blank. by @bmarty in https://github.com/element-hq/element-x-android/pull/4209
* CalVer: use 2 digits for the year and 2 digits for the month. by @bmarty in https://github.com/element-hq/element-x-android/pull/4192
* Always display encryption badge by @bmarty in https://github.com/element-hq/element-x-android/pull/4219

## New Contributors
* @Half-Shot made their first contribution in https://github.com/element-hq/element-x-android/pull/4098

**Full Changelog**: https://github.com/element-hq/element-x-android/compare/v0.7.6...v25.02.0

Changes in Element X v0.7.6 (2024-12-20)
========================================

## What's Changed
### ✨ Features
* Media gallery UI by @bmarty in https://github.com/element-hq/element-x-android/pull/4010
* Render audio file in the files list and improve media viewer for audio/voice files by @bmarty in https://github.com/element-hq/element-x-android/pull/4031
* Media gallery UI update by @bmarty in https://github.com/element-hq/element-x-android/pull/4071
### 🙌 Improvements
* Support new properties in posthog UTD reports by @richvdh in https://github.com/element-hq/element-x-android/pull/4020
### 🐛 Bugfixes
* fix(dm) : remove duplicate LaunchedEffect when opening DM by @ganfra in https://github.com/element-hq/element-x-android/pull/4012
* Always attempt to start the sync when starting the application. by @bmarty in https://github.com/element-hq/element-x-android/pull/4069
* Fix rendering issue in the toolbar. by @bmarty in https://github.com/element-hq/element-x-android/pull/4075
* fix(timeline) : dispatch timeline creations trying to avoid ANRs by @ganfra in https://github.com/element-hq/element-x-android/pull/4076
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4007
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/4043
* Add Accept-Language to extra header when opening CustomChromeTab by @bmarty in https://github.com/element-hq/element-x-android/pull/4051
### 🧱 Build
* Update Gradle Wrapper from 8.10.2 to 8.11.1 by @ElementBot in https://github.com/element-hq/element-x-android/pull/4019
### 📄 Documentation
* [Doc] Improve instructions for building Rust SDK locally by @richvdh in https://github.com/element-hq/element-x-android/pull/4015
* Build SDK for the local hardware by @richvdh in https://github.com/element-hq/element-x-android/pull/4021
### 🚧 In development 🚧
* feat(knock_requests_list) : implement design by @ganfra in https://github.com/element-hq/element-x-android/pull/3995
* feat(knock) : Knock Requests Banner UI by @ganfra in https://github.com/element-hq/element-x-android/pull/4005
* Add a feature flag to be able to enable the event cache by @bmarty in https://github.com/element-hq/element-x-android/pull/4029
* Improve title and subtitle for empty states in the gallery. by @bmarty in https://github.com/element-hq/element-x-android/pull/4038
* Inline voice message player in the files gallery. by @bmarty in https://github.com/element-hq/element-x-android/pull/4045
* Media gallery update by @bmarty in https://github.com/element-hq/element-x-android/pull/4059
* feat(knock requests) : branch logic for handling knock requests by @ganfra in https://github.com/element-hq/element-x-android/pull/4067
### Dependency upgrades
* Update dependency io.sentry:sentry-android to v7.18.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3972
* Update dependency com.google.firebase:firebase-bom to v33.7.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4001
* Update nschloe/action-cached-lfs-checkout action to v1.2.3 by @renovate in https://github.com/element-hq/element-x-android/pull/4017
* Update dependency com.posthog:posthog-android to v3.9.3 by @renovate in https://github.com/element-hq/element-x-android/pull/3960
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.70 by @renovate in https://github.com/element-hq/element-x-android/pull/4018
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.71 by @renovate in https://github.com/element-hq/element-x-android/pull/4024
* Update camera to v1.4.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4022
* Update dependency org.maplibre.gl:android-sdk to v11.7.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4028
* Update dependency io.nlopez.compose.rules:detekt to v0.4.22 by @renovate in https://github.com/element-hq/element-x-android/pull/4016
* Update dependencyAnalysis to v2.6.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3996
* Update kotlin by @renovate in https://github.com/element-hq/element-x-android/pull/3955
* Update dependency org.jsoup:jsoup to v1.18.3 by @renovate in https://github.com/element-hq/element-x-android/pull/3951
* Update dagger to v2.53.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4013
* Update dependency io.sentry:sentry-android to v7.19.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4030
* Update dependency org.jetbrains.kotlinx:kover-gradle-plugin to v0.9.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4032
* Update dependencyAnalysis to v2.6.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4041
* Update dependency androidx.compose:compose-bom to v2024.12.01 by @renovate in https://github.com/element-hq/element-x-android/pull/4023
* Update android.gradle.plugin to v8.7.3 by @renovate in https://github.com/element-hq/element-x-android/pull/3982
* Update dependency com.lemonappdev:konsist to v0.17.3 by @renovate in https://github.com/element-hq/element-x-android/pull/3997
* Update dependency com.google.accompanist:accompanist-permissions to v0.37.0 by @renovate in https://github.com/element-hq/element-x-android/pull/4035
* depencies(sdk) : update rust sdk 0.2.72 by @ganfra in https://github.com/element-hq/element-x-android/pull/4060
* Update dependency org.maplibre.gl:android-sdk to v11.7.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4066
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.73 by @renovate in https://github.com/element-hq/element-x-android/pull/4070
* Update media3 to v1.5.1 by @renovate in https://github.com/element-hq/element-x-android/pull/4072
### Others
* Add destructive param to BigIcon.Style.Default to be able to render icons with red tint by @bmarty in https://github.com/element-hq/element-x-android/pull/4004
* UI: knock avatars by @bmarty in https://github.com/element-hq/element-x-android/pull/4014
* Implement month separator for the Gallery, and improve date rendering. by @bmarty in https://github.com/element-hq/element-x-android/pull/4026
* Extract voice message player to its own module by @bmarty in https://github.com/element-hq/element-x-android/pull/4036
* Add a quick filter on the open source licence screen. by @bmarty in https://github.com/element-hq/element-x-android/pull/4052
* Make the room filter use normalized strings. by @bmarty in https://github.com/element-hq/element-x-android/pull/4050
* Add test on DefaultMediaPlayer. by @bmarty in https://github.com/element-hq/element-x-android/pull/4054
* Fix flaky test by using CompletableDeferred by @bmarty in https://github.com/element-hq/element-x-android/pull/4057
* feat(crypto): Support for new UtdCause for historical messages by @BillCarsonFr in https://github.com/element-hq/element-x-android/pull/4044
* Update message action list by @bmarty in https://github.com/element-hq/element-x-android/pull/4056
* Update recovery key UI by @bmarty in https://github.com/element-hq/element-x-android/pull/4065
* Fix gallery title by @bmarty in https://github.com/element-hq/element-x-android/pull/4078

Changes in Element X v0.7.5 (2024-12-06)
========================================

## What's Changed
### ✨ Features
* Allow to set caption when uploading file and audio files, and allow adding / edit / remove caption on Event with attachment (also works on local echo) by @bmarty in https://github.com/element-hq/element-x-android/pull/3902
* Enable all notification actions: quick reply, accept/decline invite, mark as read from notification. by @bmarty in https://github.com/element-hq/element-x-android/pull/3916
* Video player controller by @bmarty in https://github.com/element-hq/element-x-android/pull/3959
### 🙌 Improvements
* change : confirm biometric before allowing biometric unlock. by @ganfra in https://github.com/element-hq/element-x-android/pull/3930
* Hide media preprocessing by @bmarty in https://github.com/element-hq/element-x-android/pull/3943
* changes: iterate on room create screen  by @ganfra in https://github.com/element-hq/element-x-android/pull/3966
* change : knock message supporting text display number of characters by @ganfra in https://github.com/element-hq/element-x-android/pull/3970
* feat(design) : update send button background by @ganfra in https://github.com/element-hq/element-x-android/pull/4000
### 🐛 Bugfixes
* Min size for hidden media by @bmarty in https://github.com/element-hq/element-x-android/pull/3906
* fix : use RoomMembershipObserver to close room screen when leaving by @ganfra in https://github.com/element-hq/element-x-android/pull/3887
* fix : protect some usages of client to avoid crashes by @bmarty in https://github.com/element-hq/element-x-android/pull/3886
* Fix long click not working on pinned events timeline by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3940
* Element Call: display error dialog only when loading the main URL by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3962
* Fix navigation issue when entering recovery key after navigating from the banner by @bmarty in https://github.com/element-hq/element-x-android/pull/3961
* navigation : clear backstack when opening room from outer node by @ganfra in https://github.com/element-hq/element-x-android/pull/3984
* fix : hide keyboard when TextComposer is removed from composition by @ganfra in https://github.com/element-hq/element-x-android/pull/3985
* fix(room_preview) : catch all exception instead by @ganfra in https://github.com/element-hq/element-x-android/pull/3989
* fix(room_detail) : hide room avatar preview by @ganfra in https://github.com/element-hq/element-x-android/pull/3992
* fix(composer) : use HideKeyboardWhenDisposed only in MessagesView by @ganfra in https://github.com/element-hq/element-x-android/pull/3993
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3936
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3975
### Dependency upgrades
* Update dependency io.sentry:sentry-android to v7.18.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3891
* Update plugin sonarqube to v6 - autoclosed by @renovate in https://github.com/element-hq/element-x-android/pull/3895
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.64 by @renovate in https://github.com/element-hq/element-x-android/pull/3907
* Update dependency com.autonomousapps.dependency-analysis to v2.5.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3909
* Update dependency org.robolectric:robolectric to v4.14.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3924
* Update dependency io.element.android:compound-android to v0.2.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3915
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.65 by @renovate in https://github.com/element-hq/element-x-android/pull/3932
* Update media3 to v1.5.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3942
* Update plugin ktlint to v12.1.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3944
* Update wysiwyg to v2.37.14 by @renovate in https://github.com/element-hq/element-x-android/pull/3948
* Update mobile-dev-inc/action-maestro-cloud action to v1.9.7 by @renovate in https://github.com/element-hq/element-x-android/pull/3914
* Update dependency com.lemonappdev:konsist to v0.17.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3947
* deps : update rust sdk to 0.2.67 and fix breaking changes by @ganfra in https://github.com/element-hq/element-x-android/pull/3957
* Update dependency com.lemonappdev:konsist to v0.17.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3983
* Update plugin sonarqube to v6.0.1.5171 by @renovate in https://github.com/element-hq/element-x-android/pull/3958
* Update dagger to v2.53 by @renovate in https://github.com/element-hq/element-x-android/pull/3986
* Update dependency com.sigpwned:emoji4j-core to v16 by @renovate in https://github.com/element-hq/element-x-android/pull/3899
* dependencies : update rust sdk to 0.2.68 by @ganfra in https://github.com/element-hq/element-x-android/pull/3988
* Update plugin dependencycheck to v11.1.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3994
* chore(dependencies) : update rust sdk to 0.2.69  by @ganfra in https://github.com/element-hq/element-x-android/pull/3999
### Others
* Send button iteration by @bmarty in https://github.com/element-hq/element-x-android/pull/3901
* Fix photo / video name by @bmarty in https://github.com/element-hq/element-x-android/pull/3903
* Render edited caption. by @bmarty in https://github.com/element-hq/element-x-android/pull/3904
* Rely on the SDK to decide if a caption is editable or not by @bmarty in https://github.com/element-hq/element-x-android/pull/3917
* Remove AttachmentsState and use the MessagesNavigator by @bmarty in https://github.com/element-hq/element-x-android/pull/3918
* Fix element call crash when resuming from notification by @bmarty in https://github.com/element-hq/element-x-android/pull/3926
* Ensure that the SDK is syncing during an incoming call so that the app can cancel the notification by @bmarty in https://github.com/element-hq/element-x-android/pull/3931
* Add feature flag to temporary disable sending caption by default in production by @bmarty in https://github.com/element-hq/element-x-android/pull/3953
* Add timeline action item to copy caption by @bmarty in https://github.com/element-hq/element-x-android/pull/3963
* Fix wrong name of classes and method by @bmarty in https://github.com/element-hq/element-x-android/pull/3971
* Rework on media module by @bmarty in https://github.com/element-hq/element-x-android/pull/3967
* Add warning when adding a caption. by @bmarty in https://github.com/element-hq/element-x-android/pull/3977
* Do not auto-play videos. by @bmarty in https://github.com/element-hq/element-x-android/pull/3978
* MediaViewer: iterate on design by @bmarty in https://github.com/element-hq/element-x-android/pull/3979
* feat(crypto): Support new expected UTD causes UX + Analytics by @BillCarsonFr in https://github.com/element-hq/element-x-android/pull/3980
* increase ringing timeout from 15 seconds to 90 seconds by @fkwp in https://github.com/element-hq/element-x-android/pull/3991
* MediaViewer: Align title to left and move action bottom to top bar. by @bmarty in https://github.com/element-hq/element-x-android/pull/4003

Changes in Element X v0.7.4 (2024-11-20)
========================================

## What's Changed
### 🙌 Improvements
* Update the strings for unsupported calls by @bmarty in https://github.com/element-hq/element-x-android/pull/3857
### 🐛 Bugfixes
* Stop incoming call ringing if answered on another device. by @bmarty in https://github.com/element-hq/element-x-android/pull/3842
* Use formatted captions for images and video by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3864
* Fix unified push unregister by @bmarty in https://github.com/element-hq/element-x-android/pull/3877
* Hide the keyboard when navigating from the chat room screen by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3878
* Fix long click not working for media timeline items by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3879
* Instantiate the verification controller ASAP by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3893
* fix : display security banner for room list empty state by @ganfra in https://github.com/element-hq/element-x-android/pull/3892
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3852
* Sync Strings - add translations to Finnish by @ElementBot in https://github.com/element-hq/element-x-android/pull/3883
### 🚧 In development 🚧
* Create room : improve handling of room address by @ganfra in https://github.com/element-hq/element-x-android/pull/3868
### Dependency upgrades
* Update anvil to v0.4.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3792
* Update kotlin to v2.0.21-1.0.27 by @renovate in https://github.com/element-hq/element-x-android/pull/3836
* Update dependency org.maplibre.gl:android-sdk to v11.6.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3793
* Update android.gradle.plugin to v8.7.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3785
* Update lifecycle to v2.8.7 by @renovate in https://github.com/element-hq/element-x-android/pull/3763
* Update plugin dependencycheck to v11 by @renovate in https://github.com/element-hq/element-x-android/pull/3723
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.61 by @renovate in https://github.com/element-hq/element-x-android/pull/3841
* Update mobile-dev-inc/action-maestro-cloud action to v1.9.6 by @renovate in https://github.com/element-hq/element-x-android/pull/3846
* Update dependency com.posthog:posthog-android to v3.9.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3856
* Update core to v1.15.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3766
* Update dependency com.android.tools:desugar_jdk_libs to v2.1.3 by @renovate in https://github.com/element-hq/element-x-android/pull/3825
* Update dependency io.nlopez.compose.rules:detekt to v0.4.18 by @renovate in https://github.com/element-hq/element-x-android/pull/3860
* Update dependency com.posthog:posthog-android to v3.9.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3861
* Update dependency io.sentry:sentry-android to v7.17.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3862
* Update dependency androidx.compose:compose-bom to v2024.11.00 by @renovate in https://github.com/element-hq/element-x-android/pull/3869
* Update telephoto to v0.14.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3870
* Update SDK bindings version to `0.2.62` and fix `SendHandle` usages by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3876
* Update codecov/codecov-action action to v5 by @renovate in https://github.com/element-hq/element-x-android/pull/3874
* Update dependency com.google.firebase:firebase-bom to v33.6.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3880
* Update kotlin to v2.0.21-1.0.28 by @renovate in https://github.com/element-hq/element-x-android/pull/3881
* Update dependency org.robolectric:robolectric to v4.14 by @renovate in https://github.com/element-hq/element-x-android/pull/3882
* Update appyx to v1.5.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3889
* Update dependency io.nlopez.compose.rules:detekt to v0.4.19 by @renovate in https://github.com/element-hq/element-x-android/pull/3900
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.63 by @renovate in https://github.com/element-hq/element-x-android/pull/3898
### Others
* Design system : implement new TextField by @ganfra in https://github.com/element-hq/element-x-android/pull/3834
* Remove :samples:minimal module by @bmarty in https://github.com/element-hq/element-x-android/pull/3871
* Replace `textPlaceholder` color usages with `textSecondary` by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3873
* Room Preview API changes by @ganfra in https://github.com/element-hq/element-x-android/pull/3875

Changes in Element X v0.7.3 (2024-11-08)
========================================

## What's Changed
### ✨ Features
* Incoming session verification by @bmarty in https://github.com/element-hq/element-x-android/pull/3733
* Remove all GPS metadata from images uploaded as media by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3781
* Send caption with image and video by @bmarty in https://github.com/element-hq/element-x-android/pull/3803
### 🙌 Improvements
* UI iteration on the encryption settings by @bmarty in https://github.com/element-hq/element-x-android/pull/3750
* Rotate firebase token in case of error by @bmarty in https://github.com/element-hq/element-x-android/pull/3755
* Optimize media upload by @bmarty in https://github.com/element-hq/element-x-android/pull/3779
* Iteration on caption by @bmarty in https://github.com/element-hq/element-x-android/pull/3816
* Hide join call button when the user is already in the call by @bmarty in https://github.com/element-hq/element-x-android/pull/3815
* Disable button during the "verifying" step. by @bmarty in https://github.com/element-hq/element-x-android/pull/3832
### 🐛 Bugfixes
* Fix oversize padding on captioned images/videos by @frebib in https://github.com/element-hq/element-x-android/pull/3732
* Fix the onboarding flow getting stuck in some cases by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3778
* bugfix: do not remove logs after sending them by @ganfra in https://github.com/element-hq/element-x-android/pull/3780
* Use in-memory thumbnail APIs when possible by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3817
* ElementCall: allow user to switch to another call. by @bmarty in https://github.com/element-hq/element-x-android/pull/3833
* Do not delete the original file if it's not a temporary file when sending it to a room. by @bmarty in https://github.com/element-hq/element-x-android/pull/3819
* Fix verification failed issue, simplify verification logic by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3830
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3798
### 🧱 Build
* Target api 35 by @bmarty in https://github.com/element-hq/element-x-android/pull/3776
### 🚧 In development 🚧
* Knocking : update create room flow  by @ganfra in https://github.com/element-hq/element-x-android/pull/3804
### Dependency upgrades
* Update dependency io.nlopez.compose.rules:detekt to v0.4.17 by @renovate in https://github.com/element-hq/element-x-android/pull/3746
* Update dependency com.posthog:posthog-android to v3.8.3 - autoclosed by @renovate in https://github.com/element-hq/element-x-android/pull/3742
* Update dependency org.maplibre.gl:android-plugin-annotation-v9 to v3.0.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3702
* Update dependency com.posthog:posthog-android to v3.9.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3754
* Update kotlin by @renovate in https://github.com/element-hq/element-x-android/pull/3283
* Update camera to v1.4.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3765
* Update dependencyAnalysis to v2.4.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3773
* Update kotlin to v2.0.21-1.0.26 by @renovate in https://github.com/element-hq/element-x-android/pull/3774
* Update dependency androidx.annotation:annotation-jvm to v1.9.1 - autoclosed by @renovate in https://github.com/element-hq/element-x-android/pull/3762
* chore(deps): update dependencyanalysis to v2.4.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3791
* fix(deps): update dependency androidx.compose:compose-bom to v2024.10.01 by @renovate in https://github.com/element-hq/element-x-android/pull/3782
* Update dependency androidx.constraintlayout:constraintlayout-compose to v1.1.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3770
* fix(deps): update dependency androidx.constraintlayout:constraintlayout to v2.2.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3784
* fix(deps): update dependency org.matrix.rustcomponents:sdk-android to v0.2.59 by @renovate in https://github.com/element-hq/element-x-android/pull/3809
* Update mobile-dev-inc/action-maestro-cloud action to v1.9.4 by @renovate in https://github.com/element-hq/element-x-android/pull/3820
* Update dependency com.otaliastudios:transcoder to v0.11.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3805
* Update plugin paparazzi to v1.3.5 by @renovate in https://github.com/element-hq/element-x-android/pull/3826
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.60 by @renovate in https://github.com/element-hq/element-x-android/pull/3827
### Others
* Change wording to "Verify identity" by @bmarty in https://github.com/element-hq/element-x-android/pull/3751
* Improve FakeMatrixRoom to be able to check all the parameters. by @bmarty in https://github.com/element-hq/element-x-android/pull/3761
* Editor state fixture and preview improvement by @bmarty in https://github.com/element-hq/element-x-android/pull/3758
* Enable identity pinning violation notifications unconditionally by @andybalaam in https://github.com/element-hq/element-x-android/pull/3745
* Enable predictive back gesture by @frebib in https://github.com/element-hq/element-x-android/pull/3797
* Update project status by @mxandreas in https://github.com/element-hq/element-x-android/pull/3806
* Remove code duplication - no behavior change. by @bmarty in https://github.com/element-hq/element-x-android/pull/3823
* Verification UI / UX iteration by @bmarty in https://github.com/element-hq/element-x-android/pull/3829

## New Contributors
* @andybalaam made their first contribution in https://github.com/element-hq/element-x-android/pull/3745
* @mxandreas made their first contribution in https://github.com/element-hq/element-x-android/pull/3806

Changes in Element X v0.7.2 (2024-10-29)
========================================

## What's Changed
### 🙌 Improvements
* Add setting to compress image and video by @bmarty in https://github.com/element-hq/element-x-android/pull/3744
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3743
### 🧱 Build
* Release script improvement by @bmarty in https://github.com/element-hq/element-x-android/pull/3741
### Dependency upgrades
* Update dependency org.maplibre.gl:android-sdk to v11.5.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3720
* Update dependency io.sentry:sentry-android to v7.16.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3726
* Update dependencyAnalysis to v2.3.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3740
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.58 by @renovate in https://github.com/element-hq/element-x-android/pull/3749

Changes in Element X v0.7.1 (2024-10-25)
========================================

## What's Changed
### ✨ Features
* Verified user badge by @bmarty in https://github.com/element-hq/element-x-android/pull/3718
### 🙌 Improvements
* Add userId in identity change warning banner by @bmarty in https://github.com/element-hq/element-x-android/pull/3686
* OIDC prompt by @bmarty in https://github.com/element-hq/element-x-android/pull/3694
* Bump rust-sdk version to rust-sdk 0.2.57 by @BillCarsonFr in https://github.com/element-hq/element-x-android/pull/3735
### 🐛 Bugfixes
* Refresh room summaries when date or time changes in the device by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3683
* Call: ensure that the microphone is working when the application is backgrounded. by @bmarty in https://github.com/element-hq/element-x-android/pull/3685
* RTL: ensure sender information are correctly rendered in the timeline by @bmarty in https://github.com/element-hq/element-x-android/pull/3681
* Improve composer paddings by @bmarty in https://github.com/element-hq/element-x-android/pull/3695
* UI: fix list item colors by @bmarty in https://github.com/element-hq/element-x-android/pull/3706
* Small UI iteration on pin feature. by @bmarty in https://github.com/element-hq/element-x-android/pull/3714
* Use BigIcon and fix colors by @bmarty in https://github.com/element-hq/element-x-android/pull/3719
### 🗣 Translations
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3665
* Sync Strings by @ElementBot in https://github.com/element-hq/element-x-android/pull/3713
### 🧱 Build
* Update Gradle Wrapper from 8.10 to 8.10.2 by @ElementBot in https://github.com/element-hq/element-x-android/pull/3663
* fix: import path broken in module template by @torrybr in https://github.com/element-hq/element-x-android/pull/3710
### 📄 Documentation
* Update store description by @bmarty in https://github.com/element-hq/element-x-android/pull/3680
### 🚧 In development 🚧
* Feature: knock request to join by @ganfra in https://github.com/element-hq/element-x-android/pull/3725
### Dependency upgrades
* Update anvil to v0.3.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3662
* Update dependency io.nlopez.compose.rules:detekt to v0.4.16 by @renovate in https://github.com/element-hq/element-x-android/pull/3675
* Update dependency com.posthog:posthog-android to v3.8.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3674
* Update dependency io.element.android:compound-android to v0.1.1 - Better support for RTL icons. by @renovate in https://github.com/element-hq/element-x-android/pull/3676
* Update android.gradle.plugin to v8.7.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3677
* Update dependency io.sentry:sentry-android to v7.15.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3640
* Update mobile-dev-inc/action-maestro-cloud action to v1.9.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3641
* Update plugin licensee to v1.12.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3687
* Update dependency app.cash.turbine:turbine to v1.2.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3696
* Update activity to v1.9.3 by @renovate in https://github.com/element-hq/element-x-android/pull/3697
* Update dependency androidx.compose:compose-bom to v2024.10.00 by @renovate in https://github.com/element-hq/element-x-android/pull/3699
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.55 by @renovate in https://github.com/element-hq/element-x-android/pull/3701
* Update dependencyAnalysis to v2.2.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3707
* Update anvil to v0.3.3 by @renovate in https://github.com/element-hq/element-x-android/pull/3711
* Update dependency androidx.annotation:annotation-jvm to v1.9.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3698
* Update dependency com.google.firebase:firebase-bom to v33.5.0 by @renovate in https://github.com/element-hq/element-x-android/pull/3716
* Update dependency org.matrix.rustcomponents:sdk-android to v0.2.56 by @renovate in https://github.com/element-hq/element-x-android/pull/3715
* Update dependency com.squareup:kotlinpoet-ksp to v2 by @renovate in https://github.com/element-hq/element-x-android/pull/3722
* Update dependency org.maplibre.gl:android-sdk-ktx-v7 to v3.0.2 by @renovate in https://github.com/element-hq/element-x-android/pull/3703
* Dependencies : makes sure to use same version for all kotlinpoet dependencies by @ganfra in https://github.com/element-hq/element-x-android/pull/3727
* Update dependency com.google.firebase:firebase-bom to v33.5.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3731
### Others
* No need to launch a coroutine here. by @bmarty in https://github.com/element-hq/element-x-android/pull/3668
* Fix issue on canInvite refresh. by @bmarty in https://github.com/element-hq/element-x-android/pull/3670
* AsyncAction confirming with param by @bmarty in https://github.com/element-hq/element-x-android/pull/3667
* Cleanup tests by @bmarty in https://github.com/element-hq/element-x-android/pull/3672
* Ensure selectedRoomMember is not null to reduce code indentation. by @bmarty in https://github.com/element-hq/element-x-android/pull/3669
* Improve preview provider name consistency by @bmarty in https://github.com/element-hq/element-x-android/pull/3673
* Clarify model for Event with attachment by @bmarty in https://github.com/element-hq/element-x-android/pull/3574
* Improve room moderation by @bmarty in https://github.com/element-hq/element-x-android/pull/3671
* Remove duplicated code regarding user (room member and user profile) screens by @bmarty in https://github.com/element-hq/element-x-android/pull/3700
* Rename some function to avoid name clash by @bmarty in https://github.com/element-hq/element-x-android/pull/3705
* Fix flaky tests. by @bmarty in https://github.com/element-hq/element-x-android/pull/3717
* Update accent color for `Checkbox`, `RadioButton` and `Switch` components by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3728

Changes in Element X v0.7.0 (2024-10-10)
========================================

## What's Changed
### 🙌 Improvements
* Enable Login with QR code in release builds. by @bmarty in https://github.com/element-hq/element-x-android/pull/3646
* Remove unused `RoomSummary` cache by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3647
### 🐛 Bugfixes
* Add the `CallWebView` logs to our logging stack by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3637
### Dependency upgrades
* Update dependency io.element.android:emojibase-bindings to v1.3.3 by @renovate in https://github.com/element-hq/element-x-android/pull/3620
* fix(deps): update dependency androidx.compose:compose-bom to v2024.09.03 by @renovate in https://github.com/element-hq/element-x-android/pull/3583
* fix(deps): update dependency io.mockk:mockk to v1.13.13 by @renovate in https://github.com/element-hq/element-x-android/pull/3634
* chore(deps): update dependencyanalysis to v2.1.4 by @renovate in https://github.com/element-hq/element-x-android/pull/3610
* fix(deps): update dependency androidx.webkit:webkit to v1.12.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3584
* fix(deps): update dependency com.posthog:posthog-android to v3.8.1 by @renovate in https://github.com/element-hq/element-x-android/pull/3638
* Upgrade Kotlin to v2.0 by @jmartinesp in https://github.com/element-hq/element-x-android/pull/3594
### Others
* Rework room summary by @ganfra in https://github.com/element-hq/element-x-android/pull/3631
* QrCode intro screen: add subtitle and fix button wording #3632 by @bmarty in https://github.com/element-hq/element-x-android/pull/3633
* Improve avatar rendering by @ganfra in https://github.com/element-hq/element-x-android/pull/3642
* Add feature flag IdentityPinningViolationNotifications. by @bmarty in https://github.com/element-hq/element-x-android/pull/3648
* Crypto copy adjustment by @bmarty in https://github.com/element-hq/element-x-android/pull/3649


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
