# Localazy

Localazy is used to host the source strings and their translations.

## Localazy project

To add new strings, or to translate existing strings, go the the Localazy project: [https://localazy.com/p/element](https://localazy.com/p/element).

Never edit manually the files `localazy.xml` or `translations.xml`!.

## CLI Installation

To install the Localazy client, follow the instructions from [here](https://localazy.com/docs/cli/installation).

## Download translations

In the root folder of the project, run:

```shell
./tools/localazy/downloadStrings.sh
```

It will update all the `localazy.xml` and `translations.xml` resource files. In case of merge conflicts, just erase the files and download again using the script.

## Add translations to a specific module

Edit the file [config.json](./config.json) to add a new module, or add a new item in `includeRegex` arrays, then run the script again to see the effect.

[generateLocalazyConfig.py](generateLocalazyConfig.py) is the Python script that convert `config.json` to a localazy configuration file. Generally you should not edit this file.
