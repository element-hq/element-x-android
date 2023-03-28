# Contributing to Element Android

<!--- TOC -->

* [Contributing code to Matrix](#contributing-code-to-matrix)
* [Android Studio settings](#android-studio-settings)
* [Compilation](#compilation)
* [Strings](#strings)
  * [I want to add new strings to the project](#i-want-to-add-new-strings-to-the-project)
  * [I want to help translating Element](#i-want-to-help-translating-element)
* [I want to submit a PR to fix an issue](#i-want-to-submit-a-pr-to-fix-an-issue)
  * [Kotlin](#kotlin)
  * [Changelog](#changelog)
  * [Code quality](#code-quality)
    * [ktlint](#ktlint)
    * [knit](#knit)
    * [lint](#lint)
  * [Unit tests](#unit-tests)
  * [Tests](#tests)
  * [Accessibility](#accessibility)
  * [Jetpack Compose](#jetpack-compose)
  * [Authors](#authors)
* [Thanks](#thanks)

<!--- END -->

## Contributing code to Matrix

Please read https://github.com/matrix-org/synapse/blob/master/CONTRIBUTING.md

Element X Android support can be found in this room: [![Element Android Matrix room #element-android:matrix.org](https://img.shields.io/matrix/element-android:matrix.org.svg?label=%23element-android:matrix.org&logo=matrix&server_fqdn=matrix.org)](https://matrix.to/#/#element-android:matrix.org).

The rest of the document contains specific rules for Matrix Android projects

## Android Studio settings

Please set the "hard wrap" setting of Android Studio to 160 chars, this is the setting we use internally to format the source code (Menu `Settings/Editor/Code Style` then `Hard wrap at`).
Please ensure that you're using the project formatting rules (which are in the project at .idea/codeStyles/), and format the file before committing them.

## Compilation

This project should compile without any special action. Just clone it and open it with Android Studio, or compile from command line using `gradlew`.

Note: please make sure that the configuration is `app` and not `samples.minimal`.

## Strings

The strings of the project are managed externally using [https://localazy.com](https://localazy.com) and shared with ElementX iOS.

### I want to add new strings to the project

Only the core team can modify or add English strings to Localazy. As an external contributor, if you want to add new strings, feel free to add an Android resource file to the project (for instance a file named `temporary.xml`), with a note in the description of the PR for the reviewer to integrate the String into `Localazy`. If accepted, the reviewer will add the String(s) for you, and then you can download them on your branch (following these [instructions](./tools/localazy/README.md#download-translations)) and remove the temporary file.

Please follow the naming rules for the key. More details in [the dedicated section in this README.md](./tools/localazy/README.md#key-naming-rules)

### I want to help translating Element

Please note that the Localazy project is not open yet for external contributions.

To help translating, please go to [https://localazy.com/p/element](https://localazy.com/p/element).

- If you want to fix an issue with an English string, please open an issue on the github project of ElementX (Android or iOS).Only the core team can modify or add English strings.
- If you want to fix an issue in other languages, or add a missing translation, or even add a new language, please go to [https://localazy.com/p/element](https://localazy.com/p/element).

More informations can be found [in this README.md](./tools/localazy/README.md).

## I want to submit a PR to fix an issue

Please have a look in the [dedicated documentation](./docs/pull_request.md) about pull request.

Please check if a corresponding issue exists. If yes, please let us know in a comment that you're working on it.
If an issue does not exist yet, it may be relevant to open a new issue and let us know that you're implementing it.

### Kotlin

This project is full Kotlin. Please do not write Java classes.

### Changelog

Please create at least one file under ./changelog.d containing details about your change. Towncrier will be used when preparing the release.

Towncrier says to use the PR number for the filename, but the issue number is also fine.

Supported filename extensions are:

- ``.feature``: Signifying a new feature in Element Android or in the Matrix SDK.
- ``.bugfix``: Signifying a bug fix.
- ``.wip``: Signifying a work in progress change, typically a component of a larger feature which will be enabled once all tasks are complete.
- ``.doc``: Signifying a documentation improvement.
- ``.misc``: Any other changes.

See https://github.com/twisted/towncrier#news-fragments if you need more details.

### Code quality

Make sure the following commands execute without any error:

<pre>
./gradlew check
</pre>

Some separate commands can also be run, see below.

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

### Tests

Element X is currently supported on Android Lollipop (API 21+): please test your change on an Android device (or Android emulator) running with API 21. Many issues can happen (including crashes) on older devices.
Also, if possible, please test your change on a real device. Testing on Android emulator may not be sufficient.

You should consider adding Unit tests with your PR, and also integration tests (AndroidTest). Please refer to [this document](./docs/integration_tests.md) to install and run the integration test environment.

### Accessibility

Please consider accessibility as an important point. As a minimum requirement, in layout XML files please use attributes such as `android:contentDescription` and `android:importantForAccessibility`, and test with a screen reader if it's working well. You can add new string resources, dedicated to accessibility, in this case, please prefix theirs id with `a11y_`.

For instance, when updating the image `src` of an ImageView, please also consider updating its `contentDescription`. A good example is a play pause button.

### Jetpack Compose

When adding or editing `@Composable`, make sure that you create a `@Preview` function, with suffix `Preview`. This will also create a UI test automatically.

### Authors

Feel free to add an entry in file AUTHORS.md

## Thanks

Thanks for contributing to Matrix projects!
