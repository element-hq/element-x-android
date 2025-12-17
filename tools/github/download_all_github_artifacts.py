#!/usr/bin/env python3
#
# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2022-2025 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.
#

import argparse
import hashlib
import json
import os
# Run `pip3 install requests --break-system-packages` if not installed yet
import requests
# Run `pip3 install re` if not installed yet
import re
import time

# This script downloads artifacts from GitHub.
# Ref: https://docs.github.com/en/rest/actions/artifacts#get-an-artifact

error = False

### Arguments

parser = argparse.ArgumentParser(description='Download artifacts from GitHub.')
parser.add_argument('-t',
                    '--token',
                    required=True,
                    help='The GitHub token with read access.')
parser.add_argument('-r',
                    '--runUrl',
                    required=True,
                    help='the GitHub action run url.')
parser.add_argument('-d',
                    '--directory',
                    default="",
                    help='the target directory, where files will be downloaded. If not provided the runId will be used to create a directory.')
parser.add_argument('-v',
                    '--verbose',
                    help="increase output verbosity.",
                    action="store_true")
parser.add_argument('-s',
                    '--simulate',
                    help="simulate action, do not create folder or download any file.",
                    action="store_true")

args = parser.parse_args()

if args.verbose:
    print("Argument:")
    print(args)


# Split the artifact URL to get information
# Ex: https://github.com/element-hq/element-x-android/actions/runs/9065756777
runUrl = args.runUrl

url_regex = r"https://github.com/(.+?)/(.+?)/actions/runs/(.+)"
result = re.search(url_regex, runUrl)

if result is None:
    print(
        "❌ Invalid parameter --runUrl '%s'. Please check the format.\nIt should be something like: %s" %
        (runUrl, 'https://github.com/element-hq/element-x-android/actions/runs/9065756777')
    )
    exit(1)

(gitHubRepoOwner, gitHubRepo, runId) = result.groups()

if args.verbose:
    print("gitHubRepoOwner: %s, gitHubRepo: %s, runId: %s" % (gitHubRepoOwner, gitHubRepo, runId))

headers = {
   'Authorization': "Bearer %s" % args.token,
   'Accept': 'application/vnd.github+json'
}

base_url = "https://api.github.com/repos/%s/%s/actions/runs/%s" % (gitHubRepoOwner, gitHubRepo, runId)

### Fetch build state
status = ""
data = "{}"
while status != "completed":
    r = requests.get(base_url, headers=headers)
    data = json.loads(r.content.decode())

    if args.verbose:
        print("Json data:")
        print(data)
    status = data.get("status")

    if data.get("status") == "completed":
        if data.get("conclusion") != "success":
            print("❌ The action %s is completed, but there is an error, the conclusion is: %s." % (runUrl, data.get("conclusion")))
            exit(1)
    else:
        # Wait 1 minute
        print("The action %s is not completed yet, waiting 1 minute..." % runUrl)
        time.sleep(60)


artifacts_url = data.get("artifacts_url")
if args.verbose:
    print("Artifacts url: %s" % artifacts_url)

r = requests.get(artifacts_url, headers=headers)
data = json.loads(r.content.decode())

if args.directory == "":
    targetDir = runId
else:
    targetDir = args.directory

for artifact in data.get("artifacts"):
    if args.verbose:
        print("Artifact:")
        print(artifact)
    # Invoke the script to download the artifact
    os.system("python3 ./tools/github/download_github_artifacts.py -t %s -d %s -a %s" % (args.token, targetDir, artifact.get("url")))
