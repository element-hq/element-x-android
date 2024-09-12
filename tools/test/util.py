#!/usr/bin/env python3

# Copyright 2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only
# Please see LICENSE in the repository root for full details.

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
