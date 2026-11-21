#!/usr/bin/env python3
#
# Copyright (c) 2026 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.
#

"""
Check the output of a Gradle invocation for the failed tasks of a given kind.

The unit tests, the screenshot tests and the coverage verification run in a single Gradle invocation in tests.yml, so the
screenshot tests can run while the unit tests of other modules are still running. This script lets each kind of check be
reported in its own step anyway, so we can see which kind of check failed without reading the logs.

Usage: check_gradle_failures.py <gradle output file> <unit-tests|screenshot-tests|coverage|other>

Exits with 1 if a task of the given kind failed, 0 otherwise.
"""

import re
import sys

# Modules applying the Paparazzi plugin: their test task runs the screenshot tests.
SCREENSHOT_TEST_MODULES = (":tests:uitests:", ":libraries:compound:")

KINDS = ("unit-tests", "screenshot-tests", "coverage", "other")


def kind_of(task_path):
    task_name = task_path.rsplit(":", 1)[-1]
    if "kover" in task_name.lower():
        return "coverage"
    if "paparazzi" in task_name.lower():
        return "screenshot-tests"
    if re.fullmatch(r"test\w*UnitTest", task_name):
        if task_path.startswith(SCREENSHOT_TEST_MODULES):
            return "screenshot-tests"
        return "unit-tests"
    return "other"


def main():
    if len(sys.argv) != 3 or sys.argv[2] not in KINDS:
        print(__doc__)
        sys.exit(2)
    log_file, kind = sys.argv[1], sys.argv[2]

    with open(log_file, encoding="utf-8", errors="replace") as f:
        log = f.read()

    failed_tasks = sorted(set(re.findall(r"Execution failed for task '([^']+)'", log)))
    failed_tasks_of_kind = [task for task in failed_tasks if kind_of(task) == kind]

    if failed_tasks_of_kind:
        print(f"Failed tasks ({kind}):")
        for task in failed_tasks_of_kind:
            print(f"  {task}")
        sys.exit(1)

    build_failed = "BUILD FAILED" in log
    if kind == "other" and build_failed and not failed_tasks:
        # For instance a configuration error, which is not reported as a task failure.
        print("The Gradle build failed, but not because of a task. See the logs of the Gradle step.")
        sys.exit(1)
    if kind == "coverage" and build_failed:
        # The coverage verification depends on all the tests, so it is not run if some of them failed.
        print("::warning::The code coverage may not have been verified because other tasks failed.")
    print(f"No failed task ({kind}).")


if __name__ == "__main__":
    main()
