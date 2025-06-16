# Contributing to Element X Android

<!--- TOC -->

* [Developer onboarding](#developer-onboarding)
* [Contributing code to Matrix](#contributing-code-to-matrix)
* [Android Studio settings](#android-studio-settings)
* [Compilation](#compilation)
* [Strings](#strings)
  * [I want to add new strings to the project](#i-want-to-add-new-strings-to-the-project)
  * [I want to help translating Element](#i-want-to-help-translating-element)
  * [Element X Android Gallery](#element-x-android-gallery)
* [I want to submit a PR to fix an issue](#i-want-to-submit-a-pr-to-fix-an-issue)
  * [Kotlin](#kotlin)
  * [Changelog](#changelog)
  * [Code quality](#code-quality)
    * [detekt](#detekt)
    * [ktlint](#ktlint)
    * [knit](#knit)
    * [lint](#lint)
  * [Unit tests](#unit-tests)
    * [konsist](#konsist)
  * [Tests](#tests)
  * [Accessibility](#accessibility)
  * [Jetpack Compose](#jetpack-compose)
  * [Authors](#authors)
* [Thanks](#thanks)

<!--- END -->

## Developer onboarding

For a detailed overview of the project, see [Developer Onboarding](./docs/_developer_onboarding.md).

## Contributing code to Matrix

If instead of contributing to the Element X Android project, you want to contribute to Synapse, the homeserver implementation, please read the [Synapse contribution guide](https://element-hq.github.io/synapse/latest/development/contributing_guide.html).

Element X Android support can be found in this room: [![Element X Android Matrix room #element-x-android:matrix.org](https://img.shields.io/matrix/element-x-android:matrix.org.svg?label=%23element-x-android:matrix.org&logo=matrix&server_fqdn=matrix.org)](https://matrix.to/#/#element-x-android:matrix.org).

The rest of the document contains specific rules for Matrix Android projects.

## Android Studio settings

Please set the "hard wrap" setting of Android Studio to 160 chars, this is the setting we use internally to format the source code (Menu `Settings/Editor/Code Style` then `Hard wrap at`).
Please ensure that you're using the project formatting rules (which are in the project at .idea/codeStyles/), and format the file before committing them.

## Compilation

This project should compile without any special action. Just clone it and open it with Android Studio, or compile from command line using `gradlew`.

## Strings

The strings of the project are managed externally using [https://localazy.com](https://localazy.com) and shared with Element X iOS.

### I want to add new strings to the project

Only the core team can modify or add English strings to Localazy. As an external contributor, if you want to add new strings, feel free to add an Android resource file to the project (for instance a file named `temporary.xml`), with a note in the description of the PR for the reviewer to integrate the String into `Localazy`. If accepted, the reviewer will add the String(s) for you, and then you can download them on your branch (following these [instructions](./tools/localazy/README.md#download-translations)) and remove the temporary file.

Please follow the naming rules for the key. More details in [the dedicated section in this README.md](./tools/localazy/README.md#key-naming-rules)

### I want to help translating Element

To help translating, please go to [https://localazy.com/p/element](https://localazy.com/p/element).

- If you want to fix an issue with an English string, please open an issue on the github project of Element X (Android or iOS). Only the core team can modify or add English strings.
- If you want to fix an issue in other languages, or add a missing translation, or even add a new language, please go to [https://localazy.com/p/element](https://localazy.com/p/element).

More information can be found [in this README.md](./tools/localazy/README.md).

Once a language is sufficiently translated, it will be added to the app. The core team will decide when a language is sufficiently translated.

### Element X Android Gallery

Once added to Localazy, translations can be checked screen per screen using our tool Element X Android Gallery, available at https://element-hq.github.io/element-x-android/.

Localazy syncs occur every Monday and the screenshots on this page are generated every Tuesday, so you'll have to wait to see your change appearing on Element X Android Gallery.

## I want to submit a PR to fix an issue

Please have a look in the [dedicated documentation](./docs/pull_request.md) about pull request.

Please check if a corresponding issue exists. If yes, please let us know in a comment that you're working on it.
If an issue does not exist yet, it may be relevant to open a new issue and let us know that you're implementing it.

### Kotlin

This project is full Kotlin. Please do not write Java classes.

### Changelog

The release notes are generated from the pull request titles and labels. If possible, the title must describe best what will be the user facing change.

You will also need to add a label starting by `PR-` to you Pull Request to help categorize the release note. The label should be added by the PR author, but can be added by the reviewer if the submitter does not have right to add label. Also note that the label can be added after the PR has been merged, as soon as the release is not done yet.

### Code quality

Make sure the following commands execute without any error:

<pre>
./tools/quality/check.sh
</pre>

Some separate commands can also be run, see below.

#### detekt

<pre>
./gradlew detekt
</pre>

#### ktlint

<pre>
./gradlew ktlintCheck --continue
</pre>

Note that you can run

<pre>
./gradlew ktlintFormat
</pre>

For ktlint to fix some detected errors for you (you still have to check and commit the fix of course)

#### knit

[knit](https://github.com/Kotlin/kotlinx-knit) is a tool which checks markdown files on the project. Also it generates/updates the table of content (toc) of the markdown files.

So everytime the toc should be updated, just run
<pre>
./gradlew knit
</pre>

and commit the changes.

The CI will check that markdown files are up to date by running

<pre>
./gradlew knitCheck
</pre>

#### lint

<pre>
./gradlew lint
</pre>

### Unit tests

Make sure the following commands execute without any error:

<pre>
./gradlew test
</pre>

#### konsist

[konsist](https://github.com/LemonAppDev/konsist) is setup in the project to check that the architecture and the naming rules are followed. Konsist tests are classical Unit tests.

### Tests

Element X is currently supported on Android Marshmallow (API 23+): please test your change on an Android device (or Android emulator) running with API 23. Many issues can happen (including crashes) on older devices.
Also, if possible, please test your change on a real device. Testing on Android emulator may not be sufficient.

You should consider adding Unit tests with your PR, and also integration tests (AndroidTest). Please refer to [this document](./docs/integration_tests.md) to install and run the integration test environment.

### Accessibility

Please consider accessibility as an important point. As a minimum requirement, in layout XML files please use attributes such as `android:contentDescription` and `android:importantForAccessibility`, and test with a screen reader if it's working well. You can add new string resources, dedicated to accessibility, in this case, please prefix theirs id with `a11y_`.

For instance, when updating the image `src` of an ImageView, please also consider updating its `contentDescription`. A good example is a play pause button.

### Jetpack Compose

When adding or editing `@Composable`, make sure that you create an internal function annotated with `@PreviewsDayNight`, with a name suffixed by `Preview`, and having `ElementPreview` as the root composable.

Example:
```kotlin
@PreviewsDayNight
@Composable
internal fun PinIconPreview() = ElementPreview {
    PinIcon()
}
```

This will allow to preview the composable in both light and dark mode in Android Studio. This will also automatically add UI tests. The GitHub action [Record screenshots](https://github.com/element-hq/element-x-android/actions/workflows/recordScreenshots.yml) has to be run to record the new screenshots. The PR reviewer can trigger this for you if you're not part of the core team. 

### Authors

Feel free to add an entry in file AUTHORS.md

## Thanks

Thanks for contributing to Matrix projects!
