# Localazy

Localazy is used to host the source strings and their translations.

## Localazy project

To add new strings, or to translate existing strings, go the the Localazy project: [https://localazy.com/p/element](https://localazy.com/p/element).

Never edit manually the files `localazy.xml`!.

## CLI Installation

To install the Localazy client, follow the instructions from [here](https://localazy.com/docs/cli/installation).

## Download translations

In the root folder of the project, run:

```shell
localazy download --config ./tools/localazy/localazy.json
```

It will update all the `localazy.xml` resource files. In case of merge conflicts, just erase the files and download again using the Localazy client.
