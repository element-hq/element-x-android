# Nightly builds

<!--- TOC -->

* [Configuration](#configuration)
* [How to register to get nightly build](#how-to-register-to-get-nightly-build)
* [Build nightly manually](#build-nightly-manually)

<!--- END -->

## Configuration

The nightly build will contain what's on develop, in release mode, for the main variant. It is signed using a dedicated signature, and has a dedicated appId (`io.element.android.x.nightly`), so it can be installed along with the production version of Element X Android. The only other difference compared to ElementX Android is a different app name. We do not want to change the app name since it will also affect some strings in the app, and we do want to do that. (TODO today, the app name is changed.)

Nightly builds are built and released to Firebase every days, and automatically.

This is recommended to exclusively use this app, with your main account, instead of Element X Android, and fallback to ElementX Android just in case of regression, to discover as soon as possible any regression, and report it to the team. To avoid double notification, you may want to disable the notification from the Element Android production version. Just open Element Android, navigate to `Settings/Notifications` and uncheck `Enable notifications for this session` (TODO Not supported yet).

*Note:* Due to a limitation of Firebase, the nightly build is the universal build, which means that the size of the APK is a bit bigger, but this should not have any other side effect.

## How to register to get nightly build

Click on this link and follow the instruction: [https://appdistribution.firebase.dev/i/7de2dbc61e7fb2a6](https://appdistribution.firebase.dev/i/7de2dbc61e7fb2a6)

## Build nightly manually

Nightly build can be built manually from your computer. You will need to retrieved some secrets from Passbolt and add them to your file `~/.gradle/gradle.properties`:

```
signing.element.nightly.storePassword=VALUE_FROM_PASSBOLT
signing.element.nightly.keyId=VALUE_FROM_PASSBOLT
signing.element.nightly.keyPassword=VALUE_FROM_PASSBOLT
```

You will also need to add the environment variable `FIREBASE_TOKEN`:

```sh
export FIREBASE_TOKEN=VALUE_FROM_PASSBOLT
```

Then you can run the following commands (which are also used in the file for [the GitHub action](../.github/workflows/nightly.yml)):

```sh
git checkout develop
./gradlew assembleGplayNightly appDistributionUploadGplayNightly
```

Then you can reset the change on the codebase.
