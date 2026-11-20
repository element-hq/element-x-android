#! /bin/bash

# Copyright (c) 2026 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# Usage: ./tools/manifest/aaptDump.sh [--skip-build] [output_file]
#   --skip-build: do not assemble the Gplay release APK, it must have been built before.
#   output_file: where to write the dump, default to the reference file ./tools/manifest/gplay/release/aaptDump.txt

set -e

skipBuild=false
if [[ "$1" == "--skip-build" ]]; then
  skipBuild=true
  shift
fi
outputFile="${1:-./tools/manifest/gplay/release/aaptDump.txt}"

# Read buildToolsVersion from file plugins/src/main/kotlin/Versions.kt
buildToolsVersion=$(grep "BUILD_TOOLS_VERSION =" ./plugins/src/main/kotlin/Versions.kt | cut -d '=' -f 2 | xargs)
aapt="${ANDROID_HOME}/build-tools/${buildToolsVersion}/aapt"

if [[ ! -x "${aapt}" ]]; then
  if [[ "${CI}" == "true" ]]; then
    echo "Installing build-tools ${buildToolsVersion}"
    yes | "${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager" "build-tools;${buildToolsVersion}" > /dev/null
  else
    echo "Fatal: ${aapt} not found, ensure that you have installed the build-tools version ${buildToolsVersion}."
    exit 1
  fi
fi

if [[ "${skipBuild}" == "false" ]]; then
  # Assemble Gplay Release
  ./gradlew assembleGplayRelease
fi

# Dump information
"${aapt}" dump badging ./app/build/outputs/apk/gplay/release/app-gplay-universal-release.apk > "${outputFile}"
