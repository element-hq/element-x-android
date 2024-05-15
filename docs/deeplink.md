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

The asset links file is available at https://element.io/.well-known/assetlinks.json

### Supported links

Element Call link: 
> https://call.element.io/Example

Link to a user:
> https://app.element.io/#/user/@alice:matrix.org

Link to a room by id or alias:
> https://app.element.io/#/room/!roomid:matrix.org
> https://app.element.io/#/room/#element-x-android:matrix.org

Link to a room with a specific event:
> https://app.element.io/#/room/!roomid:matrix.org/$eventid

Note that it will also work with other domain such as:
> https://mobile.element.io
> https://develop.element.io
> https://staging.element.io

## Developer tools

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
    ID: e2ece472-c266-4bf0-829c-be79959a6270
    Signatures: [B0:B0:51:DC:56:5C:81:2F:E1:7F:6F:3E:94:5B:4D:79:04:71:23:AB:0D:A6:12:86:76:9E:B2:94:91:97:13:0E]
    Domain verification state:
      *.element.io: 1024
```
