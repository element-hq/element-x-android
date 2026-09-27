#! /bin/bash

# Copyright (c) 2026 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# Assemble Gplay Release
./gradlew assembleGplayRelease

# Dump information
$ANDROID_HOME/build-tools/37.0.0/aapt dump badging ./app/build/outputs/apk/gplay/release/app-gplay-universal-release.apk \
   > ./tools/manifest/gplay/release/aaptDump.txt
