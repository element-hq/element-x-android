#! /bin/bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

set -e

allFiles=0

while [[ $# -gt 0 ]]; do
  case $1 in
    -a|--all)
      allFiles=1
      shift
      ;;
    -v|--verbose)
      verbose=1
      shift
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

if [[ $allFiles == 1 ]]; then
  echo "Note: I will update all the files."
else
  echo "Note: I will update only the English files."
fi

echo "Generating the configuration file for localazy..."
python3 ./tools/localazy/generateLocalazyConfig.py $allFiles

# Search for existing string files first so we can remove them
localazyXmlFiles=$(find . \
  -type d \( -name build -o -name .git \) -prune -o \
  -type f -path '*/src/*/res/values/localazy.xml' -print | sort)

if [[ $allFiles == 1 ]]; then
  translationXmlFiles=$(find . \
      -type d \( -name build -o -name .git \) -prune -o \
      -type f -path '*/src/*/res/values-*/translations.xml' -print | sort)
fi

echo "Deleting all existing localazy.xml files..."
echo "$localazyXmlFiles" | xargs -L1 -P0 rm

if [[ $allFiles == 1 ]]; then
  echo "Deleting all existing translations.xml files..."
  echo "$translationXmlFiles" | xargs -L1 -P0 rm
fi

echo "Importing the strings..."
localazy download --config ./tools/localazy/localazy.json

echo "Removing the generated config"
rm ./tools/localazy/localazy.json

# Now list the newly generated files so we can format them and check for forbidden terms
localazyXmlFiles=$(find . \
  -type d \( -name build -o -name .git \) -prune -o \
  -type f -path '*/src/*/res/values/localazy.xml' -print | sort)

if [[ $allFiles == 1 ]]; then
  translationXmlFiles=$(find . \
      -type d \( -name build -o -name .git \) -prune -o \
      -type f -path '*/src/*/res/values-*/translations.xml' -print | sort)
fi

formatXmlFiles() {
  local xml="$1"
  if [[ $verbose == 1 ]]; then
    echo "Formatting $xml"
  fi
  ./tools/localazy/formatXmlResourcesFile.py "$xml"
}

checkForbiddenTerms() {
  set +e
  local xml="$1"
  if [[ $verbose == 1 ]]; then
    echo "Checking forbidden terms in $xml"
  fi
  ./tools/localazy/checkForbiddenTerms.py "$xml"

  # If the script finds a forbidden term, it will exit with a non-zero status code and interrupt the execution, but we don't want that.
  # Instead we want to capture that exit code and write it to a global variable so we can check later if any forbidden terms were found.
  status=$?
  if [[ $status != 0 ]]; then
    forbiddenTermsResult=$((forbiddenTermsResult || status))
  fi
}

export -f formatXmlFiles
export -f checkForbiddenTerms

echo "Formatting the resources files..."

echo "Formatting original localazy.xml files..."
# Format the original localazy.xml files in parallel
echo "$localazyXmlFiles" | xargs -L1 -P0 bash -c 'formatXmlFiles "$@"' _

if [[ $allFiles == 1 ]]; then
  if [[ $verbose == 1 ]]; then
    echo "Formatting translation files..."
  fi

  echo "$translationXmlFiles" | xargs -L1 -P0 bash -c 'formatXmlFiles "$@"' _
fi

# This stores whether any forbidden terms were found in any of the files. If any forbidden terms are found, this will be set to a non-zero value.
export forbiddenTermsResult=0

echo "Checking forbidden terms..."
echo "$localazyXmlFiles" | xargs -L1 -P0 bash -c 'checkForbiddenTerms "$@"' _

if [[ $allFiles == 1 ]]; then
  echo "Checking forbidden terms in translation files..."
  echo "$translationXmlFiles" | xargs -L1 -P0 bash -c 'checkForbiddenTerms "$@"' _
fi

if [[ $forbiddenTermsResult != 0 ]]; then
  echo "Error: Forbidden terms found in the resources files. Please check the output above."
  exit 1
fi

echo "Success!"
