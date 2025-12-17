#!/usr/bin/env bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

#######################################################################################################################
# Search forbidden pattern
#######################################################################################################################

searchForbiddenStringsScript=./tmp/search_forbidden_strings.pl

if [[ -f ${searchForbiddenStringsScript} ]]; then
  echo "${searchForbiddenStringsScript} already there"
else
  mkdir tmp
  echo "Get the script"
  wget https://raw.githubusercontent.com/matrix-org/matrix-dev-tools/develop/bin/search_forbidden_strings.pl -O ${searchForbiddenStringsScript}
fi

if [[ -x ${searchForbiddenStringsScript} ]]; then
  echo "${searchForbiddenStringsScript} is already executable"
else
  echo "Make the script executable"
  chmod u+x ${searchForbiddenStringsScript}
fi

echo
echo "Search for forbidden patterns in Kotlin source files..."

# list all Kotlin folders of the project.
allKotlinDirs=$(find . -type d |grep -v build |grep -v \.git |grep -v \.gradle |grep kotlin$)

${searchForbiddenStringsScript} ./tools/check/forbidden_strings_in_code.txt "$allKotlinDirs"

resultForbiddenStringInCode=$?

echo
echo "Search for forbidden patterns in XML resource files..."

# list all res folders of the project.
allResDirs=$(find . -type d |grep -v build |grep -v \.git |grep -v \.gradle |grep /res$)

${searchForbiddenStringsScript} ./tools/check/forbidden_strings_in_xml.txt "$allResDirs"

resultForbiddenStringInXml=$?

if [[ ${resultForbiddenStringInCode} -eq 0 ]] \
   && [[ ${resultForbiddenStringInXml} -eq 0 ]]; then
   echo "OK"
else
   echo "❌ ERROR, please check the logs above."
   exit 1
fi
