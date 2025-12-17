#!/usr/bin/env python3

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2024, 2025 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

import os

from util import compare


def checkInvalidScreenshots(reference):
    __doc__ = "Detect invalid screenshot, by comparing to an invalid reference."
    path_of_screenshots = "tests/uitests/src/test/snapshots/images/"
    files = os.listdir(path_of_screenshots)
    counter = 0
    for file in files:
        if not compare(reference, path_of_screenshots + file):
            print("Invalid screenshot detected: " + file)
            counter += 1
    return counter


def main():
    invalid_screenshot_reference_path = "tools/test/invalid_screenshot.png"
    result = checkInvalidScreenshots(invalid_screenshot_reference_path)
    if result > 0:
        print("%d invalid screenshot(s) detected" % result)
        print("Please check that the Preview is OK in Android Studio. You may want to use a Fake Composable for the screenshot to render correctly.")
        exit(1)
    else:
        print("No invalid screenshot detected!")
        exit(0)


main()
