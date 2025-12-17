#! /bin/bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

set -e

if [[ $1 == "--all" ]]; then
  echo "Note: I will update all the files."
  allFiles=1
else
  echo "Note: I will update only the English files."
  allFiles=0
fi

echo "Generating the configuration file for localazy..."
python3 ./tools/localazy/generateLocalazyConfig.py $allFiles

echo "Deleting all existing localazy.xml files..."
find . -name 'localazy.xml' -delete

if [[ $allFiles == 1 ]]; then
  echo "Deleting all existing translations.xml files..."
  find . -name 'translations.xml' -delete
fi

echo "Importing the strings..."
localazy download --config ./tools/localazy/localazy.json

echo "Removing the generated config"
rm ./tools/localazy/localazy.json

echo "Formatting the resources files..."
find . -name 'localazy.xml' -exec ./tools/localazy/formatXmlResourcesFile.py {} \;
if [[ $allFiles == 1 ]]; then
  find . -name 'translations.xml' -exec ./tools/localazy/formatXmlResourcesFile.py {} \;
fi

echo "Checking forbidden terms..."
find . -name 'localazy.xml' -exec ./tools/localazy/checkForbiddenTerms.py {} \;
if [[ $allFiles == 1 ]]; then
  find . -name 'translations.xml' -exec ./tools/localazy/checkForbiddenTerms.py {} \;
fi

echo "Success!"
