#! /bin/bash

# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only
# Please see LICENSE in the repository root for full details.

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

set +e
echo "Moving files from values-id to values-in..."
find . -type d -name 'values-id' -execdir mv {}/translations.xml {}/../values-in/translations.xml \; 2> /dev/null

echo "Deleting all the folders values-id..."
find . -type d -name 'values-id' -exec rm -rf {} \; 2> /dev/null
set -e

echo "Checking forbidden terms..."
find . -name 'localazy.xml' -exec ./tools/localazy/checkForbiddenTerms.py {} \;
if [[ $allFiles == 1 ]]; then
  find . -name 'translations.xml' -exec ./tools/localazy/checkForbiddenTerms.py {} \;
fi

echo "Success!"
