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
- ElementX uses [Paparazzi](https://github.com/cashapp/paparazzi) to render, record and verify Composable. All Composable Preview will be use to make screenshot test, thanks to the usage of [Showkase](https://github.com/airbnb/Showkase).
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

It's recommended to delete the content of the folder `/snapshots` before recording.

```shell
rm -rf ./tests/uitests/src/test/snapshots
./gradlew recordPaparazziDebug
```

Paparazzi will generate images in `:tests:uitests/src/test/snapshots`, which will need to be committed to the repository using Git LFS.

## Verifying

```shell
./gradlew verifyPaparazziDebug
```

In the case of failure, Paparazzi will generate images in `:tests:uitests/out/failure`. The images will show the expected and actual screenshots along with a delta of the two images.

## Contributing

- Creating Previewable Composable will automatically creates new screenshot tests.
- After creating the new test, record and commit the newly rendered screens.
- `./tools/git/validate_lfs.sh` can be run to ensure everything is working correctly with Git LFS, the CI also runs this check.
