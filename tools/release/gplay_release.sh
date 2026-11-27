#!/usr/bin/env bash

# Copyright (c) 2026 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# Publish Element X on the open testing track of Google Play, using the Google Play Developer API.
# This script is called by releaseV2.sh, but it can also be run manually, for instance to resume a release.
#
# Usage:
#   gplay_release.sh check
#       Check that the credentials are working. Nothing is modified on Google Play.
#   gplay_release.sh upload <version> <versionCode> <signedAabPath> <releaseNotesFile>
#       Upload the signed app bundle, and create the release on the open testing track, with the given release notes.
#       A confirmation is asked before committing the change.
#   gplay_release.sh download-universal-apk <versionCode> <outputApkPath>
#       Download the universal APK generated and signed by Google Play for the given version code.
#
# Requirements: curl, jq and openssl, and the environment variable ELEMENT_X_GPLAY_SERVICE_ACCOUNT_PATH set to
# the path of the JSON key of a Google Cloud service account allowed to publish the application. To set it up:
#   - in a Google Cloud project, enable the "Google Play Android Developer API";
#   - create a service account in this project, and create a JSON key for it;
#   - in the Play Console, go to "Users and permissions" and invite the email of the service account, granting
#     access to Element X only, with the permission "Release to testing tracks".

packageName="io.element.android.x"
# "beta" is the name of the open testing track in the API.
track="beta"
releaseNotesLanguage="en-US"
apiBaseUrl="https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${packageName}"
uploadBaseUrl="https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/${packageName}"

fail() {
  printf "Fatal: %s\n" "$1" >&2
  exit 1
}

usage() {
  cat >&2 << EOF
Usage:
  $0 check
  $0 upload <version> <versionCode> <signedAabPath> <releaseNotesFile>
  $0 download-universal-apk <versionCode> <outputApkPath>
See the header of $0 for more details.
EOF
  exit 1
}

# Encode the standard input to base64url, without padding.
base64Url() {
  openssl base64 -A | tr '+/' '-_' | tr -d '='
}

# Get an OAuth access token for the service account, using a JWT signed with its private key.
# See https://developers.google.com/identity/protocols/oauth2/service-account#httprest
authenticate() {
  local keyFile="${ELEMENT_X_GPLAY_SERVICE_ACCOUNT_PATH}"
  [[ -n "${keyFile}" ]] || fail "ELEMENT_X_GPLAY_SERVICE_ACCOUNT_PATH is not defined in the environment."
  [[ -f "${keyFile}" ]] || fail "the file ${keyFile} (ELEMENT_X_GPLAY_SERVICE_ACCOUNT_PATH) does not exist."
  for tool in curl jq openssl; do
    command -v "${tool}" &> /dev/null || fail "${tool} is not installed."
  done

  local clientEmail tokenUri now header claims unsignedJwt signature response
  clientEmail=$(jq -r '.client_email // empty' "${keyFile}")
  tokenUri=$(jq -r '.token_uri // "https://oauth2.googleapis.com/token"' "${keyFile}")
  [[ -n "${clientEmail}" ]] || fail "the file ${keyFile} is not a service account key."

  now=$(date +%s)
  header=$(printf '{"alg":"RS256","typ":"JWT"}' | base64Url)
  claims=$(jq -cjn \
    --arg iss "${clientEmail}" \
    --arg aud "${tokenUri}" \
    --argjson iat "${now}" \
    '{iss: $iss, scope: "https://www.googleapis.com/auth/androidpublisher", aud: $aud, iat: $iat, exp: ($iat + 3600)}' \
    | base64Url)
  unsignedJwt="${header}.${claims}"
  # The private key is given through a pipe, so that it is never written to the disk.
  signature=$(printf "%s" "${unsignedJwt}" \
    | openssl dgst -sha256 -sign <(jq -r '.private_key' "${keyFile}") \
    | base64Url)
  [[ -n "${signature}" ]] || fail "unable to sign the authentication request with the key ${keyFile}."

  if ! response=$(curl -sS --fail-with-body -X POST "${tokenUri}" \
      --data-urlencode "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer" \
      --data-urlencode "assertion=${unsignedJwt}.${signature}"); then
    fail "unable to get an access token for ${clientEmail}: ${response}"
  fi
  accessToken=$(printf "%s" "${response}" | jq -r '.access_token // empty')
  [[ -n "${accessToken}" ]] || fail "no access token in the authentication response."
  printf "Authenticated on Google Play as %s.\n" "${clientEmail}"
}

# Call the API and print the response body. On error, print the error to stderr and return 1.
# Usage: api <method> <url> [extra curl arguments...]
# The access token is given through a pipe, so that it does not appear in the list of processes.
api() {
  local method="$1" url="$2" response
  shift 2
  if ! response=$(curl -sS --fail-with-body -X "${method}" \
      -H @<(printf "Authorization: Bearer %s\n" "${accessToken}") \
      "$@" "${url}"); then
    printf "Error: %s %s failed:\n%s\n" "${method}" "${url%%\?*}" "${response}" >&2
    return 1
  fi
  printf "%s" "${response}"
}

editId=""

createEdit() {
  local response
  response=$(api POST "${apiBaseUrl}/edits") || fail "unable to create an edit."
  editId=$(printf "%s" "${response}" | jq -r '.id // empty')
  [[ -n "${editId}" ]] || fail "no edit id in the response."
}

# Delete the current edit, discarding all its changes.
deleteEdit() {
  if [[ -n "${editId}" ]]; then
    api DELETE "${apiBaseUrl}/edits/${editId}" > /dev/null && printf "The changes have been discarded.\n"
    editId=""
  fi
}

