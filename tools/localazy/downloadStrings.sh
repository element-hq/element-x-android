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

formatXmlFiles() {
  local xml="$1"
  echo "Formatting $xml"
  ./tools/localazy/formatXmlResourcesFile.py "$xml"
}

checkForbiddenTerms() {
  local xml="$1"
  echo "Checking forbidden terms in $xml"
  ./tools/localazy/checkForbiddenTerms.py "$xml"
}

export -f formatXmlFiles
export -f checkForbiddenTerms

echo "Formatting the resources files..."
localazyXmlFiles=$(find . \
  -type d \( -name build -o -name .git \) -prune -o \
  -type f -path '*/src/*/res/values/localazy.xml' -print | sort)

translationXmlFiles=""

echo "Formatting original localazy.xml files..."
# Format the original localazy.xml files in parallel
echo "$localazyXmlFiles" | xargs -L1 -P0 bash -c 'formatXmlFiles "$@"' _

if [[ $allFiles == 1 ]]; then
  echo "Formatting translation files..."
  translationXmlFiles=$(find . \
    -type d \( -name build -o -name .git \) -prune -o \
    -type f -path '*/src/*/res/values-*/translations.xml' -print | sort)

    echo "$translationXmlFiles" | xargs -L1 -P0 bash -c 'formatXmlFiles "$@"' _
fi

echo "Checking forbidden terms..."
echo "$localazyXmlFiles" | xargs -L1 -P0 bash -c 'checkForbiddenTerms "$@"' _

if [[ $allFiles == 1 ]]; then
  echo "$translationXmlFiles" | xargs -L1 -P0 bash -c 'checkForbiddenTerms "$@"' _
fi

echo "Success!"
