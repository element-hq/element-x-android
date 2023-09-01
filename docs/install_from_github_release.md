# Installing Element X Android from a Github Release

This document explains how to install Element X Android from a Github Release.

<!--- TOC -->

* [Requirements](#requirements)
* [Steps](#steps)
* [I already have the application on my phone](#i-already-have-the-application-on-my-phone)

<!--- END -->

## Requirements

The Github release will contain an Android App Bundle (with `aab` extension) file, unlike in the Element Android project where releases directly provide the APKs. So there are some steps to perform to generate and sign App Bundle APKs. An APK suitable for the targeted device will then be generated.

The easiest way to do that is to use the debug signature that is shared between the developers and stored in the Element X Android project. So we recommend to clone the project first, to be able to use the debug signature it contains. But note that you can use any other signature. You don't need to install Android Studio, you will only need a shell terminal.

You can clone the project by running:
```bash
git clone git@github.com:vector-im/element-x-android.git
```
or
```bash
git clone https://github.com/vector-im/element-x-android.git
```

You will also need to install [bundletool](https://developer.android.com/studio/command-line/bundletool). On MacOS, you can run the following command:

```bash
brew install bundletool
```

## Steps

1. Open the GitHub release that you want to install from https://github.com/vector-im/element-x-android/releases
2. Download the asset `app-release-signed.aab`
3. Navigate to the folder where you cloned the project and run the following command:
```bash
bundletool build-apks --bundle=<PATH_TO_YOUR_APP-RELEASE-SIGNED.AAB_FILE> --output=./tmp/elementx.apks \
      --ks=./app/signature/debug.keystore --ks-pass=pass:android --ks-key-alias=androiddebugkey --key-pass=pass:android \
      --overwrite
```
For instance:
```bash
bundletool build-apks --bundle=./tmp/Element/0.1.5/app-release-signed.aab --output=./tmp/elementx.apks \ 
      --ks=./app/signature/debug.keystore --ks-pass=pass:android --ks-key-alias=androiddebugkey --key-pass=pass:android \
      --overwrite
```
4. Run an Android emulator, or connect a real device to your computer
5. Install the APKs on the device:
```bash
bundletool install-apks --apks=./tmp/elementx.apks
```

That's it, the application should be installed on your device, you can start it from the launcher icon.

##  I already have the application on my phone

If the application was already installed on your phone, there are several cases:

- it was installed from the PlayStore, you will have to uninstall it first because the signature will not match.
- it was installed from a previous GitHub release, this is like an application upgrade, so no need to uninstall the existing app.
- it was installed from a more recent GitHub release, you will have to uninstall it first.
