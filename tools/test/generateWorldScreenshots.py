#!/usr/bin/env python3
#
# Copyright 2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only
# Please see LICENSE in the repository root for full details.
#

import os

def detectAllExistingTranslations():
    # Read all the folder in "libraries/ui-strings/src/main/res"
    folders = os.listdir("libraries/ui-strings/src/main/res")
    # Remove the "values" folder
    folders.remove("values")
    # Map to keep only the language code
    folders = list(map(lambda folder: folder[7:], folders))
    # Map to keep only the string before the "-"
    folders = list(map(lambda folder: folder.split("-")[0], folders))
    # Remove duplicates
    folders = list(set(folders))
    return folders


def main():
    languages = detectAllExistingTranslations()
    print ("Will record the screenshots for those languages: %s" % languages)
    # Run the python script "generateAllScreenshots.py" with the detected languages
    os.system("./tools/test/generateAllScreenshots.py %s" % " ".join(languages))

main()
