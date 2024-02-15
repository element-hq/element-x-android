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
