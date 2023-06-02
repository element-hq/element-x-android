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

if [[ -z ${GITHUB_REF_NAME} ]]; then
  echo "Missing GITHUB_REF_NAME variable"
  exit 1
fi

git config user.name "ElementBot"
git config user.email "benoitm+elementbot@element.io"

echo "Git status"
git status

echo "Fetching..."
git fetch --all

echo "Checkout origin/$GITHUB_REF_NAME"
git checkout "origin/$GITHUB_REF_NAME"

echo "Record screenshots"
./gradlew recordPaparazziDebug --stacktrace -PpreDexEnable=false --max-workers 4 --warn

echo "Committing changes"
git add -A
git commit -m "Update screenshots"

echo "Pushing changes"
git push "https://$GITHUB_TOKEN@github.com/$GITHUB_REPOSITORY.git"
echo "Done!"
