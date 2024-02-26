# Maestro

Maestro is a framework that we are using to test navigation across the application.
To setup, please refer at [https://maestro.mobile.dev](https://maestro.mobile.dev)

<!--- TOC -->

* [Run test](#run-test)
  * [Output](#output)
* [Write test](#write-test)
* [CI](#ci)
* [iOS](#ios)
* [Future](#future)

<!--- END -->

## Run test

From root dir of the project

*Note: Since Element X does not allow account creation, we have to use an existing account to run maestro test suite. So to run locally, please replace `user` and `123` with your test matrix.org account credentials, and `my room` with one of a room this account has joined. Note that the test will send messages to this room.*

```shell
maestro test \
    -e MAESTRO_APP_ID=io.element.android.x.debug \
    -e MAESTRO_USERNAME=user1 \
    -e MAESTRO_PASSWORD=123 \
    -e MAESTRO_RECOVERY_KEY=ABC \
    -e MAESTRO_ROOM_NAME="MyRoom" \
    -e MAESTRO_INVITEE1_MXID=user2 \
    -e MAESTRO_INVITEE2_MXID=user3 \
    .maestro/allTests.yaml
```

### Output

Test result will be printed on the console, and screenshots will be generated at `./build/maestro`

## Write test

Tests are yaml files. Generally each yaml file should leave the app in the same screen than at the beginning.

Start the Element X app and run this command to help writing test.

```shell
maestro studio
```

Note that sometimes, this prevent running the test. So kill the `maestro studio` process to be able to run the test again.

Also, if updating the application code, do not forget to deploy again the application before running the maestro tests.

## CI

The CI is running maestro using the workflow `.github/worflow/maestro.yaml` and [maestro cloud](https://cloud.mobile.dev/). For now we are limited to 100 runs a month.
Some GitHub secrets are used to be able to do that: `MAESTRO_CLOUD_API_KEY`, for now api key from `benoitm@element.io` maestro cloud account, and `MATRIX_MAESTRO_ACCOUNT_PASSWORD` which is the password of the account `@maestroelement:matrix.org`. This account contains a room `MyRoom` to be able to run the maestro test suite.

## iOS

Need to install `idb-companion` first

```shell
brew install idb-companion
```

Also:
https://github.com/mobile-dev-inc/maestro/issues/146
https://github.com/mobile-dev-inc/maestro/issues/107
So you have to change your input keyboard to QWERTY for it to work properly.

## Future

- run on Element X iOS. This is already working but it need some change on the test to make it works. Could pass a PLATFORM parameter to have unique test and use conditional test.
- run specific test on both iOS and Android devices to make them communicate together. Could be possible to test room invite and join, verification, call, etc. To be done when Element X will be able to create account and create room. A main script would be able to detect the Android device and the iOS device, and run several maestro tests sequentially, using `--device` parameter to perform a global test.
