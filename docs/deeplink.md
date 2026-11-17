# Element X Android deeplink

<!--- TOC -->

* [Introduction](#introduction)
  * [Asset Links](#asset-links)
  * [Supported links](#supported-links)
* [Developer tools](#developer-tools)

<!--- END -->


## Introduction

Element X Android supports deep linking to specific screens in the application. This document explains how to use deep links in Element X Android.

### Asset Links

Each verified domain serves its own asset links file:
- https://app.element.io/.well-known/assetlinks.json
- https://develop.element.io/.well-known/assetlinks.json
- https://staging.element.io/.well-known/assetlinks.json
- https://mobile.element.io/.well-known/assetlinks.json

Note: `*.element.io` is not used in the manifest, because it would also intercept links to `mas.element.io`.

### Supported links

Link to a user:
> https://app.element.io/#/user/@alice:matrix.org

Link to a room by id or alias:
> https://app.element.io/#/room/!roomid:matrix.org
> https://app.element.io/#/room/#element-x-android:matrix.org

Link to a room with a specific event:
> https://app.element.io/#/room/!roomid:matrix.org/$eventid

Note that it will also work with these domains:
> https://develop.element.io
> https://staging.element.io

Mobile configuration link (used to configure the account provider during login):
> https://mobile.element.io/element/?account_provider=example.org&login_hint=mxid:@alice:example.org

`matrix:` URIs ([MSC2312](https://github.com/matrix-org/matrix-spec-proposals/pull/2312)):
> matrix:r/element-android:matrix.org

`element://` links, used when opening the app from the https://matrix.to website:
> element://room/%23element-android%3Amatrix.org
> element://user/%40alice%3Amatrix.org

## Developer tools

Scripts to trigger the links above are available in `./tools/adb/deeplink_*.sh`.

Using an Android 12 or higher emulator

Ensure links verification is enabled
```bash
adb shell am compat enable 175408749 io.element.android.x.debug  
```

Reset link verifications for the given package id
```bash
adb shell pm set-app-links --package io.element.android.x.debug 0 all 
```

Force the package id links to be verified
```bash
adb shell pm verify-app-links --re-verify io.element.android.x.debug 
```

Print the link verification of the package id
```bash
adb shell pm get-app-links io.element.android.x.debug
```

```
  io.element.android.x.debug:
    ID: <id>
    Signatures: [<signature>]
    Domain verification state:
      app.element.io: verified
      develop.element.io: verified
      staging.element.io: verified
      mobile.element.io: verified
```
