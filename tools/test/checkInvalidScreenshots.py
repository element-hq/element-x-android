#!/usr/bin/env python3

#  Copyright (c) 2024 New Vector Ltd
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

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
