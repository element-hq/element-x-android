#!/usr/bin/env bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# Ensure no Gradle module ships a `temporary.xml` strings file.
# New English strings live in `temporary.xml` while a change is in flight, but
# the core team imports them into Localazy before merging, so none should ever
# remain in the repository.

set -euo pipefail

# Look for any `src/<sourceSet>/res/values/temporary.xml` file, ignoring build
# outputs and the git metadata directory.
temporaryFiles=$(find . \
  -type d \( -name build -o -name .git \) -prune -o \
  -type f -path '*/src/*/res/values/temporary.xml' -print | sort)

if [[ -n "$temporaryFiles" ]]; then
  echo "Error: found temporary string file(s) that must be imported into Localazy and removed:"
  echo "$temporaryFiles" | sed 's|^\./|  - |'
  exit 1
fi

echo "OK: no temporary.xml string files found."
