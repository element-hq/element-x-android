# element-x-android

ElementX Android is a [Matrix](https://matrix.org/) Android Client provided by [Element](https://element.io/).

The application is a total rewrite of [Element-Android](https://github.com/vector-im/element-android) using the [Matrix Rust SDK](https://github.com/matrix-org/matrix-rust-sdk) underneath and targeting devices running Android 5+. The UI layer is written using Jetpack compose.

<!--- TOC -->

* [Rust SDK](#rust-sdk)
* [Roadmap](#roadmap)
* [Contributing](#contributing)
* [Build instructions](#build-instructions)
* [Modules](#modules)
* [Support](#support)
* [Copyright & License](#copyright-&-license)

<!--- END -->

## Rust SDK

ElementX leverages the [Matrix Rust SDK](https://github.com/matrix-org/matrix-rust-sdk) through an FFI layer that the final client can directly import and use.

We're doing this as a way to share code between platforms and while we've seen promising results it's still in the experimental stage and bound to change.

## Roadmap

We are aiming to have a fast and fully functional personal messaging application by the end of year 2023.

## Contributing

Please see our [contribution guide](CONTRIBUTING.md).

Come chat with the community in the dedicated Matrix [room](https://matrix.to/#/#element-android:matrix.org).

## Build instructions

Just clone the project and open it in Android Studio.

## Modules

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
<img src=./docs/images/module_graph.png width=800 />

## Support

When you are experiencing an issue on ElementX Android, please first search in [GitHub issues](https://github.com/vector-im/element-x-android/issues)
and then in [#element-android:matrix.org](https://matrix.to/#/#element-android:matrix.org).
If after your research you still have a question, ask at [#element-android:matrix.org](https://matrix.to/#/#element-android:matrix.org). Otherwise feel free to create a GitHub issue if you encounter a bug or a crash, by explaining clearly in detail what happened. You can also perform bug reporting (Rageshake) from the Element application by shaking your phone or going to the application settings. This is especially recommended when you encounter a crash.

## Copyright & License

Copyright (c) 2022 New Vector Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the [LICENSE](LICENSE) file, or at:

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
