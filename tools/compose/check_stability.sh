#!/usr/bin/env bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2025 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

set -e

# Build the project with compose report
echo "Building the project with compose report..."
./gradlew assembleGplayDebug -PcomposeCompilerReports=true -PcomposeCompilerMetrics=true --stacktrace

echo "Checking stability of State classes..."
# Using the find command, list all the files ending with -classes.txt
find . -type f -name "*-classes.txt" | while read -r file; do
    # echo "Processing $file"
    # Check that there is no line containing "unstable class .*State {"
    if grep -E 'unstable class .*State \{' "$file"; then
        echo "❌ ERROR: Found unstable State class in $file"
        exit 1
    fi
done
