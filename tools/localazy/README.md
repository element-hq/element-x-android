# Localazy

Localazy is used to host the source strings and their translations.

<!--- TOC -->

* [Localazy project](#localazy-project)
  * [Key naming rules](#key-naming-rules)
* [CLI Installation](#cli-installation)
* [Download translations](#download-translations)
* [Add translations to a specific module](#add-translations-to-a-specific-module)

<!--- END -->

## Localazy project

To add new strings, or to translate existing strings, go the the Localazy project: [https://localazy.com/p/element](https://localazy.com/p/element). Please follow the key naming rules (see below).

Never edit manually the files `localazy.xml` or `translations.xml`!.

### Key naming rules

For code clarity and in order to download strings to the correct module, here are some naming rules to follow as much as possible:

- Keys for common strings, i.e. strings that can be used at multiple places must start by `action_` if this is a verb, or `common_` if not;
- Keys for strings used in a single screen must start with `screen_` followed by the screen name, followed by a free name. Example: `screen_onboarding_welcome_title`;
- Keys can have `_title` or `_subtitle` suffixes. Example: `screen_onboarding_welcome_title`, `screen_change_server_subtitle`;
- `a11y_` pattern can be used for strings used for accessibility. Example: `a11y_hide_password`, `screen_roomlist_a11y_create_message`;
- Strings for error message can start by `error_`, or contain `_error_` if used in a specific screen only. Example: `error_some_messages_have_not_been_sent`, `screen_change_server_error_invalid_homeserver`.

*Note*: those rules applies for `strings` and for `plurals`.

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
