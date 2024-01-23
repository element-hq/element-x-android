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

# do not exit when any command fails (issue with git flow)
set +e

printf "\n================================================================================\n"
printf "|                    Welcome to the release script!                            |\n"
printf "================================================================================\n"

printf "Checking environment...\n"
envError=0

# Check that bundletool is installed
if ! command -v bundletool &> /dev/null
then
    printf "Fatal: bundletool is not installed. You can install it running \`brew install bundletool\`\n"
    envError=1
fi

# Path of the key store (it's a file)
keyStorePath="${ELEMENT_X_KEYSTORE_PATH}"
if [[ -z "${keyStorePath}" ]]; then
    printf "Fatal: ELEMENT_X_KEYSTORE_PATH is not defined in the environment.\n"
    envError=1
fi
# Keystore password
keyStorePassword="${ELEMENT_X_KEYSTORE_PASSWORD}"
if [[ -z "${keyStorePassword}" ]]; then
    printf "Fatal: ELEMENT_X_KEYSTORE_PASSWORD is not defined in the environment.\n"
    envError=1
fi
# Key password
keyPassword="${ELEMENT_X_KEY_PASSWORD}"
if [[ -z "${keyPassword}" ]]; then
    printf "Fatal: ELEMENT_X_KEY_PASSWORD is not defined in the environment.\n"
    envError=1
fi
# GitHub token
gitHubToken="${ELEMENT_GITHUB_TOKEN}"
if [[ -z "${gitHubToken}" ]]; then
    printf "Fatal: ELEMENT_GITHUB_TOKEN is not defined in the environment.\n"
    envError=1
fi
# Android home
androidHome="${ANDROID_HOME}"
if [[ -z "${androidHome}" ]]; then
    printf "Fatal: ANDROID_HOME is not defined in the environment.\n"
    envError=1
fi
# @elementbot:matrix.org matrix token / Not mandatory
elementBotToken="${ELEMENT_BOT_MATRIX_TOKEN}"
if [[ -z "${elementBotToken}" ]]; then
    printf "Warning: ELEMENT_BOT_MATRIX_TOKEN is not defined in the environment.\n"
fi

if [ ${envError} == 1 ]; then
  exit 1
fi

minSdkVersion=23
buildToolsVersion="32.0.0"
buildToolsPath="${androidHome}/build-tools/${buildToolsVersion}"

if [[ ! -d ${buildToolsPath} ]]; then
    printf "Fatal: ${buildToolsPath} folder not found, ensure that you have installed the SDK version ${buildToolsVersion}.\n"
    exit 1
fi

# Check if git flow is enabled
gitFlowDevelop=`git config gitflow.branch.develop`
if [[ ${gitFlowDevelop} != "" ]]
then
    printf "Git flow is initialized\n"
else
    printf "Git flow is not initialized. Initializing...\n"
    ./tools/gitflow/gitflow-init.sh
fi

printf "OK\n"

printf "\n================================================================================\n"
printf "Ensuring main and develop branches are up to date...\n"

git checkout main
git pull
git checkout develop
git pull

printf "\n================================================================================\n"
# Guessing version to propose a default version
versionsFile="./plugins/src/main/kotlin/Versions.kt"
versionMajorCandidate=`grep "val versionMajor" ${versionsFile} | cut  -d " " -f6`
versionMinorCandidate=`grep "val versionMinor" ${versionsFile} | cut  -d " " -f6`
versionPatchCandidate=`grep "val versionPatch" ${versionsFile} | cut  -d " " -f6`
versionCandidate="${versionMajorCandidate}.${versionMinorCandidate}.${versionPatchCandidate}"

read -p "Please enter the release version (example: ${versionCandidate}). Just press enter if ${versionCandidate} is correct. " version
version=${version:-${versionCandidate}}

# extract major, minor and patch for future use
versionMajor=`echo ${version} | cut  -d "." -f1`
versionMinor=`echo ${version} | cut  -d "." -f2`
versionPatch=`echo ${version} | cut  -d "." -f3`
nextPatchVersion=$((versionPatch + 2))

printf "\n================================================================================\n"
printf "Starting the release ${version}\n"
git flow release start ${version}

# Note: in case the release is already started and the script is started again, checkout the release branch again.
ret=$?
if [[ $ret -ne 0 ]]; then
  printf "Mmh, it seems that the release is already started. Checking out the release branch...\n"
  git checkout "release/${version}"
fi

