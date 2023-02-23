[![Latest build](https://github.com/vector-im/element-x-android/actions/workflows/build.yml/badge.svg?query=branch%3Adevelop)](https://github.com/vector-im/element-x-android/actions/workflows/build.yml?query=branch%3Adevelop)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=vector-im_element-x-android&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=vector-im_element-x-android)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=vector-im_element-x-android&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=vector-im_element-x-android)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=vector-im_element-x-android&metric=bugs)](https://sonarcloud.io/summary/new_code?id=vector-im_element-x-android)
[![codecov](https://codecov.io/github/vector-im/element-x-android/branch/develop/graph/badge.svg?token=ecwvia7amV)](https://codecov.io/github/vector-im/element-x-android)
[![Element Android Matrix room #element-android:matrix.org](https://img.shields.io/matrix/element-android:matrix.org.svg?label=%23element-android:matrix.org&logo=matrix&server_fqdn=matrix.org)](https://matrix.to/#/#element-android:matrix.org)
[![Weblate](https://translate.element.io/widgets/element-android/-/svg-badge.svg)](https://translate.element.io/engage/element-android/?utm_source=widget)

# element-x-android

ElementX Android is a [Matrix](https://matrix.org/) Android Client provided by [Element](https://element.io/).

The application is a total rewrite of [Element-Android](https://github.com/vector-im/element-android) using the [Matrix Rust SDK](https://github.com/matrix-org/matrix-rust-sdk) underneath and targeting devices running Android 5+. The UI layer is written using Jetpack compose.

<!--- TOC -->

* [Screenshots](#screenshots)
* [Rust SDK](#rust-sdk)
* [Contributing](#contributing)
* [Build instructions](#build-instructions)
* [Support](#support)
* [Copyright & License](#copyright-&-license)

<!--- END -->

## Screenshots

Here are some early screenshots of the application:

|<img src=./docs/images/screen1.png width=280 />|<img src=./docs/images/screen2.png width=280 />|<img src=./docs/images/screen3.png width=280 />|<img src=./docs/images/screen4.png width=280 />|
|-|-|-|-|

## Rust SDK

ElementX leverages the [Matrix Rust SDK](https://github.com/matrix-org/matrix-rust-sdk) through an FFI layer that the final client can directly import and use.

We're doing this as a way to share code between platforms and while we've seen promising results it's still in the experimental stage and bound to change.

## Contributing

Please see our [contribution guide](CONTRIBUTING.md).

Come chat with the community in the dedicated Matrix [room](https://matrix.to/#/#element-android:matrix.org).

## Build instructions

Just clone the project and open it in Android Studio.

## Support

When you are experiencing an issue on ElementX Android, please first search in [GitHub issues](https://github.com/vector-im/element-x-android/issues)
and then in [#element-android:matrix.org](https://matrix.to/#/#element-android:matrix.org).
If after your research you still have a question, ask at [#element-android:matrix.org](https://matrix.to/#/#element-android:matrix.org). Otherwise feel free to create a GitHub issue if you encounter a bug or a crash, by explaining clearly in detail what happened. You can also perform bug reporting (Rageshake) from the Element application by shaking your phone or going to the application settings. This is especially recommended when you encounter a crash.

## Copyright & License

Copyright (c) 2022 New Vector Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the [LICENSE](LICENSE) file, or at:

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
