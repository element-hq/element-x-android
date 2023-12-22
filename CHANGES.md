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
