#!/usr/bin/env bash
#
# Copyright (c) 2026 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.
#

# Wait for an artifact uploaded by another job of the current workflow run.
#
# A job can't depend on another one in the middle of its steps, so poll the GitHub API until the other job completes, then:
# - if it succeeded: writes `available=true` and the `artifact-id` of its artifact to $GITHUB_OUTPUT.
# - if it failed or was cancelled, for instance because its tests failed: writes `available=false`.
#
# Only the artifact of a job which completed in the current attempt is used: when re-running all the jobs, the artifact of
# the previous attempt is still listed until the other job uploads it again. When only re-running some jobs, the other job
# keeps its result and its artifact from the previous attempt, and is listed as completed in the current attempt.
#
# The script fails if the other job is not part of the run, if it succeeded without uploading the artifact, or if the GitHub
# API keeps failing. The step running it should also have a `timeout-minutes`, in case the other job never completes.
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

# Prints "<status> <conclusion>" of the job in the current attempt, or nothing if there is no such job. Fails if the API request fails.
job_state() {
  gh api "repos/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID/attempts/$GITHUB_RUN_ATTEMPT/jobs" --paginate \
    --jq ".jobs[] | select(.name == \"$job_name\") | \"\(.status) \(.conclusion)\""
}

# Prints the id of the most recent artifact with this name, or nothing if there is none. Fails if the API request fails.
latest_artifact_id() {
  gh api "repos/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID/artifacts?name=$artifact_name" --paginate \
    --jq ".artifacts[] | select(.name == \"$artifact_name\" and .expired == false) | \"\(.created_at) \(.id)\"" |
    sort | tail -n 1 | cut -d ' ' -f 2
}

echo "Waiting for the '$job_name' job to upload the '$artifact_name' artifact..."
while true; do
  if ! state=$(job_state); then
    api_failed
  elif [ -z "$state" ]; then
    # Most likely the job was renamed without updating the name used here: fail instead of waiting until the timeout.
    echo "::error::There is no '$job_name' job in this workflow run, so the '$artifact_name' artifact will never be uploaded."
    exit 1
  else
    consecutive_api_failures=0
    read -r status conclusion <<< "$state"
    if [ "$status" = "completed" ]; then
      if [ "$conclusion" != "success" ]; then
        echo "::warning::The '$job_name' job did not succeed ($conclusion), so the '$artifact_name' artifact is not available, see this job."
        echo "available=false" >> "$GITHUB_OUTPUT"
        exit 0
      fi
      # The job succeeded, so its artifact must exist: retry if the API fails, and fail if it's missing.
      while ! artifact_id=$(latest_artifact_id); do
        api_failed
        sleep "$poll_interval_seconds"
      done
      if [ -z "$artifact_id" ]; then
        echo "::error::The '$job_name' job succeeded but the '$artifact_name' artifact was not found."
        exit 1
      fi
      echo "The '$artifact_name' artifact is available (id $artifact_id)."
      echo "available=true" >> "$GITHUB_OUTPUT"
      echo "artifact-id=$artifact_id" >> "$GITHUB_OUTPUT"
      exit 0
    fi
  fi
  sleep "$poll_interval_seconds"
done
