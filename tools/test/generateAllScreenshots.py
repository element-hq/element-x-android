#!/usr/bin/env python3

import os
import re


def deleteExistingScreenshots():
    print("Deleting existing screenshots...")
    os.system("rm -rf screenshots")


def generateAllScreenshots():
    print("Generating all screenshots...")
    os.system("./gradlew recordPaparazziDebug -PallLanguages")


def detectLanguages():
    __doc__ = "Detect languages from screenshots, other than English"
    files = os.listdir("tests/uitests/src/test/snapshots/images/")
    languages = set(map(lambda file: file[-7:-5], files))
    languages = [lang for lang in languages if re.match("[a-z]", lang) and lang != "en"]
    print("Detected languages: %s" % languages)
    return languages


def compare(file1, file2):
    __doc__ = "Compare two files, return True if different, False if identical."
    # Compare file size
    file1_stats = os.stat(file1)
    file2_stats = os.stat(file2)
    if file1_stats.st_size != file2_stats.st_size:
        return True
    # Compare file content
    with open(file1, "rb") as f1, open(file2, "rb") as f2:
        content1 = f1.read()
        content2 = f2.read()
        return content1 != content2


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
    print("Moving screenshots for %s to %s..." % (lang, targetFolder))
    files = os.listdir("tests/uitests/src/test/snapshots/images/")
    # Filter files by language
    files = [file for file in files if file[-7:-5] == lang]
    # Create the folder "./screenshots/<lang>"
    os.makedirs(targetFolder, exist_ok=True)
    for file in files:
        fullFile = "tests/uitests/src/test/snapshots/images/" + file
        os.rename(fullFile, targetFolder + "/" + file)


def main():
    deleteExistingScreenshots()
    generateAllScreenshots()
    lang = detectLanguages()
    for l in lang:
        deleteDuplicatedScreenshots(l)
        moveScreenshots(l)


main()
