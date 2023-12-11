#!/usr/bin/env python3

#  Copyright (c) 2020 New Vector Ltd
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import argparse
import json
import os
import os.path
# Run `pip3 install requests` if not installed yet
import requests

### Arguments

parser = argparse.ArgumentParser(description='Download sas string from matrix-doc.')
parser.add_argument('-v',
                    '--verbose',
                    help="increase output verbosity.",
                    action="store_true")

args = parser.parse_args()

if args.verbose:
    print("Argument:")
    print(args)

base_url = "https://raw.githubusercontent.com/matrix-org/matrix-spec/main/data-definitions/sas-emoji.json"

base_emoji_url = "https://raw.githubusercontent.com/twitter/twemoji/master/assets/svg/"

print("Downloading " + base_url + "…")

r0 = requests.get(base_url)
data0 = json.loads(r0.content.decode())

if args.verbose:
    print("Json data:")
    print(data0)

print()

scripts_dir = os.path.dirname(os.path.abspath(__file__))
data_defs_dir = os.path.join(scripts_dir, "../../tmp/emoji/")

def handle_emoji(dict):
    print("Handle emoji " + str(dict["number"]) + " (" + dict["description"] + ")…")
    if args.verbose:
        print("With")
        print(dict)
    # Transform dict["unicode"] from "U+2601U+FE0F" to "2601U"
    emoji = dict["unicode"].split("U+")[1].lower()
    url = base_emoji_url + emoji + ".svg"
    file = os.path.join(data_defs_dir, "ic_verification_" + format(dict["number"], '02d') + ".svg")
    print("Downloading " + url + " to " + file + "…")
    r = requests.get(url)
    if r.status_code != 200:
        print("Fatal: " + str(r.status_code))
        # Stop script with error
        sys.exit(1)
    os.makedirs(os.path.dirname(file), exist_ok=True)
    with open(file, "w") as f:
        f.write(r.content.decode())

for emoji in data0:
    handle_emoji(emoji)

print()
print("Success!")
print()
print("To convert to vector drawable, download tool from https://www.androiddesignpatterns.com/2018/11/android-studio-svg-to-vector-cli.html")
print("unzip it, and run:")
print("vd-tool/bin/vd-tool -c -in ./tmp/emoji -out features/verifysession/impl/src/main/res/drawable")