# Ensure version is OK
versionsFileBak="${versionsFile}.bak"
cp ${versionsFile} ${versionsFileBak}
sed "s/private const val versionMajor = .*/private const val versionMajor = ${versionMajor}/" ${versionsFileBak} > ${versionsFile}
sed "s/private const val versionMinor = .*/private const val versionMinor = ${versionMinor}/" ${versionsFile}    > ${versionsFileBak}
sed "s/private const val versionPatch = .*/private const val versionPatch = ${versionPatch}/" ${versionsFileBak} > ${versionsFile}
rm ${versionsFileBak}

# This commit may have no effect because generally we do not change the version during the release.
git commit -a -m "Setting version for the release ${version}"

printf "\n================================================================================\n"
printf "Building the bundle locally first...\n"
./gradlew clean app:bundleGplayRelease

printf "\n================================================================================\n"
printf "Running towncrier...\n"
yes | towncrier build --version "v${version}"

printf "\n================================================================================\n"
read -p "Check the file CHANGES.md consistency. It's possible to reorder items (most important changes first) or change their section if relevant. Also an opportunity to fix some typo, or rewrite things. Do not commit your change. Press enter to continue. "

# Get the changes to use it to create the GitHub release
changelogUrlEncoded=`git diff CHANGES.md | grep ^+ | tail -n +2 | cut -c2- | jq -sRr @uri | sed s/\(/%28/g | sed s/\)/%29/g`

printf "\n================================================================================\n"
printf "Committing...\n"
git commit -a -m "Changelog for version ${version}"

printf "\n================================================================================\n"
printf "Creating fastlane file...\n"
printf -v versionMajor2Digits "%02d" ${versionMajor}
printf -v versionMinor2Digits "%02d" ${versionMinor}
printf -v versionPatch2Digits "%02d" ${versionPatch}
fastlaneFile="4${versionMajor2Digits}${versionMinor2Digits}${versionPatch2Digits}0.txt"
fastlanePathFile="./fastlane/metadata/android/en-US/changelogs/${fastlaneFile}"
printf "Main changes in this version: TODO.\nFull changelog: https://github.com/element-hq/element-x-android/releases" > ${fastlanePathFile}

read -p "I have created the file ${fastlanePathFile}, please edit it and press enter to continue. "
git add ${fastlanePathFile}
git commit -a -m "Adding fastlane file for version ${version}"

printf "\n================================================================================\n"
printf "OK, finishing the release...\n"
git flow release finish "${version}"

printf "\n================================================================================\n"
read -p "Done, push the branch 'main' and the new tag (yes/no) default to yes? " doPush
doPush=${doPush:-yes}

if [ ${doPush} == "yes" ]; then
  printf "Pushing branch 'main' and tag 'v${version}'...\n"
  git push origin main
  git push origin "v${version}"
else
    printf "Not pushing, do not forget to push manually!\n"
fi

printf "\n================================================================================\n"
printf "Checking out develop...\n"
git checkout develop

# Set next version
printf "\n================================================================================\n"
printf "Setting next version on file '${versionsFile}'...\n"
cp ${versionsFile} ${versionsFileBak}
sed "s/private const val versionPatch = .*/private const val versionPatch = ${nextPatchVersion}/" ${versionsFileBak} > ${versionsFile}
rm ${versionsFileBak}

printf "\n================================================================================\n"
read -p "I have updated the versions to prepare the next release, please check that the change are correct and press enter so I can commit. "

printf "Committing...\n"
git commit -a -m 'version++'

printf "\n================================================================================\n"
read -p "Done, push the branch 'develop' (yes/no) default to yes? (A rebase may be necessary in case develop got new commits) " doPush
doPush=${doPush:-yes}

if [ ${doPush} == "yes" ]; then
  printf "Pushing branch 'develop'...\n"
  git push origin develop
else
    printf "Not pushing, do not forget to push manually!\n"
fi

printf "\n================================================================================\n"
printf "Wait for the GitHub action https://github.com/element-hq/element-x-android/actions/workflows/release.yml?query=branch%%3Amain to build the 'main' branch.\n"
read -p "After GHA is finished, please enter the artifact URL (for 'elementx-app-gplay-bundle-unsigned'): " artifactUrl

printf "\n================================================================================\n"
printf "Downloading the artifact...\n"

 # Download files
targetPath="./tmp/Element/${version}"

python3 ./tools/github/download_github_artifacts.py \
     --token ${gitHubToken} \
     --artifactUrl ${artifactUrl} \
     --directory ${targetPath} \
     --ignoreErrors

printf "\n================================================================================\n"
printf "Unzipping the artifact...\n"

