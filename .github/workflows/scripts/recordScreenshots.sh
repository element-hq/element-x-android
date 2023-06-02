#!/bin/bash

#
# Copyright (c) 2023 New Vector Ltd
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

if [[ -z ${GITHUB_TOKEN} ]]; then
  echo "Missing GITHUB_TOKEN variable"
  exit 1
fi

if [[ -z ${GITHUB_REPOSITORY} ]]; then
  echo "Missing GITHUB_REPOSITORY variable"
  exit 1
fi

if [[ -z ${BRANCH} ]]; then
  echo "Missing BRANCH variable"
  exit 1
fi

./gradlew recordPaparazziDebug --stacktrace -PpreDexEnable=false --max-workers 4 --warn

git config user.name "ElementBot"
git config user.email "benoitm+elementbot@element.io"
git fetch --all
git checkout --track "origin/$BRANCH"
git add -A
git commit -m "Update screenshots"
git push "https://$GITHUB_TOKEN@github.com/$GITHUB_REPOSITORY.git"
echo "Done!"
