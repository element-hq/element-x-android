#!/usr/bin/env python3
#
# Copyright 2024 New Vector Ltd
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
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
