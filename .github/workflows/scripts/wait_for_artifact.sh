#!/usr/bin/env bash
#
# Copyright (c) 2026 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.
#

# Wait for an artifact uploaded by another job of the current workflow run.
#
# A job can't depend on another one in the middle of its steps, so poll the GitHub API until either:
# - the artifact is available: writes `available=true` to $GITHUB_OUTPUT.
# - the other job completed without uploading it, for instance because its tests failed: writes `available=false`.
# The script fails if the other job is not part of the run, or if the GitHub API keeps failing.
# The step running it should also have a `timeout-minutes`, in case the other job never completes.
#
# Usage: wait_for_artifact.sh <artifact name> <name of the job uploading it>
# Needs GH_TOKEN with the `actions: read` permission, and the GITHUB_* variables of the run.

set -euo pipefail

artifact_name="$1"
job_name="$2"

poll_interval_seconds="${POLL_INTERVAL_SECONDS:-15}"
max_consecutive_api_failures=5
consecutive_api_failures=0

api_failed() {
  consecutive_api_failures=$((consecutive_api_failures + 1))
  echo "::warning::GitHub API request failed ($consecutive_api_failures/$max_consecutive_api_failures)."
  if [ "$consecutive_api_failures" -ge "$max_consecutive_api_failures" ]; then
    echo "::error::The GitHub API failed $max_consecutive_api_failures times in a row, giving up waiting for the '$artifact_name' artifact."
    exit 1
  fi
}

# Prints "true" if the artifact exists, "false" otherwise. Fails if the API request fails.
artifact_exists() {
  local names
  names=$(gh api "repos/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID/artifacts" --paginate --jq '.artifacts[].name') || return 1
  if grep -qxF "$artifact_name" <<< "$names"; then echo true; else echo false; fi
}

# Prints the status of the job, or nothing if there is no such job in the run. Fails if the API request fails.
job_status() {
  gh api "repos/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID/attempts/$GITHUB_RUN_ATTEMPT/jobs" --paginate \
    --jq ".jobs[] | select(.name == \"$job_name\") | .status"
}

echo "Waiting for the '$artifact_name' artifact uploaded by the '$job_name' job..."
while true; do
  if ! exists=$(artifact_exists); then
    api_failed
  elif [ "$exists" = "true" ]; then
    echo "The '$artifact_name' artifact is available."
    echo "available=true" >> "$GITHUB_OUTPUT"
    exit 0
  elif ! status=$(job_status); then
    api_failed
  else
    consecutive_api_failures=0
    if [ -z "$status" ]; then
      # Most likely the job was renamed without updating the name used here: fail instead of waiting until the timeout.
      echo "::error::There is no '$job_name' job in this workflow run, so the '$artifact_name' artifact will never be uploaded."
      exit 1
    fi
    if [ "$status" = "completed" ]; then
      # The job may have uploaded the artifact between the two requests.
      if exists=$(artifact_exists) && [ "$exists" = "true" ]; then
        echo "The '$artifact_name' artifact is available."
        echo "available=true" >> "$GITHUB_OUTPUT"
        exit 0
      fi
      echo "::warning::The '$job_name' job completed without uploading the '$artifact_name' artifact, see this job."
      echo "available=false" >> "$GITHUB_OUTPUT"
      exit 0
    fi
  fi
  sleep "$poll_interval_seconds"
done
