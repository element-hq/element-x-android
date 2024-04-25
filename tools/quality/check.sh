#!/usr/bin/env bash

#
# Copyright 2023 New Vector Ltd
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
