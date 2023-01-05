#!/usr/bin/env bash

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

printf "\n"
printf "================================================================================\n"
printf "|                    Importing strings from Element                            |\n"
printf "================================================================================\n"

basedir=`pwd`
tmpPath="${basedir}/tmpStrings"

## Delete tmp dir
#rm -rf ${tmpPath}

# Create tmp dir
mkdir ${tmpPath}

printf "\n================================================================================\n"
printf "Downloading Element Android source from develop...\n"

curl https://github.com/vector-im/element-android/archive/refs/heads/develop.zip -i -L -o ${tmpPath}/element.zip

printf "\n================================================================================\n"
printf "Unzipping Element Android source...\n"

unzip -q ${tmpPath}/element.zip -d ${tmpPath}

printf "\n================================================================================\n"
printf "Importing the strings...\n"
elementAndroidPath="${tmpPath}/element-android-develop"

cp -R ${elementAndroidPath}/library/ui-strings/src/main/res ${basedir}/libraries/ui-strings/src/main

## Delete tmp dir
rm -rf ${tmpPath}

# Commit all changes to git
# git commit -a -m "Import strings from Element Android"

printf "\n================================================================================\n"
printf "Done\n"
printf "================================================================================\n"
