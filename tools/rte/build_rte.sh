#!/usr/bin/env bash

# Copyright (c) 2025 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# Exit on error
set -e

# Ask to build from local source or to clone the repository
read -p "Do you want to build the RTE from local source (yes/no) default to yes? " buildLocal
buildLocal=${buildLocal:-yes}

date=$(gdate +%Y%m%d%H%M%S)
elementPwd=$(pwd)

# Ask for the RTE local source path
# if folder rte/ exists, use it as default
if [ "${buildLocal}" == "yes" ]; then
    read -p "Please enter the path to the Rust SDK local source, default to ../matrix-rich-text-editor: " rtePath
    rtePath=${rtePath:-../matrix-rich-text-editor/}
    if [ ! -d "${rtePath}" ]; then
        printf "\nFolder ${rtePath} does not exist. Please clone the matrix-rich-text-editor repository in the folder ../matrix-rich-text-editor.\n\n"
        exit 0
    fi
else
    read -p "Please enter the RTE repository url, default to https://github.com/matrix-org/matrix-rich-text-editor.git " rteUrl
    rteUrl=${rteUrl:-https://github.com/matrix-org/matrix-rich-text-editor.git}
    read -p "Please enter the Rust SDK branch, default to main " rteBranch
    rteBranch=${rteBranch:-main}
    cd ..
    git clone "${rteUrl}" matrix-rich-text-editor-"$date"
    cd matrix-rich-text-editor-"$date"
    git checkout "${rteBranch}"
    rtePath=$(pwd)
    cd "${elementPwd}"
fi


cd "${rtePath}"
git status

read -p "Will build with this version of the RTE ^. Is it correct (yes/no) default to yes? " rteCorrect
rteCorrect=${rteCorrect:-yes}

if [ "${rteCorrect}" != "yes" ]; then
    exit 0
fi

# Ask if the user wants to build the app after
read -p "Do you want to build the app after (yes/no) default to yes? " buildApp
buildApp=${buildApp:-yes}

cd "${elementPwd}"

cd "$rtePath"

printf "\nBuilding the RTE for aarch64...\n\n"
make android-bindings-aarch64
cd platforms/android
./gradlew clean :library:assembleRelease :library-compose:assembleRelease
cp ./library/build/outputs/aar/library-release.aar "$elementPwd"/libraries/textcomposer/lib/library.aar
cp ./library-compose/build/outputs/aar/library-compose-release.aar "$elementPwd"/libraries/textcomposer/lib/library-compose.aar

cd "${elementPwd}"
mkdir -p ./libraries/textcomposer/lib/versions
cp ./libraries/textcomposer/lib/library.aar ./libraries/textcomposer/lib/versions/library-"${date}".aar
cp ./libraries/textcomposer/lib/library-compose.aar ./libraries/textcomposer/lib/versions/library-compose-"${date}".aar


if [ "${buildApp}" == "yes" ]; then
    printf "\nBuilding the application...\n\n"
    ./gradlew assembleDebug
fi

if [ "${buildLocal}" == "no" ]; then
    printf "\nCleaning up...\n\n"
    rm -rf ../matrix-rich-text-editor-"$date"
fi

printf "\nDone!\n"
