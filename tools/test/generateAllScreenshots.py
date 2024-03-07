#!/usr/bin/env python3

import os
import re
import sys
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
      # Patch file T.kt, replace `@TestParameter(value = ["de"]) localeStr: String,` with `@TestParameter(value = ["de", "fr"]) localeStr: String,`
      with open(tFile, "r") as file:
          data = file.read()
      languagesList = ", ".join([f"\"{lang}\"" for lang in languages])
      data = data.replace("@TestParameter(value = [\"de\"]) localeStr: String,", "@TestParameter(value = [%s]) localeStr: String," % languagesList)
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

def generateJavascriptFile():
    __doc__ = "Generate a javascript file to load the screenshots"
    print("Generating javascript file...")
    languages = detectRecordedLanguages()
    # First item is the list of languages, adding "en" at the beginning
    data = [["en"] + languages]
    # If any translated screenshot exists, keep the file
    files = sorted(
        os.listdir("tests/uitests/src/test/snapshots/images/"),
        key=lambda file: file[file.find("_", 6):],
    )
    for file in files:
        fullFile = "./tests/uitests/src/test/snapshots/images/" + file
        dataForFile = [fullFile]
        hasAnyTranslatedFile = False
        for l in languages:
            translatedFile = "./screenshots/" + l + "/" + file[:3] + "T" + file[4:-7] + l + file[-5:]
            if os.path.exists(translatedFile):
                hasAnyTranslatedFile = True
                dataForFile.append(translatedFile)
            else:
                dataForFile.append("")
        if hasAnyTranslatedFile:
            data.append(dataForFile)

    with open("screenshots/html/data.js", "w") as f:
        f.write("// Generated file, do not edit\n")
        f.write("export const screenshots = [\n")
        for line in data:
            f.write("[\n")
            for item in line:
                f.write("\"" + item + "\",\n")
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
