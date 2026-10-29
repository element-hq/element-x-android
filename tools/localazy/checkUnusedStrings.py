#!/usr/bin/env python3

# Copyright (c) 2026 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# Generate a Markdown report of the Localazy strings which are not used by Element X Android nor by Element X iOS.
# Requires a local checkout of the Element X iOS project.
# Usage: ./tools/localazy/checkUnusedStrings.py [--ios ../element-x-ios] [--output unusedStrings.md]

import argparse
import datetime
import os
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

ANDROID_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
IOS_LOCALIZATIONS = "ElementX/Resources/Localizations/en.lproj"
IOS_GENERATED_STRINGS = "ElementX/Sources/Generated/Strings.swift"
IOS_SKIPPED_ROOT_DIRS = ("UnitTests", "UITests", "PreviewTests", "IntegrationTests", "AccessibilityTests")


# Yield the paths of the files under root with one of the given extensions, ignoring hidden and build folders,
# and the folders for which isSkipped(path relative to root) returns True.
def walk(root, extensions, isSkipped):
    for directory, dirs, files in os.walk(root):
        dirs[:] = [d for d in dirs if not d.startswith(".") and d != "build"]
        if isSkipped(os.path.relpath(directory, root)):
            continue
        for file in files:
            if file.endswith(extensions):
                yield os.path.join(directory, file)


# Return the set of words and the set of string literals found in the files.
# Example: the line `Text(stringResource(CommonStrings.action_copy), tag = "a11y.copy")`
# gives the words {"Text", "stringResource", "CommonStrings", "action_copy", "tag", "a11y", "copy"}
# and the literals {"a11y.copy"}.
def readWords(files):
    words = set()
    literals = set()
    for path in files:
        with open(path, encoding="utf-8", errors="ignore") as f:
            content = f.read()
        words.update(re.findall(r"\w+", content))
        literals.update(re.findall(r'"([\w.]+)"', content))
    return words, literals


# Return a dict of the English Android strings, from all the `src/main/res/values/localazy.xml` files.
# Example:
#   <string name="action_copy">"Copy"</string>
#   <plurals name="common_member_count">
#     <item quantity="one">"%1$d member"</item>
#     <item quantity="other">"%1$d members"</item>
#   </plurals>
# gives {"action_copy": "\"Copy\"", "common_member_count": "\"%1$d members\""}.
# For plurals, the "other" form is used.
def getAndroidStrings():
    result = {}
    for directory, dirs, files in os.walk(ANDROID_ROOT):
        dirs[:] = [d for d in dirs if not d.startswith(".") and d != "build"]
        if directory.endswith("/src/main/res/values") and "localazy.xml" in files:
            for element in ET.parse(os.path.join(directory, "localazy.xml")).getroot():
                name = element.get("name")
                if element.tag == "string":
                    result[name] = "".join(element.itertext())
                elif element.tag == "plurals":
                    items = {item.get("quantity"): "".join(item.itertext()) for item in element}
                    result[name] = items.get("other") or next(iter(items.values()), "")
    return result


# Return a dict of the English iOS strings, from the files `Localizable.strings` and `Localizable.stringsdict`.
# Example: the line `"a11y.encryption_details" = "Encryption details";` of `Localizable.strings`
# gives {"a11y.encryption_details": "Encryption details"}. See getIosPluralValue() for `Localizable.stringsdict`.
def getIosStrings(iosRoot):
    result = {}
    localizations = os.path.join(iosRoot, IOS_LOCALIZATIONS)
    with open(os.path.join(localizations, "Localizable.strings"), encoding="utf-8") as f:
        for match in re.finditer(r'^"((?:[^"\\]|\\.)+)"\s*=\s*"((?:[^"\\]|\\.)*)";', f.read(), re.M):
            result[match.group(1)] = match.group(2)
    children = list(ET.parse(os.path.join(localizations, "Localizable.stringsdict")).getroot().find("dict"))
    for i in range(0, len(children), 2):
        result.setdefault(children[i].text, getIosPluralValue(children[i + 1]))
    return result


