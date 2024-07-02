# Screenshot testing

<!--- TOC -->

* [Overview](#overview)
* [Setup](#setup)
* [Recording](#recording)
* [Verifying](#verifying)
* [Contributing](#contributing)

<!--- END -->

## Overview

- Screenshot tests are tests which record the content of a rendered screen and verify subsequent runs to check if the screen renders differently.
- Element X uses [Paparazzi](https://github.com/cashapp/paparazzi) to render, record and verify Composables. All internal/public Composable Preview will be used for screenshot tests, thanks to the usage of [ComposablePreviewScanner](https://github.com/sergio-sastre/ComposablePreviewScanner).
- The screenshot verification occurs on every pull request as part of the `tests.yml` workflow.

## Setup

- Install Git LFS through your package manager of choice (`brew install git-lfs` | `yay -S git-lfs`).
- Install the Git LFS hooks into the project.

```shell
# with element-android as the current working directory
git lfs install --local
```

If installed correctly, `git push` and `git pull` will now include LFS content.

## Recording

Recording of screenshots is done by triggering the GitHub action [Record screenshots](https://github.com/element-hq/element-x-android/actions/workflows/recordScreenshots.yml), to avoid differences of generated binary files (png images) depending on developers' environment.

So basically, you will create a branch, do some commits with your work on it, then push your branch, trigger the GitHub action to record the screenshots (only if you think preview may have changed), and finally create a pull request. The GitHub action will record the screenshots and commit the changes to the branch.

You can still record the screenshots locally, but please do not commit the changes.

To record the screenshot locally, run the following command:

```shell
./gradlew recordPaparazziDebug
```

The task will delete the content of the folder `/snapshots` before recording (see the task `removeOldSnapshots` defined in the project).

If this is not the case, you can run

```shell
rm -rf ./tests/uitests/src/test/snapshots
```

Paparazzi will generate images in `:tests:uitests/src/test/snapshots`, which will need to be committed to the repository using Git LFS.

## Verifying

```shell
./gradlew verifyPaparazziDebug
```

In the case of failure, Paparazzi will generate images in `:tests:uitests/build/paparazzi/failures`. The images will show the expected and actual screenshots along with a delta of the two images.

## Contributing

- Creating Previewable Composable will automatically creates new screenshot tests.
- After creating the new test, record and commit the newly rendered screens.
- `./tools/git/validate_lfs.sh` can be run to ensure everything is working correctly with Git LFS, the CI also runs this check.
