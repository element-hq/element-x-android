#!/usr/bin/env bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# List of tasks to run before creating a PR, to limit the risk of getting rejected by the CI.
# Can be used as a git hook if you want.

# exit when any command fails
set -e

# First run the quickest script
./tools/check/check_code_quality.sh

# Check ktlint and Konsist first
./gradlew runQualityChecks

# Build, test and check the project, with warning as errors
./gradlew check -PallWarningsAsErrors=true
