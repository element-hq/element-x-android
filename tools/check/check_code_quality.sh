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
echo "Search for forbidden patterns in code..."

# list all Kotlin folders of the project.
allKotlinDirs=`find . -type d |grep -v build |grep -v \.git |grep -v \.gradle |grep kotlin$`

${searchForbiddenStringsScript} ./tools/check/forbidden_strings_in_code.txt $allKotlinDirs

resultForbiddenStringInCode=$?

if [[ ${resultForbiddenStringInCode} -eq 0 ]]; then
   echo "MAIN OK"
else
   echo "❌ MAIN ERROR"
   exit 1
fi
