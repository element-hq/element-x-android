#!/bin/sh

#
# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only.
# Please see LICENSE in the repository root for full details.
#

# First we disable the onboarding flow on Chrome, which is a source of issues
# (see https://stackoverflow.com/a/64629745)
echo "Disabling Chrome onboarding flow"
adb shell am set-debug-app --persistent com.android.chrome
adb shell 'echo "chrome --disable-fre --no-default-browser-check --no-first-run" > /data/local/tmp/chrome-command-line'
adb shell am start -n com.android.chrome/com.google.android.apps.chrome.Main

adb install -r $1
echo "Starting the screen recording..."
adb push .github/workflows/scripts/maestro/local-recording.sh /data/local/tmp/
adb shell "chmod +x /data/local/tmp/local-recording.sh"
adb shell "/data/local/tmp/local-recording.sh & echo \$! > /data/local/tmp/screenrecord_pid.txt" &
set +e
~/.maestro/bin/maestro test .maestro/allTests.yaml
TEST_STATUS=$?
echo "Test run completed with status $TEST_STATUS"

# Stop the screen recording loop
SCRIPT_PID=$(adb shell "cat /data/local/tmp/screenrecord_pid.txt")
adb shell "kill -2 $SCRIPT_PID"

# Get the PID of the screen recording process
SCREENRECORD_PID=$(adb shell ps | grep screenrecord | awk '{print $2}')
# Wait for the screen recording process to exit
while [ ! -z $SCREENRECORD_PID ]; do
  echo "Waiting for screen recording ($SCREENRECORD_PID) to finish..."
  adb shell "kill -2 $SCREENRECORD_PID"
  sleep 1
  SCREENRECORD_PID=$(adb shell ps | grep screenrecord | awk '{print $2}')
done

adb pull /data/local/tmp/recordings/ ~/.maestro/tests/
exit $TEST_STATUS
