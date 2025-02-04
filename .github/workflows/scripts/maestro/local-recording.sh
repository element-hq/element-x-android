#!/bin/sh

#
# Copyright 2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only
# Please see LICENSE in the repository root for full details.
#

COUNT=0
mkdir -p /data/local/tmp/recordings;
FILENAME=/data/local/tmp/recordings/testRecording$COUNT.mp4
while true
 do
   ((COUNT++))
   FILENAME=/data/local/tmp/recordings/testRecording$COUNT.mp4
   echo "\nRecording video file #$COUNT"
   screenrecord --bugreport --bit-rate=16m --size 720x1280 $FILENAME
 done
