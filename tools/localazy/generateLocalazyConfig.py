#!/usr/bin/env python3

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2024, 2025 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

import json
import sys

# Read the config.json file
with open('./tools/localazy/config.json', 'r') as f:
    config = json.load(f)

allFiles = sys.argv[1] == "1"

# Convert a module name to a path
# Ex: ":features:verifysession:impl" => "features/verifysession/impl"
def convertModuleToPath(name):
    return name[1:].replace(":", "/")

# Regex that will be excluded from the Android project, you may add items here if necessary.
regexToAlwaysExclude = [
    "Notification",
    ".*_ios"
]

baseAction = {
    "type": "android",
    # Replacement done in all string values
    "replacements": {
        "...": "…"
    },
    "params": {
        "force_underscore": "yes"
    }
}

# Store all regex specific to module, to exclude the corresponding key from the common string module
allRegexToExcludeFromMainModule = []
# All actions that will be serialized in the localazy config
allActions = []

# Iterating on the config
for entry in config["modules"]:
    # Create action for the default language
    excludeRegex = regexToAlwaysExclude
    if "excludeRegex" in entry:
        excludeRegex += entry["excludeRegex"]
    action = baseAction | {
        "output": convertModuleToPath(entry["name"]) + "/src/main/res/values/localazy.xml",
        "includeKeys": list(map(lambda i: "REGEX:" + i, entry["includeRegex"])),
        "excludeKeys": list(map(lambda i: "REGEX:" + i, excludeRegex)),
        "conditions": [
            "equals: ${langAndroidResNoScript}, en | equals: ${file}, content.json"
        ]
    }
    # print(action)
    allActions.append(action)
    # Create action for the translations
    if allFiles:
        actionTranslation = baseAction | {
            "output": convertModuleToPath(entry["name"]) + "/src/main/res/values-${langAndroidResNoScript}/translations.xml",
            "includeKeys": list(map(lambda i: "REGEX:" + i, entry["includeRegex"])),
            "excludeKeys": list(map(lambda i: "REGEX:" + i, excludeRegex)),
            "conditions": [
                "!equals: ${langAndroidResNoScript}, en | equals: ${file}, content.json"
            ],
            "langAliases": {
                "id": "in"
            }
        }
        allActions.append(actionTranslation)
    allRegexToExcludeFromMainModule.extend(entry["includeRegex"])

# Append configuration for the main string module: default language
mainAction = baseAction | {
    "output": "libraries/ui-strings/src/main/res/values/localazy.xml",
    "excludeKeys": list(map(lambda i: "REGEX:" + i, allRegexToExcludeFromMainModule + regexToAlwaysExclude)),
    "conditions": [
        "equals: ${langAndroidResNoScript}, en | equals: ${file}, content.json"
    ]
}
# print(mainAction)
allActions.append(mainAction)

if allFiles:
    # Append configuration for the main string module: translations
    mainActionTranslation = baseAction | {
        "output": "libraries/ui-strings/src/main/res/values-${langAndroidResNoScript}/translations.xml",
        "excludeKeys": list(map(lambda i: "REGEX:" + i, allRegexToExcludeFromMainModule + regexToAlwaysExclude)),
        "conditions": [
            "!equals: ${langAndroidResNoScript}, en | equals: ${file}, content.json"
        ],
        "langAliases": {
            "id": "in"
        }
    }
    allActions.append(mainActionTranslation)

# Generate the configuration for localazy
result = {
    "readKey": "a7876306080832595063-aa37154bb3772f6146890fca868d155b2228b492c56c91f67abdcdfb74d6142d",
    "conversion": {
        "actions": allActions
    }
}

# Json serialization
with open('./tools/localazy/localazy.json', 'w') as json_file:
    json.dump(result, json_file, indent=4, sort_keys=True)