# Return the "other" form of a plural from `Localizable.stringsdict`, ignoring the `NSString*` entries.
# Example: for the key `a11y_digits_entered`, element is:
#   <dict>
#     <key>COUNT</key>
#     <dict>
#       <key>NSStringFormatSpecTypeKey</key>
#       <string>NSStringPluralRuleType</string>
#       <key>NSStringFormatValueTypeKey</key>
#       <string>d</string>
#       <key>one</key>
#       <string>%1$d digit entered</string>
#       <key>other</key>
#       <string>%1$d digits entered</string>
#     </dict>
#     <key>NSStringLocalizedFormatKey</key>
#     <string>%#@COUNT@</string>
#   </dict>
# and the result is "%1$d digits entered".
def getIosPluralValue(element):
    for variable in element.findall("dict"):
        entries = list(variable)
        forms = {entries[j].text: entries[j + 1].text for j in range(0, len(entries) - 1, 2) if entries[j].tag == "key"}
        forms = {quantity: value for quantity, value in forms.items() if not quantity.startswith("NSString")}
        if forms:
            return forms.get("other") or next(iter(forms.values()), "")
    return ""


# Return a dict mapping each iOS key to its SwiftGen accessor, from the generated file `Strings.swift`.
# Example: the lines
#   internal static var actionCopy: String { return L10n.tr("Localizable", "action_copy") }
#   internal static func a11yDigitsEntered(_ p1: Int) -> String {
#     return L10n.tr("Localizable", "a11y_digits_entered", p1)
# give {"action_copy": "actionCopy", "a11y_digits_entered": "a11yDigitsEntered"}.
def getIosAccessors(iosRoot):
    with open(os.path.join(iosRoot, IOS_GENERATED_STRINGS), encoding="utf-8") as f:
        content = f.read()
    result = {}
    for match in re.finditer(r'static (?:var|func) (\w+)[^\n]*?\n?[^\n]*?L10n\.tr\("Localizable", "([^"]+)"', content):
        result.setdefault(match.group(2), match.group(1))
    return result


# Return the set of words found in the Android production code (`.kt`, `.kts` and `.xml` files), excluding the
# string resource files, the test code and the tools folder. A key is used if it is in this set.
def getAndroidUsedWords():
    def isSkipped(path):
        return (
            re.search(r"(^|/)src/(test|androidTest)(/|$)", path) is not None
            or re.search(r"/res/values[^/]*$", path) is not None
            or path == "tools"
            or path.startswith("tools/")
        )

    words, _ = readWords(walk(ANDROID_ROOT, (".kt", ".kts", ".xml"), isSkipped))
    return words


# Return the sets of words and of string literals found in the iOS production code (`.swift` files), excluding the
# generated code and the test code. A key is used if its accessor is in the words, or if the key is in the literals.
def getIosUsedWordsAndLiterals(iosRoot):
    def isSkipped(path):
        return path.split("/")[0] in IOS_SKIPPED_ROOT_DIRS or "Sources/Generated" in path

    return readWords(walk(iosRoot, (".swift",), isSkipped))


# Return the short hash of the current commit of the git repository.
def getSha(repo):
    return subprocess.check_output(["git", "-C", repo, "rev-parse", "--short", "HEAD"], text=True).strip()


# Format a string value so that it fits in a Markdown table cell: remove the surrounding quotes, replace the line breaks
# with spaces, escape the pipes, unescape the quotes and truncate to 120 characters.
# Example: `"Don\'t | stop\nnow"` gives `Don't \| stop now`.
def formatValue(value):
    value = (value or "").strip()
    if len(value) >= 2 and value[0] == value[-1] == '"':
        value = value[1:-1]
    value = value.replace("\\n", " ").replace("\n", " ").replace("|", "\\|").strip()
    value = value.replace("\\\"", "\"").replace("\\'", "'")
    return value if len(value) <= 120 else value[:117] + "…"


