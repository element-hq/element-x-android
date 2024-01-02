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

set -e

TOKEN=$GITHUB_TOKEN
REPO=$GITHUB_REPOSITORY

SHORT=t:,r:
LONG=token:,repo:
OPTS=$(getopt -a -n recordScreenshots --options $SHORT --longoptions $LONG -- "$@")

eval set -- "$OPTS"
while :
do
  case "$1" in
    -t | --token )
      TOKEN="$2"
      shift 2
      ;;
    -r | --repo )
      REPO="$2"
      shift 2
      ;;
    --)
      shift;
      break
      ;;
    *)
      echo "Unexpected option: $1"
      help
      ;;
  esac
done

BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo Branch used: $BRANCH

if [[ -z ${TOKEN} ]]; then
  echo "No token specified, either set the env var GITHUB_TOKEN or use the --token option"
  exit 1
fi

if [[ -z ${REPO} ]]; then
  echo "No repo specified, either set the env var GITHUB_REPOSITORY or use the --repo option"
  exit 1
fi

echo "Deleting previous screenshots"
./gradlew removeOldSnapshots --stacktrace -PpreDexEnable=false --max-workers 4 --warn

echo "Record screenshots"
./gradlew recordPaparazziDebug --stacktrace -PpreDexEnable=false --max-workers 4 --warn

echo "Committing changes"
git config http.sslVerify false

if [[ -z ${INPUT_AUTHOR_NAME} ]]; then
  git config user.name "ElementBot"
else
  git config --local user.name "${INPUT_AUTHOR_NAME}"
fi

if [[ -z ${INPUT_AUTHOR_EMAIL} ]]; then
  git config user.email "benoitm+elementbot@element.io"
else
  git config --local user.name "${INPUT_AUTHOR_EMAIL}"
fi
git add -A
git commit -m "Update screenshots"

GITHUB_REPO="https://$GITHUB_ACTOR:$TOKEN@github.com/$REPO.git"
echo "Pushing changes"
if [[ -z ${GITHUB_ACTOR} ]]; then
  echo "No GITHUB_ACTOR env var"
  GITHUB_REPO="https://$TOKEN@github.com/$REPO.git"
fi
git push $GITHUB_REPO "$BRANCH"
echo "Done!"
