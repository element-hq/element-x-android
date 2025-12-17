#!/usr/bin/env bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

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

# Read minSdkVersion from file plugins/src/main/kotlin/Versions.kt
minSdkVersion=$(grep "MIN_SDK_FOSS =" ./plugins/src/main/kotlin/Versions.kt |cut -d '=' -f 2 |xargs)
# Read buildToolsVersion from file plugins/src/main/kotlin/Versions.kt
buildToolsVersion=$(grep "BUILD_TOOLS_VERSION =" ./plugins/src/main/kotlin/Versions.kt |cut -d '=' -f 2 |xargs)
buildToolsPath="${androidHome}/build-tools/${buildToolsVersion}"

if [[ ! -d ${buildToolsPath} ]]; then
    printf "Fatal: %s folder not found, ensure that you have installed the SDK version %s.\n" "${buildToolsPath}" "${buildToolsVersion}"
    exit 1
fi

# Check if git flow is enabled
gitFlowDevelop=$(git config gitflow.branch.develop)
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
# Get current year on 2 digits
versionYearCandidate=$(date +%y)
currentVersionMonth=$(grep "val versionMonth" ${versionsFile} | cut  -d " " -f6)
# Get current month on 2 digits
versionMonthCandidate=$(date +%m)
versionMonthCandidateNoLeadingZero=${versionMonthCandidate/#0/}
currentVersionReleaseNumber=$(grep "val versionReleaseNumber" ${versionsFile} | cut  -d " " -f6)
# if the current month is the same as the current version, we increment the release number, else we reset it to 0
if [[ ${currentVersionMonth} -eq ${versionMonthCandidateNoLeadingZero} ]]; then
  versionReleaseNumberCandidate=$((currentVersionReleaseNumber + 1))
else
  versionReleaseNumberCandidate=0
fi
versionCandidate="${versionYearCandidate}.${versionMonthCandidate}.${versionReleaseNumberCandidate}"

read -r -p "Please enter the release version (example: ${versionCandidate}). Format must be 'YY.MM.x' or 'YY.MM.xy'. Just press enter if ${versionCandidate} is correct. " version
version=${version:-${versionCandidate}}

# extract year, month and release number for future use
versionYear=$(echo "${version}" | cut  -d "." -f1)
versionMonth=$(echo "${version}" | cut  -d "." -f2)
versionMonthNoLeadingZero=${versionMonth/#0/}
versionReleaseNumber=$(echo "${version}" | cut  -d "." -f3)

printf "\n================================================================================\n"
printf "Starting the release %s\n" "${version}"
git flow release start "${version}"

# Note: in case the release is already started and the script is started again, checkout the release branch again.
ret=$?
if [[ $ret -ne 0 ]]; then
  printf "Mmh, it seems that the release is already started. Checking out the release branch...\n"
  git checkout "release/${version}"
fi

# Ensure version is OK
versionsFileBak="${versionsFile}.bak"
cp ${versionsFile} ${versionsFileBak}
sed "s/private const val versionYear = .*/private const val versionYear = ${versionYear}/" ${versionsFileBak} > ${versionsFile}
sed "s/private const val versionMonth = .*/private const val versionMonth = ${versionMonthNoLeadingZero}/" ${versionsFile}    > ${versionsFileBak}
sed "s/private const val versionReleaseNumber = .*/private const val versionReleaseNumber = ${versionReleaseNumber}/" ${versionsFileBak} > ${versionsFile}
rm ${versionsFileBak}

git commit -a -m "Setting version for the release ${version}"

printf "\n================================================================================\n"
printf "Creating fastlane file...\n"
printf -v versionReleaseNumber2Digits "%02d" "${versionReleaseNumber}"
fastlaneFile="20${versionYear}${versionMonth}${versionReleaseNumber2Digits}0.txt"
fastlanePathFile="./fastlane/metadata/android/en-US/changelogs/${fastlaneFile}"
printf "Main changes in this version: bug fixes and improvements.\nFull changelog: https://github.com/element-hq/element-x-android/releases" > "${fastlanePathFile}"

read -r -p "I have created the file ${fastlanePathFile}, please edit it and press enter to continue. "
git add "${fastlanePathFile}"
git commit -a -m "Adding fastlane file for version ${version}"

printf "\n================================================================================\n"
printf "OK, finishing the release...\n"
git flow release finish "${version}"

printf "\n================================================================================\n"
read -r -p "Done, push the branch 'main' and the new tag (yes/no) default to yes? " doPush
doPush=${doPush:-yes}

if [ "${doPush}" == "yes" ]; then
  printf "Pushing branch 'main' and tag 'v%s'...\n" "${version}"
  git push origin main
  git push origin "v${version}"
else
    printf "Not pushing, do not forget to push manually!\n"
fi

printf "\n================================================================================\n"
printf "Checking out develop...\n"
git checkout develop

printf "\n================================================================================\n"
printf "The GitHub action https://github.com/element-hq/element-x-android/actions/workflows/release.yml?query=branch%%3Amain should have start a new run.\n"
read -r -p "Please enter the url of the run, no need to wait for it to complete (example: https://github.com/element-hq/element-x-android/actions/runs/9065756777): " runUrl

targetPath="./tmp/Element/${version}"

printf "\n================================================================================\n"
printf "Downloading the artifacts...\n"

ret=1

while [[ $ret -ne 0 ]]; do
  python3 ./tools/github/download_all_github_artifacts.py \
     --token "${gitHubToken}" \
     --runUrl "${runUrl}" \
     --directory "${targetPath}"

  ret=$?
  if [[ $ret -ne 0 ]]; then
    read -r -p "Error while downloading the artifacts. You may want to fix the issue and retry. Retry (yes/no) default to yes? " doRetry
    doRetry=${doRetry:-yes}
    if [ "${doRetry}" == "no" ]; then
      exit 1
    fi
  fi
done

printf "\n================================================================================\n"
printf "Unzipping the F-Droid artifact...\n"

fdroidTargetPath="${targetPath}/fdroid"
unzip "${targetPath}"/elementx-app-fdroid-apks-unsigned.zip -d "${fdroidTargetPath}"

printf "\n================================================================================\n"
printf "Patching the FDroid APKs using inplace-fix.py...\n"

inplaceFixScript="./tools/release/inplace-fix.py"
python3 "${inplaceFixScript}" --page-size 16 fix-pg-map-id "${fdroidTargetPath}"/app-fdroid-arm64-v8a-release.apk   '0000000'
python3 "${inplaceFixScript}" --page-size 16 fix-pg-map-id "${fdroidTargetPath}"/app-fdroid-armeabi-v7a-release.apk '0000000'
python3 "${inplaceFixScript}" --page-size 16 fix-pg-map-id "${fdroidTargetPath}"/app-fdroid-x86-release.apk         '0000000'
python3 "${inplaceFixScript}" --page-size 16 fix-pg-map-id "${fdroidTargetPath}"/app-fdroid-x86_64-release.apk      '0000000'

printf "\n================================================================================\n"
printf "Signing the FDroid APKs...\n"

cp "${fdroidTargetPath}"/app-fdroid-arm64-v8a-release.apk \
   "${fdroidTargetPath}"/app-fdroid-arm64-v8a-release-signed.apk
"${buildToolsPath}"/apksigner sign \
       -v \
       --alignment-preserved true \
       --ks "${keyStorePath}" \
       --ks-pass pass:"${keyStorePassword}" \
       --ks-key-alias elementx \
       --key-pass pass:"${keyPassword}" \
       --min-sdk-version "${minSdkVersion}" \
       "${fdroidTargetPath}"/app-fdroid-arm64-v8a-release-signed.apk

cp "${fdroidTargetPath}"/app-fdroid-armeabi-v7a-release.apk \
   "${fdroidTargetPath}"/app-fdroid-armeabi-v7a-release-signed.apk
"${buildToolsPath}"/apksigner sign \
       -v \
       --alignment-preserved true \
       --ks "${keyStorePath}" \
       --ks-pass pass:"${keyStorePassword}" \
       --ks-key-alias elementx \
       --key-pass pass:"${keyPassword}" \
       --min-sdk-version "${minSdkVersion}" \
       "${fdroidTargetPath}"/app-fdroid-armeabi-v7a-release-signed.apk

cp "${fdroidTargetPath}"/app-fdroid-x86-release.apk \
   "${fdroidTargetPath}"/app-fdroid-x86-release-signed.apk
"${buildToolsPath}"/apksigner sign \
       -v \
       --alignment-preserved true \
       --ks "${keyStorePath}" \
       --ks-pass pass:"${keyStorePassword}" \
       --ks-key-alias elementx \
       --key-pass pass:"${keyPassword}" \
       --min-sdk-version "${minSdkVersion}" \
       "${fdroidTargetPath}"/app-fdroid-x86-release-signed.apk

cp "${fdroidTargetPath}"/app-fdroid-x86_64-release.apk \
   "${fdroidTargetPath}"/app-fdroid-x86_64-release-signed.apk
"${buildToolsPath}"/apksigner sign \
       -v \
       --alignment-preserved true \
       --ks "${keyStorePath}" \
       --ks-pass pass:"${keyStorePassword}" \
       --ks-key-alias elementx \
       --key-pass pass:"${keyPassword}" \
       --min-sdk-version "${minSdkVersion}" \
       "${fdroidTargetPath}"/app-fdroid-x86_64-release-signed.apk

printf "\n================================================================================\n"
printf "Please check the information below:\n"

printf "File app-fdroid-arm64-v8a-release-signed.apk:\n"
"${buildToolsPath}"/aapt dump badging "${fdroidTargetPath}"/app-fdroid-arm64-v8a-release-signed.apk | grep package
printf "File app-fdroid-armeabi-v7a-release-signed.apk:\n"
"${buildToolsPath}"/aapt dump badging "${fdroidTargetPath}"/app-fdroid-armeabi-v7a-release-signed.apk | grep package
printf "File app-fdroid-x86-release-signed.apk:\n"
"${buildToolsPath}"/aapt dump badging "${fdroidTargetPath}"/app-fdroid-x86-release-signed.apk | grep package
printf "File app-fdroid-x86_64-release-signed.apk:\n"
"${buildToolsPath}"/aapt dump badging "${fdroidTargetPath}"/app-fdroid-x86_64-release-signed.apk | grep package

printf "\n"
read -r -p "Does it look correct? Press enter when it's done. "

printf "\n================================================================================\n"
printf "The APKs in %s have been signed!\n" "${fdroidTargetPath}"

printf "\n================================================================================\n"
printf "Unzipping the Gplay artifact...\n"

gplayTargetPath="${targetPath}/gplay"
unzip "${targetPath}"/elementx-app-gplay-bundle-unsigned.zip -d "${gplayTargetPath}"

unsignedBundlePath="${gplayTargetPath}/app-gplay-release.aab"
signedBundlePath="${gplayTargetPath}/app-gplay-release-signed.aab"

printf "\n================================================================================\n"
printf "Signing file %s with build-tools version %s for min SDK version %s...\n" "${unsignedBundlePath}" "${buildToolsVersion}" "${minSdkVersion}"

cp "${unsignedBundlePath}" "${signedBundlePath}"

"${buildToolsPath}"/apksigner sign \
    -v \
    --ks "${keyStorePath}" \
    --ks-pass pass:"${keyStorePassword}" \
    --ks-key-alias elementx \
    --key-pass pass:"${keyPassword}" \
    --min-sdk-version "${minSdkVersion}" \
    "${signedBundlePath}"

printf "\n================================================================================\n"
printf "Please check the information below:\n"

printf "Version code: "
bundletool dump manifest --bundle="${signedBundlePath}" --xpath=/manifest/@android:versionCode
printf "Version name: "
bundletool dump manifest --bundle="${signedBundlePath}" --xpath=/manifest/@android:versionName

printf "\n"
read -r -p "Does it look correct? Press enter to continue. "

printf "\n================================================================================\n"
printf "The file %s has been signed and can be uploaded to the PlayStore!\n" "${signedBundlePath}"

printf "\n================================================================================\n"
read -r -p "Do you want to build the APKs from the app bundle? You need to do this step if you want to install the application to your device. (yes/no) default to no " doBuildApks
doBuildApks=${doBuildApks:-no}

if [ "${doBuildApks}" == "yes" ]; then
  printf "Building apks...\n"
  bundletool build-apks --bundle="${signedBundlePath}" --output="${gplayTargetPath}"/elementx.apks \
      --ks=./app/signature/debug.keystore --ks-pass=pass:android --ks-key-alias=androiddebugkey --key-pass=pass:android \
      --overwrite

  read -r -p "Do you want to install the application to your device? Make sure there is one (and only one!) connected device first. (yes/no) default to yes " doDeploy
  doDeploy=${doDeploy:-yes}
  if [ "${doDeploy}" == "yes" ]; then
    printf "Installing apk for your device...\n"
    bundletool install-apks --apks="${gplayTargetPath}"/elementx.apks
    read -r -p "Please run the application on your phone to check that the upgrade went well. Press enter to continue. "
  else
    printf "APK will not be deployed!\n"
  fi
else
  printf "APKs will not be generated!\n"
fi

printf "\n================================================================================\n"
printf "Create the open testing release on GooglePlay.\n"

printf "On GooglePlay console, go the the open testing section and click on \"Create new release\" button, then:\n"
printf " - upload the file %s.\n" "${signedBundlePath}"
printf " - copy the release note from the fastlane file.\n"
printf " - download the universal APK, to be able to provide it to the GitHub release: click on the right arrow next to the \"App bundle\", then click on the \"Download\" tab, and download the \"Signed, universal APK\".\n"
printf " - submit the release.\n"
read -r -p "Press enter to continue. "

printf "You can then go to \"Publishing overview\" and send the new release for a review by Google.\n"
read -r -p "Press enter to continue. "

printf "\n================================================================================\n"
githubCreateReleaseLink="https://github.com/element-hq/element-x-android/releases/new?tag=v${version}&title=Element%20X%20Android%20v${version}"
printf "Creating the release on gitHub.\n"
printf -- "Open this link: %s\n" "${githubCreateReleaseLink}"
printf "Then\n"
printf " - Click on the 'Generate releases notes' button.\n"
printf " - Optionally reorder items and fix typos.\n"
printf " - Add the file %s to the GitHub release.\n" "${signedBundlePath}"
printf " - Add the universal APK, downloaded from the GooglePlay console to the GitHub release.\n"
printf " - Add the 4 signed APKs for F-Droid, located at %s to the GitHub release.\n" "${fdroidTargetPath}"
read -r -p ". Press enter to continue. "

printf "\n================================================================================\n"
printf "Update the project release notes:\n\n"

read -r -p "Copy the content of the release note generated by GitHub to the file CHANGES.md and press enter to commit the change. "

printf "\n================================================================================\n"
printf "Committing...\n"
git commit -a -m "Changelog for version ${version}"

printf "\n================================================================================\n"
read -r -p "Done, push the branch 'develop' (yes/no) default to yes? (A rebase may be necessary in case develop got new commits) " doPush
doPush=${doPush:-yes}

if [ "${doPush}" == "yes" ]; then
  printf "Pushing branch 'develop'...\n"
  git push origin develop
else
    printf "Not pushing, do not forget to push manually!\n"
fi

printf "\n================================================================================\n"
printf "Message for the Android internal room:\n\n"
message="@room Element X Android ${version} is ready to be tested. You can get it from https://github.com/element-hq/element-x-android/releases/tag/v${version}. You can install the universal APK. If you want to install the application from the app bundle, you can follow instructions [here](https://github.com/element-hq/element-x-android/blob/develop/docs/install_from_github_release.md). Please report any feedback. Thanks!"
printf "%s\n\n" "${message}"

if [[ -z "${elementBotToken}" ]]; then
  read -r -p "ELEMENT_BOT_MATRIX_TOKEN is not defined in the environment. Cannot send the message for you. Please send it manually, and press enter to continue. "
else
  read -r -p "Send this message to the room (yes/no) default to yes? " doSend
  doSend=${doSend:-yes}
  if [ "${doSend}" == "yes" ]; then
    printf "Sending message...\n"
    transactionId=$(openssl rand -hex 16)
    # Element Android internal
    matrixRoomId="!LiSLXinTDCsepePiYW:matrix.org"
    curl -X PUT --data "{\"msgtype\":\"m.text\",\"body\":\"${message}\"}" -H "Authorization: Bearer ${elementBotToken}" https://matrix-client.matrix.org/_matrix/client/r0/rooms/${matrixRoomId}/send/m.room.message/\$local."${transactionId}"
  else
    printf "Message not sent, please send it manually!\n"
  fi
fi

printf "\n================================================================================\n"
printf "Congratulation! Kudos for using this script! Have a nice day!\n"
printf "================================================================================\n"