# Find the keys used neither on Android nor on iOS, and write the Markdown report.
def main():
    parser = argparse.ArgumentParser(description="Generate a report of the Localazy strings unused by both Element X Android and Element X iOS.")
    parser.add_argument("--ios", default=os.path.join(ANDROID_ROOT, "..", "element-x-ios"), help="Path to the Element X iOS project")
    parser.add_argument("--output", default=os.path.join(ANDROID_ROOT, "unusedStrings.md"), help="Path to the generated Markdown file")
    args = parser.parse_args()
    iosRoot = os.path.abspath(args.ios)
    if not os.path.isdir(os.path.join(iosRoot, IOS_LOCALIZATIONS)):
        sys.exit(f"Element X iOS project not found at {iosRoot}, use --ios to set its path")

    androidStrings = getAndroidStrings()
    iosStrings = getIosStrings(iosRoot)
    iosAccessors = getIosAccessors(iosRoot)
    androidWords = getAndroidUsedWords()
    iosWords, iosLiterals = getIosUsedWordsAndLiterals(iosRoot)

    missingAccessors = [key for key in iosStrings if key not in iosAccessors]
    if missingAccessors:
        print(f"Warning: {len(missingAccessors)} iOS keys without SwiftGen accessor, e.g. {missingAccessors[:5]}", file=sys.stderr)

    def isUsedOnIos(key):
        accessor = iosAccessors.get(key)
        return (accessor is not None and accessor in iosWords) or key in iosLiterals

    # iOS keeps nested Localazy keys dotted (a11y.foo), Android flattens them (a11y_foo)
    iosKeysByNormalizedKey = {key.replace(".", "_"): key for key in iosStrings}
    displayKeys = {key: key for key in androidStrings}
    displayKeys.update(iosKeysByNormalizedKey)
    allKeys = sorted(displayKeys)
    unusedKeys = [
        key for key in allKeys
        if not (key in androidStrings and key in androidWords)
        and not (key in iosKeysByNormalizedKey and isUsedOnIos(iosKeysByNormalizedKey[key]))
    ]

    lines = [
        "# Unused strings",
        "",
        f"Localazy keys that are not used by Element X Android (`{getSha(ANDROID_ROOT)}`) or Element X iOS (`{getSha(iosRoot)}`), "
        f"generated on {datetime.date.today().isoformat()}.",
        "",
        f"**{len(unusedKeys)}** unused keys, out of {len(allKeys)} keys found in the English string files of both projects.",
        "",
        "How this was computed:",
        "- Android: a key counts as used when it appears as a word in production `.kt`, `.kts` or `.xml` files "
        "(excluding `res/values*/`, `src/test`, `src/androidTest` and `tools/`).",
        "- iOS: a key counts as used when its SwiftGen accessor (`L10n.xxx`) or the raw key literal appears in production `.swift` files "
        "(excluding `Sources/Generated`, `UnitTests`, `UITests`, `PreviewTests`, `IntegrationTests` and `AccessibilityTests`).",
        "- The Android and iOS columns say whether the key is in that platform's English string files.",
        "",
        "Caveats: keys built dynamically at runtime are not detected, so check each key before you delete it. "
        "A string used only in tests counts as unused.",
        "",
        "| Key | English value | Android | iOS | Localazy |",
        "|---|---|---|---|---|",
    ]
    for key in unusedKeys:
        displayKey = displayKeys[key]
        value = androidStrings[key] if key in androidStrings else iosStrings.get(displayKey)
        lines.append(
            f"| `{displayKey}` | {formatValue(value)} "
            f"| {'present' if key in androidStrings else 'absent'} "
            f"| {'present' if key in iosKeysByNormalizedKey else 'absent'} "
            f"| [link](https://localazy.com/p/element/source-language?search={displayKey}) |"
        )
    with open(args.output, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")
    print(f"{len(unusedKeys)} unused keys out of {len(allKeys)}, report written to {args.output}")


if __name__ == "__main__":
    main()
