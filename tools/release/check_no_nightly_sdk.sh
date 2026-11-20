#!/usr/bin/env bash

# Copyright (c) 2026 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# Fails if the matrix_sdk dependency uses a "-nightly" version, which must never be released.

versionsFile="$(dirname "$0")/../../gradle/libs.versions.toml"

sdkLine=$(grep -E '^matrix_sdk[[:space:]]*=' "${versionsFile}")
if [[ -z "${sdkLine}" ]]; then
  echo "Fatal: could not find the matrix_sdk dependency in ${versionsFile}"
  exit 1
fi

if [[ "${sdkLine}" == *-nightly* ]]; then
  echo "Fatal: the matrix_sdk dependency uses a nightly version, which cannot be released:"
  echo "  ${sdkLine}"
  exit 1
fi

echo "matrix_sdk dependency is not a nightly version."
