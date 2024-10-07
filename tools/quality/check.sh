#!/usr/bin/env bash

# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only
# Please see LICENSE in the repository root for full details.

# List of tasks to run before creating a PR, to limit the risk of getting rejected by the CI.
# Can be used as a git hook if you want.

# exit when any command fails
set -e

# First run the quickest script
./tools/check/check_code_quality.sh

# Check ktlint and Konsist first
./gradlew runQualityChecks

# Build, test and check the project, with warning as errors
# It also check that the minimal app is compiling.
./gradlew check -PallWarningsAsErrors=true
