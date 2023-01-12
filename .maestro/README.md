# Maestro

Maestro is a framework that we are using to test navigation across the application.
To setup, please refer at [https://maestro.mobile.dev](https://maestro.mobile.dev)

<!--- TOC -->

* [Run test](#run-test)
  * [Output](#output)
* [Write test](#write-test)
* [CI](#ci)

<!--- END -->

## Run test

From root dir of the project

*Note: Since ElementX does not allow account creation nor room creation, we have to use an existing account with an existing room to run maestro test suite. So to run locally, please replace `user` and `123` with your test matrix.org account credentials, and `my room` with one of a room this account has join. Note that the test will send messages to this room.*

```shell
maestro test \
    -e APP_ID=io.element.android.x.debug \
    -e USERNAME=user \
    -e PASSWORD=123 \
    -e ROOM_NAME="my room" \
    .maestro/allTest.yaml
```

### Output

Test result will be printed on the console, and screenshots will be generated at `./build/maestro`

## Write test

Tests are yaml file. Generally each yaml file should leave the app in the same screen than at the beginning.

Start the app and run this command to help writing test.

```shell
maestro studio
```

Note that sometimes, this prevent running the test. So kill the `meastro studio` process to be able to run the test again.

## CI

The CI is running maestro using the workflow `.github/worflow/maestro.yaml` and [maestro cloud](https://cloud.mobile.dev/). For now we are limited to 100 runs a month.
Some GitHub secret are used to be able to do that: `MAESTRO_CLOUD_API_KEY`, for now api key from `benoitm@element.io` maestro cloud account, and `MATRIX_MAESTRO_ACCOUNT_PASSWORD` which is the password of the account @maestroelement:matrix.org. This account contains a room `MyRoom` to ba able to run the maestro test suite.
