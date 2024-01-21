#!/usr/bin/env bash

# Exit on error
set -e

d=$(date +%Y%m%d%H%M%S)
script_dir="$(dirname "$0")"
exa_dir="$(readlink -f "$script_dir"/../..)"

rust_sdk_dir=~/code/matrix-rust-sdk-project-neo
matrix_rust_components_kotlin_dir=~/code/matrix-rust-components-kotlin

"${matrix_rust_components_kotlin_dir}/scripts/build.sh" \
    -p "${rust_sdk_dir}" \
    -m sdk \
    -t aarch64-linux-android \
    -o "${exa_dir}/libraries/rustsdk" \
    -d

mv "${exa_dir}/libraries/rustsdk/sdk-android-debug.aar" "${exa_dir}/libraries/rustsdk/matrix-rust-sdk.aar"
mkdir -p "${exa_dir}/libraries/rustsdk/sdks"

./gradlew assembleDebug
