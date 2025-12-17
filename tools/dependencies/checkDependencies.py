#!/usr/bin/env python3

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2024, 2025 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

import os
import subprocess


def getProjectDependencies():
    print("=> Computing dependencies...")
    command = subprocess.run(
        ["./gradlew :app:dependencies"],
        shell=True,
        capture_output=True,
        text=True,
    )
    data = command.stdout
    # Remove the trailing info like "(*)"
    result = list(map(lambda x: x.split(" (")[0], data.split("\n")))
    # Filter out comment line
    result = list(filter(lambda x: "--- project" in x, result))
    return result


def checkThatModulesExist(dependencies):
    print("=> Checking that all modules exist...")
    error = 0
    modules = set()
    for line in dependencies:
        if line:
            line = line.split(" ")
            for elem in line:
                if ":" in elem:
                    modules.add(elem)
    for module in modules:
        path = "." + module.replace(":", "/") + "/build.gradle.kts"
        if not os.path.exists(path):
            error += 1
            print("Error: there is at least one dependency to '" + module + "' but the module does not exist.")
            print("    Please remove occurrence(s) of 'implementation(projects" + module.replace(":", ".") + ")'.")
    return error


def checkThatThereIsNoTestDependency(dependencies):
    print("=> Checking that there are no test dependencies...")
    errors = set()
    currentProject = ""
    for line in dependencies:
        if line.startswith("+--- project "):
            currentProject = line.split(" ")[2]
        else:
            if ":test" in currentProject:
                continue
            else:
                subProject = line.split(" ")[-1]
                if subProject.endswith(":test") or ":tests:" in subProject and "detekt-rules" not in subProject:
                    error = "Error: '" + currentProject + "' depends on the test project '" + subProject + "'\n"
                    error += "    Please replace occurrence(s) of 'implementation(projects" + subProject.replace(":", ".") + ")'"
                    error += " with 'testImplementation(projects" + subProject.replace(":", ".") + ")'."
                    errors.add(error)
    for error in errors:
        print(error)
    return len(errors)


def main():
    dependencies = getProjectDependencies()
    # for dep in dependencies:
    #     print(dep)
    errors = 0
    errors += checkThatModulesExist(dependencies)
    errors += checkThatThereIsNoTestDependency(dependencies)
    print()
    if (errors == 0):
        print("All checks passed successfully.")
    elif (errors == 1):
        print("Please fix the error above.")
    else:
        print("Please fix the " + str(errors) + " errors above.")
    exit(errors)


if __name__ == "__main__":
    main()
