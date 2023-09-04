Changes in Element X v0.1.6 (2023-09-04)
========================================

Features ✨
----------
 - Enable the Polls feature. Allows to create, view, vote and end polls. ([#1196](https://github.com/vector-im/element-x-android/issues/1196))
- Create poll. ([#1143](https://github.com/vector-im/element-x-android/issues/1143))

Bugfixes 🐛
----------
- Ensure notification for Event from encrypted room get decrypted content. ([#1178](https://github.com/vector-im/element-x-android/issues/1178))
 - Make sure Snackbars are only displayed once. ([#928](https://github.com/vector-im/element-x-android/issues/928))
 - Fix the orientation of sent images. ([#1135](https://github.com/vector-im/element-x-android/issues/1135))
 - Bug reporter crashes when 'send logs' is disabled. ([#1168](https://github.com/vector-im/element-x-android/issues/1168))
 - Add missing link to the terms on the analytics setting screen. ([#1177](https://github.com/vector-im/element-x-android/issues/1177))
 - Re-enable `SyncService.withEncryptionSync` to improve decryption of notifications. ([#1198](https://github.com/vector-im/element-x-android/issues/1198))
 - Crash with `aspectRatio` modifier when `Float.NaN` was used as input. ([#1995](https://github.com/vector-im/element-x-android/issues/1995))

Other changes
-------------
 - Remove unnecessary year in copyright mention. ([#1187](https://github.com/vector-im/element-x-android/issues/1187))


Changes in Element X v0.1.5 (2023-08-28)
========================================

Bugfixes 🐛
----------
 - Fix crash when opening any room. ([#1160](https://github.com/vector-im/element-x-android/issues/1160))


Changes in Element X v0.1.4 (2023-08-28)
========================================

Features ✨
----------
 - Allow cancelling media upload ([#769](https://github.com/vector-im/element-x-android/issues/769))
 - Enable OIDC support. ([#1127](https://github.com/vector-im/element-x-android/issues/1127))
 - Add a "Setting up account" screen, displayed the first time the user logs in to the app (per account). ([#1149](https://github.com/vector-im/element-x-android/issues/1149))

Bugfixes 🐛
----------
 - Videos sent from the app were cropped in some cases. ([#862](https://github.com/vector-im/element-x-android/issues/862))
 - Timeline: sender names are now displayed in one single line. ([#1033](https://github.com/vector-im/element-x-android/issues/1033))
 - Fix `TextButtons` being displayed in black. ([#1077](https://github.com/vector-im/element-x-android/issues/1077))
 - Linkify links in HTML contents. ([#1079](https://github.com/vector-im/element-x-android/issues/1079))
 - Fix bug reporter failing after not finding some log files. ([#1082](https://github.com/vector-im/element-x-android/issues/1082))
 - Fix rendering of inline elements in list items. ([#1090](https://github.com/vector-im/element-x-android/issues/1090))
 - Fix crash RuntimeException "No matching key found for the ciphertext in the stream" ([#1101](https://github.com/vector-im/element-x-android/issues/1101))
 - Make links in messages clickable again. ([#1111](https://github.com/vector-im/element-x-android/issues/1111))
 - When event has no id, just cancel parsing the latest room message for a room. ([#1125](https://github.com/vector-im/element-x-android/issues/1125))
 - Only display verification prompt after initial sync is done. ([#1131](https://github.com/vector-im/element-x-android/issues/1131))

In development 🚧
----------------
 - [Poll] Add feature flag in developer options ([#1064](https://github.com/vector-im/element-x-android/issues/1064))
 - [Polls] Improve UI and render ended state ([#1113](https://github.com/vector-im/element-x-android/issues/1113))

Other changes
-------------
 - Compound: add `ListItem` and `ListSectionHeader` components. ([#990](https://github.com/vector-im/element-x-android/issues/990))
 - Migrate `object` to `data object` in sealed interface / class #1135 ([#1135](https://github.com/vector-im/element-x-android/issues/1135))


Changes in Element X v0.1.2 (2023-08-16)
========================================

Bugfixes 🐛
----------
 - Filter push notifications using push rules. ([#640](https://github.com/vector-im/element-x-android/issues/640))
 - Use `for` instead of `forEach` in `DefaultDiffCacheInvalidator` to improve performance. ([#1035](https://github.com/vector-im/element-x-android/issues/1035))

In development 🚧
----------------
 - [Poll] Render start event in the timeline ([#1031](https://github.com/vector-im/element-x-android/issues/1031))

Other changes
-------------
 - Add Button component based on Compound designs ([#1021](https://github.com/vector-im/element-x-android/issues/1021))
 - Compound: implement dialogs. ([#1043](https://github.com/vector-im/element-x-android/issues/1043))
 - Compound: customise `IconButton` component. ([#1049](https://github.com/vector-im/element-x-android/issues/1049))
 - Compound: implement `DropdownMenu` customisations. ([#1050](https://github.com/vector-im/element-x-android/issues/1050))
 - Compound: implement Snackbar component. ([#1054](https://github.com/vector-im/element-x-android/issues/1054))


Changes in Element X v0.1.0 (2023-07-19)
========================================

First release of Element X 🚀!
