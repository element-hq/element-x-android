# Developer on boarding

<!--- TOC -->

* [Introduction](#introduction)
  * [Quick introduction to Matrix](#quick-introduction-to-matrix)
    * [Matrix data](#matrix-data)
      * [Room](#room)
      * [Event](#event)
    * [Sync](#sync)
  * [The Android project](#the-android-project)
  * [Application](#application)
    * [Global architecture](#global-architecture)
    * [Jetpack Compose](#jetpack-compose)
    * [Template](#template)
  * [Push](#push)
  * [Dependencies management](#dependencies-management)
  * [Test](#test)
  * [Other points](#other-points)
    * [Logging](#logging)
    * [Rageshake](#rageshake)
  * [Tips](#tips)
* [Happy coding!](#happy-coding)

<!--- END -->

## Introduction

This doc is a quick introduction about the project and its architecture.

It's aim is to help new developers to understand the overall project and where to start developing.

Other useful documentation:
- all the docs in this folder!
- the [contributing doc](../CONTRIBUTING.md), that you should also read carefully.

### Quick introduction to Matrix

Matrix website: [matrix.org](https://matrix.org), [discover page](https://matrix.org/discover).
*Note*: Matrix.org is also hosting a homeserver ([.well-known file](https://matrix.org/.well-known/matrix/client)).
The reference homeserver (this is how Matrix servers are called) implementation is [Synapse](https://github.com/matrix-org/synapse/). But other implementations exist. The Matrix specification is here to ensure that any Matrix client, such as Element Android and its SDK can talk to any Matrix server.

Have a quick look to the client-server API documentation: [Client-server documentation](https://spec.matrix.org/v1.3/client-server-api/). Other network API exist, the list is here: (https://spec.matrix.org/latest/)

Matrix is an open source protocol. Change are possible and are tracked using [this GitHub repository](https://github.com/matrix-org/matrix-doc/). Changes to the protocol are called MSC: Matrix Spec Change. These are PullRequest to this project.

Matrix object are Json data. Unstable prefixes must be used for Json keys when the MSC is not merged (i.e. accepted).

#### Matrix data

There are many object and data in the Matrix worlds. Let's focus on the most important and used, `Room` and `Event`

##### Room

`Room` is a place which contains ordered `Event`s. They are identified with their `room_id`. Nearly all the data are stored in rooms, and shared using homeserver to all the Room Member.

*Note*: Spaces are also Rooms with a different `type`.

##### Event

`Events` are items of a Room, where data is embedded.

There are 2 types of Room Event:

- Regular Events: contain useful content for the user (message, image, etc.), but are not necessarily displayed as this in the timeline (reaction, message edition, call signaling).
- State Events: contain the state of the Room (name, topic, etc.). They have a non null value for the key `state_key`.

Also all the Room Member details are in State Events: one State Event per member. In this case, the `state_key` is the matrixId (= userId).

Important Fields of an Event:
- `event_id`: unique across the Matrix universe;
- `room_id`: the room the Event belongs to;
- `type`: describe what the Event contain, especially in the `content` section, and how the SDK should handle this Event;
- `content`: dynamic Event data; depends on the `type`.

So we have a triple `event_id`, `type`, `state_key` which uniquely defines an Event.

#### Sync

This is managed by the Rust SDK.

### The Android project

The project should compile out of the box.

This Android project is a multi modules project.

- `app` module is the Android application module. Other modules are libraries;
- `features` modules contain some UI and can be seen as screen of the application;
- `libraries` modules contain classes that can be useful for other modules to work.

A few details about some modules:

- `libraries-core` module contains utility classes;
- `libraries-designsystem` module contains Composables which can be used across the app (theme, etc.);
- `libraries-elementresources` module contains resource from Element Android (mainly strings);
- `libraries-matrix` module contains wrappers around the Matrix Rust SDK.

Here is the current module dependency graph:

<!-- To update this graph, run `./tools/docs/generateModuleGraph.sh` (one day the CI will do it hopefully). -->
<img src=./images/module_graph.png width=800 />

### Application

This Android project mainly handle the application layer of the whole software. The communication with the Matrix server, as well as the local storage, the cryptography (encryption and decryption of Event, key management, etc.) is managed by the Rust SDK.

The application is responsible to store the session credentials though.

#### Global architecture

Main libraries and frameworks used in this application:

- Navigation state with [Appyx](https://bumble-tech.github.io/appyx/)
- DI: [Dagger](https://dagger.dev/) and [Anvil](https://github.com/square/anvil)
- Reactive State management with Compose runtime and [Molecule](https://github.com/cashapp/molecule)

Some patterns are inspired by [Circuit](https://slackhq.github.io/circuit/)

**Note**: No more Android Architecture Component ViewModel, no more Mavericks, and of course no more Epoxy!

#### Jetpack Compose

Some useful links:
- https://developer.android.com/jetpack/compose/mental-model
- https://developer.android.com/jetpack/compose/libraries
- https://developer.android.com/jetpack/compose/modifiers-list
- https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md#api-guidelines-for-jetpack-compose

About Preview
- https://alexzh.com/jetpack-compose-preview/

#### Template

(TODO: This is coming)
There is a template module to easily start a new feature. When creating a new module, you can just copy paste the template.

### Push

**Note** Firebase Push is not yet implemented on the project.

Please see the dedicated [documentation](notifications.md) for more details.

This is the classical scenario:

- App receives a Push. Note: Push is ignored if app is in foreground;
- App asks the SDK to load Event data (fastlane mode). We have a change to get the data faster and display the notification faster;
- App asks the SDK to perform a sync request.

### Dependencies management

We are using [Gradle version catalog](https://docs.gradle.org/current/userguide/platforms.html#sub:central-declaration-of-dependencies) on this project.

All the dependencies (including android artifact, gradle plugin, etc.) should be declared in [../gradle/libs.versions.toml](libs.versions.toml) file.
Some dependency, mainly because they are not shared can be declared in `build.gradle.kts` files.

[Dependabot](https://github.com/dependabot) is set up on the project. This tool will automatically create Pull Request to upgrade our dependencies one by one.
**Note** Dependabot does not support yet Gradle verrsion catalog. This is tracked by [this issue](https://github.com/dependabot/dependabot-core/issues/3121).

### Test

We have 3 tests frameworks in place:

- Maestro to test the global usage of the application. See the related [documentation](../.maestro/README.md).
- Combination of [Showkase](https://github.com/airbnb/Showkase) and [Paparazzi](https://github.com/cashapp/paparazzi), to test UI pixel perfect. To add test, just add `@Preview` for the composable you are adding. See the related [documentation](screenshot_testing.md).
- Tests on presenter with Molecule and [Turbine](https://github.com/cashapp/turbine) (TODO this is coming)

**Note** For now we want to avoid using mock (such as *mockk*), because this should be note necessary.

### Other points

#### Logging

**Important warning: ** NEVER log private user data, or use the flag `LOG_PRIVATE_DATA`. Be very careful when logging `data class`, all the content will be output!

[Timber](https://github.com/JakeWharton/timber) is used to log data to logcat. We do not use directly the `Log` class. If possible please use a tag, as per

````kotlin
Timber.tag(loggerTag.value).d("my log")
````

because automatic tag (= class name) will not be available on the release version.

Also generally it is recommended to provide the `Throwable` to the Timber log functions.

Last point, note that `Timber.v` function may have no effect on some devices. Prefer using `Timber.d` and up.

#### Rageshake

Rageshake is a feature to send bug report directly from the application. Just shake your phone and you will be prompted to send a bug report.

Bug reports can contain:
- a screenshot of the current application state
- the application logs from up to 15 application starts
- the logcat logs

The data will be sent to an internal server, which is not publicly accessible. A GitHub issue will also be created to a private GitHub repository.

Rageshake can be very useful to get logs from a release version of the application.

### Tips

- Element Android has a `developer mode` in the `Settings/Advanced settings`. Other useful options are available here; (TODO Not supported yet!)
- Show hidden Events can also help to debug feature. When developer mode is enabled, it is possible to view the source (= the Json content) of any Events; (TODO Not supported yet!)
- Type `/devtools` in a Room composer to access a developer menu. There are some other entry points. Developer mode has to be enabled; (TODO Not supported yet!)
- Hidden debug menu: when developer mode is enabled and on debug build, there are some extra screens that can be accessible using the green wheel. In those screens, it will be possible to toggle some feature flags; (TODO Not supported yet!)
- Using logcat, filtering with `Compositions` can help you to understand what screen are currently displayed on your device. Searching for string displayed on the screen can also help to find the running code in the codebase.
- When this is possible, prefer using `sealed interface` instead of `sealed class`;
- When writing temporary code, using the string "DO NOT COMMIT" in a comment can help to avoid committing things by mistake. If committed and pushed, the CI will detect this String and will warn the user about it. (TODO Not supported yet!)

## Happy coding!

The team is here to support you, feel free to ask anything to other developers.

Also please feel to update this documentation, if incomplete/wrong/obsolete/etc.

**Thanks!**
