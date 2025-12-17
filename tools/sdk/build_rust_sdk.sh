#!/usr/bin/env bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# Exit on error
set -e

# Ask to build from local source or to clone the repository
read -p "Do you want to build the Rust SDK from local source (yes/no) default to yes? " buildLocal
buildLocal=${buildLocal:-yes}

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    date=$(date +%Y%m%d%H%M%S)
else
    date=$(gdate +%Y%m%d%H%M%S)
fi

elementPwd=$(pwd)

# Ask for the Rust SDK local source path
# if folder rustSdk/ exists, use it as default
if [ "${buildLocal}" == "yes" ]; then
    read -p "Please enter the path to the Rust SDK local source, default to ../matrix-rust-sdk" rustSdkPath
    rustSdkPath=${rustSdkPath:-../matrix-rust-sdk/}
    if [ ! -d "${rustSdkPath}" ]; then
        printf "\nFolder ${rustSdkPath} does not exist. Please clone the matrix-rust-sdk repository in the folder ../matrix-rust-sdk.\n\n"
        exit 0
    fi
else
    read -p "Please enter the Rust SDK repository url, default to https://github.com/matrix-org/matrix-rust-sdk.git " rustSdkUrl
    rustSdkUrl=${rustSdkUrl:-https://github.com/matrix-org/matrix-rust-sdk.git}
    read -p "Please enter the Rust SDK branch, default to main " rustSdkBranch
    rustSdkBranch=${rustSdkBranch:-main}
    cd ..
    git clone "${rustSdkUrl}" matrix-rust-sdk-"$date"
    cd matrix-rust-sdk-"$date"
    git checkout "${rustSdkBranch}"
    rustSdkPath=$(pwd)
    cd "${elementPwd}"
fi


cd "${rustSdkPath}"
git status

read -p "Will build with this version of the Rust SDK ^. Is it correct (yes/no) default to yes? " sdkCorrect
sdkCorrect=${sdkCorrect:-yes}

if [ "${sdkCorrect}" != "yes" ]; then
    exit 0
fi

# Ask if the user wants to build the app after
read -p "Do you want to build the app after (yes/no) default to no? " buildApp
buildApp=${buildApp:-no}

cd "${elementPwd}"

default_arch="$(uname -m)-linux-android"
# On ARM MacOS, `uname -m` returns arm64, but the toolchain is called aarch64
default_arch="${default_arch/arm64/aarch64}"

read -p "Enter the architecture you want to build for (default '$default_arch'): " target_arch
target_arch="${target_arch:-${default_arch}}"

# If folder ../matrix-rust-components-kotlin does not exist, clone the repo
if [ ! -d "../matrix-rust-components-kotlin" ]; then
    printf "\nFolder ../matrix-rust-components-kotlin does not exist. Cloning the repository into ../matrix-rust-components-kotlin.\n\n"
    git clone https://github.com/matrix-org/matrix-rust-components-kotlin.git ../matrix-rust-components-kotlin
fi

printf "\nResetting matrix-rust-components-kotlin to the latest main branch...\n\n"
cd ../matrix-rust-components-kotlin
git reset --hard
git checkout main
git pull

printf "\nBuilding the SDK for ${target_arch}...\n\n"
./scripts/build.sh -p "${rustSdkPath}" -m sdk -t "${target_arch}" -o "${elementPwd}/libraries/rustsdk"

cd "${elementPwd}"
mv ./libraries/rustsdk/sdk-android-debug.aar ./libraries/rustsdk/matrix-rust-sdk.aar
mkdir -p ./libraries/rustsdk/sdks
cp ./libraries/rustsdk/matrix-rust-sdk.aar ./libraries/rustsdk/sdks/matrix-rust-sdk-"${date}".aar


if [ "${buildApp}" == "yes" ]; then
    printf "\nBuilding the application...\n\n"
    ./gradlew assembleDebug
fi

if [ "${buildLocal}" == "no" ]; then
    printf "\nCleaning up...\n\n"
    rm -rf ../matrix-rust-sdk-"$date"
fi

printf "\nDone!\n"
