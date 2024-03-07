#!/usr/bin/env python3

#  Copyright (c) 2024 New Vector Ltd
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

import os


def compare(file1, file2):
    __doc__ = "Compare two files, return True if different, False if identical."
    # Compare file size
    file1_stats = os.stat(file1)
    file2_stats = os.stat(file2)
    if file1_stats.st_size != file2_stats.st_size:
        return True
    # Compare file content
    with open(file1, "rb") as f1, open(file2, "rb") as f2:
        content1 = f1.read()
        content2 = f2.read()
        return content1 != content2