unzip ${targetPath}/elementx-app-gplay-bundle-unsigned.zip -d ${targetPath}

unsignedBundlePath="${targetPath}/app-release.aab"
signedBundlePath="${targetPath}/app-release-signed.aab"

printf "\n================================================================================\n"
printf "Signing file ${unsignedBundlePath} with build-tools version ${buildToolsVersion} for min SDK version ${minSdkVersion}...\n"

cp ${unsignedBundlePath} ${signedBundlePath}

${buildToolsPath}/apksigner sign \
    -v \
    --ks ${keyStorePath} \
    --ks-pass pass:${keyStorePassword} \
    --ks-key-alias elementx \
    --key-pass pass:${keyPassword} \
    --min-sdk-version ${minSdkVersion} \
    ${signedBundlePath}

printf "\n================================================================================\n"
printf "Please check the information below:\n"

printf "Version code: "
bundletool dump manifest --bundle=${signedBundlePath} --xpath=/manifest/@android:versionCode
printf "Version name: "
bundletool dump manifest --bundle=${signedBundlePath} --xpath=/manifest/@android:versionName

printf "\n"
read -p "Does it look correct? Press enter to continue. "

printf "\n================================================================================\n"
printf "The file ${signedBundlePath} has been signed and can be uploaded to the PlayStore!\n"

printf "\n================================================================================\n"
read -p "Do you want to build the APKs from the app bundle? You need to do this step if you want to install the application to your device. (yes/no) default to yes " doBuildApks
doBuildApks=${doBuildApks:-yes}

if [ ${doBuildApks} == "yes" ]; then
  printf "Building apks...\n"
  bundletool build-apks --bundle=${signedBundlePath} --output=${targetPath}/elementx.apks \
      --ks=./app/signature/debug.keystore --ks-pass=pass:android --ks-key-alias=androiddebugkey --key-pass=pass:android \
      --overwrite

  read -p "Do you want to install the application to your device? Make sure there is one (and only one!) connected device first. (yes/no) default to yes " doDeploy
  doDeploy=${doDeploy:-yes}
  if [ ${doDeploy} == "yes" ]; then
    printf "Installing apk for your device...\n"
    bundletool install-apks --apks=${targetPath}/elementx.apks
    read -p "Please run the application on your phone to check that the upgrade went well. Press enter to continue. "
  else
    printf "APK will not be deployed!\n"
  fi
else
  printf "APKs will not be generated!\n"
fi

printf "\n================================================================================\n"
githubCreateReleaseLink="https://github.com/element-hq/element-x-android/releases/new?tag=v${version}&title=Element%20X%20Android%20v${version}&body=${changelogUrlEncoded}"
printf "Creating the release on gitHub.\n"
printf -- "Open this link: %s\n" ${githubCreateReleaseLink}
printf "Then\n"
printf " - copy paste the section of the file CHANGES.md for this release (if not there yet)\n"
printf " - click on the 'Generate releases notes' button\n"
printf " - Add the file ${signedBundlePath} to the GitHub release.\n"
read -p ". Press enter to continue. "

printf "\n================================================================================\n"
printf "Message for the Android internal room:\n\n"
message="@room Element X Android ${version} is ready to be tested. You can get it from https://github.com/element-hq/element-x-android/releases/tag/v${version}. Installation instructions can be found [here](https://github.com/element-hq/element-x-android/blob/develop/docs/install_from_github_release.md). Please report any feedback. Thanks!"
printf "${message}\n\n"

if [[ -z "${elementBotToken}" ]]; then
  read -p "ELEMENT_BOT_MATRIX_TOKEN is not defined in the environment. Cannot send the message for you. Please send it manually, and press enter to continue. "
else
  read -p "Send this message to the room (yes/no) default to yes? " doSend
  doSend=${doSend:-yes}
  if [ ${doSend} == "yes" ]; then
    printf "Sending message...\n"
    transactionId=`openssl rand -hex 16`
    # Element Android internal
    matrixRoomId="!LiSLXinTDCsepePiYW:matrix.org"
    curl -X PUT --data $"{\"msgtype\":\"m.text\",\"body\":\"${message}\"}" -H "Authorization: Bearer ${elementBotToken}" https://matrix-client.matrix.org/_matrix/client/r0/rooms/${matrixRoomId}/send/m.room.message/\$local.${transactionId}
  else
    printf "Message not sent, please send it manually!\n"
  fi
fi

printf "\n================================================================================\n"
printf "Congratulation! Kudos for using this script! Have a nice day!\n"
printf "================================================================================\n"
