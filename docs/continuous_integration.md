# Continuous integration strategy

<!--- TOC -->

* [Introduction](#introduction)
* [CI tools](#ci-tools)
* [Rules](#rules)
* [What is the CI checking](#what-is-the-ci-checking)
* [What is the CI reporting](#what-is-the-ci-reporting)
* [Current choices](#current-choices)
  * [R8 task](#r8-task)
  * [Android test (connected test)](#android-test-connected-test)

<!--- END -->

## Introduction

This document gives some information about how we take advantage of the continuous integration (CI).

## CI tools

We use GitHub Actions to configure and perform the CI.

## Rules

We want:

1. The CI to detect as soon as possible any issue in the code
2. The CI to be fast - it's run on all the Pull Requests, and developers do not like to wait too long
3. The CI to be reliable - it should not fail randomly
4. The CI to generate artifacts which can be used by the team and the community
5. The CI to generate useful logs and reports, not too verbose, not too short
6. The developer to be able to run the CI locally - to help with this we have [a script](../tools/check/check_code_quality.sh) the can be run locally and which does more checks that just building and deploying the app.
7. The CI to be used as a common environment for the team: generate the screenshots image for the screenshot test, build the release build (unsigned)
8. The CI to run repeated tasks, like building the nightly builds, integrating data from external tools (translations, etc.)
9. The CI to upgrade our dependencies (Renovate)
10. The CI to do some issue triaging

## What is the CI checking

The CI checks that:

1. The code is compiling, without any warnings, for all the app build types and variants
2. The tests are passing
3. The code quality is good (detekt, ktlint, lint)
4. The code is running and smoke tests are passing (maestro)
5. The PullRequest itself is good (with danger)
6. Files that must be added with git-lfs are added with git-lfs

## What is the CI reporting

The CI reports:

1. Code coverage reports
2. Sonar reports

## Current choices

### R8 task

The CI does not run R8 because it's too slow, and it breaks rule 2.

The drawback is that the nightly build can fail, as well as the release build.

Since the nightly build is failing, the team can detect the failure quite fast and react to it.

### Android test (connected test)

We limit the number of connected tests (tests under folder `androidTest`), because it often break rule 2 and 3.
