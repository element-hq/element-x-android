# Contributing to Element X Android

<!--- TOC -->

* [Contributing to Element](#contributing-to-element)
  * [I want to help translating Element](#i-want-to-help-translating-element)
  * [I want to fix a bug](#i-want-to-fix-a-bug)
  * [I want to add a new feature or enhancement](#i-want-to-add-a-new-feature-or-enhancement)
* [Developer onboarding](#developer-onboarding)
  * [Submitting the PRs](#submitting-the-prs)
  * [Android Studio settings](#android-studio-settings)
  * [Compilation](#compilation)
  * [Strings](#strings)
  * [Element X Android Gallery](#element-x-android-gallery)
  * [Kotlin](#kotlin)
  * [Changelog](#changelog)
  * [Code quality](#code-quality)
    * [detekt](#detekt)
    * [ktlint](#ktlint)
    * [checkDocs](#checkdocs)
    * [lint](#lint)
  * [Unit tests](#unit-tests)
    * [konsist](#konsist)
  * [Tests](#tests)
  * [Accessibility](#accessibility)
  * [Jetpack Compose](#jetpack-compose)
  * [Authors](#authors)
* [Thanks](#thanks)

<!--- END -->

## Contributing to Element

Element X Android support can be found in this room: [![Element X Android Matrix room #element-x-android:matrix.org](https://img.shields.io/matrix/element-x-android:matrix.org.svg?label=%23element-x-android:matrix.org&logo=matrix&server_fqdn=matrix.org)](https://matrix.to/#/#element-x-android:matrix.org).

The rest of the document contains specific rules for Matrix Android projects.

### I want to help translating Element

To help translating, please go to [https://localazy.com/p/element](https://localazy.com/p/element).

- If you want to fix an issue in other languages, or add a missing translation, or even add a new language, please go to [https://localazy.com/p/element](https://localazy.com/p/element).
- If you want to fix an issue with an English string, please open an issue on the github project of Element X (Android or iOS). Only the core team can modify or add English strings. As an external contributor, if you want to add new strings, feel free to add an Android resource file to the project (for instance a file named `temporary.xml`), with a note in the description of the PR for the reviewer to integrate the String into `Localazy`. If accepted, the reviewer will add the String(s) for you, and then you can download them on your branch (following these [instructions](./tools/localazy/README.md#download-translations)) and remove the temporary file. Please follow the naming rules for the key. More details in [the dedicated section in this README.md](./tools/localazy/README.md#key-naming-rules) More information can be found [in this README.md](./tools/localazy/README.md).

Once a language is sufficiently translated, it will be added to the app. The core team will decide when a language is sufficiently translated.

### I want to fix a bug

Please check if a corresponding issue exists, if not please create one. In both cases, let us know in the comment that you've started working on it.

### I want to add a new feature or enhancement

To make a great product with a great user experience, all the small efforts need to go in the same direction and be aligned and consistent with each other.

Before making your contribution, please consider the following:

* One product can’t do everything well. Element is focusing on private end-to-end encrypted messaging and voice - this can either be for consumers (e.g. friends and family) or for professional teams and organizations. Public forums and other types of chats without E2EE remain supported but are not the primary use case in case UX compromises need to be made.
* There are 3 platforms - Android, [iOS](https://github.com/element-hq/element-x-ios) and [Web/Desktop](https://github.com/element-hq/element-web). These platforms need to have feature parity and design consistency. For some features, supporting all platforms is a must have, in some cases exceptions can be made to have it on one platform only.
* To make sure your idea fits both from a design/solution and use case perspective, please open a new issue (or find an existing issue) in [element-meta](https://github.com/element-hq/element-meta/issues) repository describing the use case and how you plan to tackle it. Do not just describe what feature is missing, explain why the users need it with a couple of real life examples from the field.
  * In case of an existing issue, please comment that you're planning to contribute. If you create a new issue, please specify that in the issue. In such a case we will try to review the issue ASAP and provide you with initial feedback so you can be confident if and at which conditions your contributions will be accepted.

Once we know that you want to contribute and have confirmed that the new feature is overall aligned with the product direction, the designers of the core team will help you with the designs and any other type of guidance when it comes to the user experience. We will try to unblock you as quickly as we can, but it may not be instant. Having a clear understanding of the use case and the impact of the feature will help us with the prioritization and faster responses.

Only once all of the above is met should you open a PR with your proposed changes.

## Developer onboarding

For a detailed overview of the project, see [Developer Onboarding](./docs/_developer_onboarding.md).

### Submitting the PRs

Please have a look in the [dedicated documentation](./docs/pull_request.md) about pull request.

### Android Studio settings

Please set the "hard wrap" setting of Android Studio to 160 chars, this is the setting we use internally to format the source code (Menu `Settings/Editor/Code Style` then `Hard wrap at`).
Please ensure that you're using the project formatting rules (which are in the project at .idea/codeStyles/), and format the file before committing them.

### Compilation

This project should compile without any special action. Just clone it and open it with Android Studio, or compile from command line using `gradlew`.

### Strings

The strings of the project are managed externally using [https://localazy.com](https://localazy.com) and shared with Element X iOS.

### Element X Android Gallery

Once added to Localazy, translations can be checked screen per screen using our tool Element X Android Gallery, available at https://element-hq.github.io/element-x-android/.

Localazy syncs occur every Monday and the screenshots on this page are generated every Tuesday, so you'll have to wait to see your change appearing on Element X Android Gallery.

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

#### checkDocs

`checkDocs` is a Gradle task which checks markdown files on the project to ensure their table of contents is up to date. It uses `tools/docs/generate_toc.py --verify` under the hood, and has a counterpart `generateDocsToc` task which runs `tools/docs/generate_toc.py` to update the table of contents of markdown files.

So everytime the toc should be updated, just run
<pre>
./gradlew generateDocsToc
</pre>

and commit the changes.

The CI will check that markdown files are up to date by running

<pre>
./gradlew checkDocs
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
