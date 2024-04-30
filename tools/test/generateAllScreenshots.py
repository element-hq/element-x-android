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
import re
import sys
import time
from util import compare


# Read all arguments and return a list of them, this are the languages list.
def readArguments():
    # Return sys.argv without the first argument
    return sys.argv[1:]


def generateAllScreenshots(languages):
    # If languages is empty, generate all screenshots
    if len(languages) == 0:
        print("Generating all screenshots...")
        os.system("./gradlew recordPaparazziDebug -PallLanguages")
    else:
        tFile = "tests/uitests/src/test/kotlin/ui/T.kt"
        print("Generating screenshots for languages: %s" % languages)
        # Record the languages one by one, else it's getting too slow
        for lang in languages:
            print("Generating screenshots for language: %s" % lang)
            # Patch file T.kt, replace `@TestParameter(value = ["de"]) localeStr: String,` with `@TestParameter(value = [<the languages>]) localeStr: String,`
            with open(tFile, "r") as file:
                data = file.read()
            data = data.replace("@TestParameter(value = [\"de\"]) localeStr: String,", "@TestParameter(value = [\"%s\"]) localeStr: String," % lang)
            with open(tFile, "w") as file:
                file.write(data)
            os.system("./gradlew recordPaparazziDebug -PallLanguagesNoEnglish")
            # Git reset the change on file T.kt
            os.system("git checkout HEAD -- %s" % tFile)


def detectLanguages():
    __doc__ = "Detect languages from screenshots, other than English"
    files = os.listdir("tests/uitests/src/test/snapshots/images/")
    languages = set(map(lambda file: file[-7:-5], files))
    languages = [lang for lang in languages if re.match("[a-z]", lang) and lang != "en"]
    print("Detected languages: %s" % languages)
    return languages


def deleteDuplicatedScreenshots(lang):
    __doc__ = "Delete screenshots identical to the English version for a language"
    print("Deleting screenshots identical to the English version for language %s..." % lang)
    files = os.listdir("tests/uitests/src/test/snapshots/images/")
    # Filter files by language
    files = [file for file in files if file[-7:-5] == lang]
    identicalFileCounter = 0
    differentFileCounter = 0
    for file in files:
        englishFile = file[:3] + "S" + file[4:-7] + "en" + file[-5:]
        fullFile = "tests/uitests/src/test/snapshots/images/" + file
        fullEnglishFile = "tests/uitests/src/test/snapshots/images/" + englishFile
        isDifferent = compare(fullFile, fullEnglishFile)
        if isDifferent:
            differentFileCounter += 1
        else:
            identicalFileCounter += 1
            os.remove(fullFile)
    print("For language %s, keeping %d files and deleting %d files." % (lang, differentFileCounter, identicalFileCounter))


def moveScreenshots(lang):
    __doc__ = "Move screenshots to the folder per language"
    targetFolder = "screenshots/" + lang
    print("Deleting existing screenshots for %s..." % lang)
    os.system("rm -rf %s" % targetFolder)
    print("Moving screenshots for %s to %s..." % (lang, targetFolder))
    files = os.listdir("tests/uitests/src/test/snapshots/images/")
    # Filter files by language
    files = [file for file in files if file[-7:-5] == lang]
    # Create the folder "./screenshots/<lang>"
    os.makedirs(targetFolder, exist_ok=True)
    for file in files:
        fullFile = "tests/uitests/src/test/snapshots/images/" + file
        os.rename(fullFile, targetFolder + "/" + file)


def detectRecordedLanguages():
    # List all the subfolders of the screenshots folder which contains 2 letters, sorted alphabetically
    return sorted([f for f in os.listdir("screenshots") if len(f) == 2])


def computeDarkFileName(lightFileName):
    if "-Day_0" in lightFileName:
        return lightFileName.replace("-Day_0", "-Night_1")
    match = re.match("(.*)-Day-(\\d+)_(\\d+)(.*)", lightFileName, flags=re.ASCII)
    if match:
        return match.group(1) + "-Night-" + match.group(2) + "_" + str((int(match.group(3)) + 1)) + match.group(4)
    return ""

def generateJavascriptFile():
    __doc__ = "Generate a javascript file to load the screenshots"
    print("Generating javascript file...")
    languages = detectRecordedLanguages()
    # First item is the list of languages, adding "en" and "en-dark" at the beginning
    data = [["en", "en-dark"] + languages]
    files = sorted(
        os.listdir("tests/uitests/src/test/snapshots/images/"),
        key=lambda file: file[file.find("_", 6):],
    )
    for file in files:
        # Continue if file contains "-Night", keep only light screenshots
        if "-Night" in file:
            continue
        dataForFile = [file[:-4]]
        darkFile = computeDarkFileName(file)
        if os.path.exists("./tests/uitests/src/test/snapshots/images/" + darkFile):
            dataForFile.append(darkFile[:-4])
        else:
            dataForFile.append("")
        for l in languages:
            simpleFile = file[:3] + "T" + file[4:-7] + l + file[-5:-4]
            translatedFile = "./screenshots/" + l + "/" + simpleFile + ".png"
            if os.path.exists(translatedFile):
                # Get the last modified date of the file in seconds and round to days
                date = os.popen("git log -1 --format=%ct -- \"" + translatedFile + "\"").read().strip()
                # if date is empty, use today's date
                if date == "":
                    date = time.time()
                dateDay = int(date) // 86400
                dataForFile.append(dateDay)
            else:
                dataForFile.append(0)
        data.append(dataForFile)

    with open("screenshots/html/data.js", "w") as f:
        f.write("// Generated file, do not edit\n")
        f.write("export const screenshots = [\n")
        for line in data:
            f.write("[")
            for item in line:
                # If item is a string, add quotes
                if isinstance(item, str):
                    f.write("\"" + item + "\",")
                else:
                    f.write(str(item) + ",")
            f.write("],\n")
        f.write("];\n")


def main():
    generateAllScreenshots(readArguments())
    lang = detectLanguages()
    for l in lang:
        deleteDuplicatedScreenshots(l)
        moveScreenshots(l)
    generateJavascriptFile()


main()