# Discard the current edit and exit with an error.
abortEdit() {
  printf "Fatal: %s\n" "$1" >&2
  deleteEdit
  exit 1
}

check() {
  authenticate
  createEdit
  local response
  response=$(api GET "${apiBaseUrl}/edits/${editId}/tracks/${track}") || abortEdit "unable to read the track ${track}."
  printf "Current releases on the track %s: %s\n" "${track}" \
    "$(printf "%s" "${response}" | jq -c '[.releases[]? | {name, status, versionCodes}]')"
  deleteEdit
}

upload() {
  local version="$1" versionCode="$2" aabPath="$3" releaseNotesFile="$4"
  [[ -n "${releaseNotesFile}" ]] || usage
  [[ -f "${aabPath}" ]] || fail "the file ${aabPath} does not exist."
  [[ -f "${releaseNotesFile}" ]] || fail "the file ${releaseNotesFile} does not exist."

  authenticate
  createEdit

  printf "Uploading %s, this can take a while...\n" "${aabPath}"
  local response uploadedVersionCode
  response=$(api POST "${uploadBaseUrl}/edits/${editId}/bundles?uploadType=media" \
      -H "Content-Type: application/octet-stream" \
      --data-binary @"${aabPath}") || abortEdit "unable to upload the app bundle."
  uploadedVersionCode=$(printf "%s" "${response}" | jq -r '.versionCode // empty')
  if [[ "${uploadedVersionCode}" != "${versionCode}" ]]; then
    abortEdit "the uploaded app bundle has the version code ${uploadedVersionCode}, but ${versionCode} was expected."
  fi
  printf "The app bundle with version code %s has been uploaded.\n" "${uploadedVersionCode}"

  printf "Creating the release on the track %s...\n" "${track}"
  # The release name follows the format used by the Play Console: "versionCode (versionName)".
  local trackBody
  trackBody=$(jq -n \
    --arg track "${track}" \
    --arg name "${versionCode} (${version})" \
    --arg versionCode "${versionCode}" \
    --arg language "${releaseNotesLanguage}" \
    --rawfile notes "${releaseNotesFile}" \
    '{track: $track, releases: [{name: $name, versionCodes: [$versionCode], status: "completed", releaseNotes: [{language: $language, text: ($notes | rtrimstr("\n"))}]}]}')
  api PUT "${apiBaseUrl}/edits/${editId}/tracks/${track}" \
      -H "Content-Type: application/json" \
      --data "${trackBody}" > /dev/null || abortEdit "unable to update the track ${track}."

  api POST "${apiBaseUrl}/edits/${editId}:validate" > /dev/null || abortEdit "the changes are not valid."

  printf "\nThe release is ready:\n"
  printf " - track: %s (open testing)\n" "${track}"
  printf " - version: %s (%s)\n" "${version}" "${versionCode}"
  printf " - release notes (%s):\n%s\n\n" "${releaseNotesLanguage}" "$(cat "${releaseNotesFile}")"
  read -r -p "Commit the release to the open testing track (yes/no) default to yes? " doCommit
  doCommit=${doCommit:-yes}
  if [ "${doCommit}" != "yes" ]; then
    abortEdit "the release has not been committed."
  fi

  # Unless managed publishing is enabled for the app, committing the change also sends it for review.
  api POST "${apiBaseUrl}/edits/${editId}:commit" > /dev/null || abortEdit "unable to commit the release."
  editId=""
  printf "The release %s has been created on the open testing track.\n" "${version}"
}

downloadUniversalApk() {
  local versionCode="$1" outputApkPath="$2"
  [[ -n "${outputApkPath}" ]] || usage

  authenticate

  # The APKs are generated by Google Play after the upload of the app bundle, so they can take a few minutes to be available.
  local downloadId="" response
  printf "Looking for the universal APK of version code %s...\n" "${versionCode}"
  for _ in $(seq 1 30); do
    if response=$(api GET "${apiBaseUrl}/generatedApks/${versionCode}" 2> /dev/null); then
      downloadId=$(printf "%s" "${response}" | jq -r '.generatedApks[0].generatedUniversalApk.downloadId // empty')
      if [[ -n "${downloadId}" ]]; then
        break
      fi
    fi
    printf "The universal APK is not available yet, waiting...\n"
    sleep 20
  done
  [[ -n "${downloadId}" ]] || fail "the universal APK of version code ${versionCode} is not available."

  printf "Downloading the universal APK to %s...\n" "${outputApkPath}"
  mkdir -p "$(dirname "${outputApkPath}")"
  local encodedDownloadId partialPath="${outputApkPath}.part"
  encodedDownloadId=$(jq -rn --arg id "${downloadId}" '$id | @uri')
  if ! curl -sS --fail-with-body \
      -H @<(printf "Authorization: Bearer %s\n" "${accessToken}") \
      -o "${partialPath}" \
      "${apiBaseUrl}/generatedApks/${versionCode}/downloads/${encodedDownloadId}:download?alt=media"; then
    printf "Error: the download has failed:\n%s\n" "$(cat "${partialPath}" 2> /dev/null)" >&2
    rm -f "${partialPath}"
    exit 1
  fi
  mv "${partialPath}" "${outputApkPath}"
  printf "The universal APK has been downloaded to %s.\n" "${outputApkPath}"
}

case "$1" in
  check)
    check
    ;;
  upload)
    upload "$2" "$3" "$4" "$5"
    ;;
  download-universal-apk)
    downloadUniversalApk "$2" "$3"
    ;;
  *)
    usage
    ;;
esac
