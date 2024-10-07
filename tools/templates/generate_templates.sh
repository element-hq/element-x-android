#!/usr/bin/env bash

# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only
# Please see LICENSE in the repository root for full details.

echo "Zipping the contents of the 'files' directory..."

# Ensure tmp folder exists
mkdir -p tmp

rm -f ./tmp/file_templates.zip
pushd ./tools/templates/files || exit
zip -r ../../../tmp/file_templates.zip .
popd || exit
