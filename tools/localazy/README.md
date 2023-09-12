# Localazy

Localazy is used to host the source strings and their translations.

<!--- TOC -->

* [Localazy project](#localazy-project)
  * [Key naming rules](#key-naming-rules)
    * [Special suffixes](#special-suffixes)
    * [Placeholders](#placeholders)
* [CLI Installation](#cli-installation)
* [Download translations](#download-translations)
* [Add translations to a specific module](#add-translations-to-a-specific-module)

<!--- END -->

## Localazy project

[![Localazy](https://img.shields.io/endpoint?url=https%3A%2F%2Fconnect.localazy.com%2Fstatus%2Felement%2Fdata%3Fcontent%3Dall%26title%3Dlocalazy%26logo%3Dtrue)](https://localazy.com/p/element)

To add new strings, or to translate existing strings, go the the Localazy project: [https://localazy.com/p/element](https://localazy.com/p/element). Please follow the key naming rules (see below).

Never edit manually the files `localazy.xml` or `translations.xml`!.

### Key naming rules

For code clarity and in order to download strings to the correct module, here are some naming rules to follow as much as possible:

- Keys for common strings, i.e. strings that can be used at multiple places must start by `action.` if this is a verb, or `common.` if not;
- Keys for common accessibility strings must start by `a11y.`. Example: `a11y.hide_password`;
- Keys for common strings should be named to match the string. Example: `action.copy_link` for the string `Copy link`;
- When creating common strings, make sure to enable "Use dot (.) to create nested keys";
- Keys for strings used in a single screen must start with `screen_` followed by the screen name, followed by a free name. Example: `screen_onboarding_welcome_title`;
- Keys can have `_title` or `_subtitle` suffixes. Example: `screen_onboarding_welcome_title`, `screen_change_server_subtitle`;
- For dialogs, keys can have `_dialog_title`, `_dialog_content`, and `_dialog_submit` suffixes. Example: `screen_signout_confirmation_dialog_title`, `screen_signout_confirmation_dialog_content`, `screen_signout_confirmation_dialog_submit`;
- `a11y.` pattern can be used for strings that are only used for accessibility. Example: `a11y.hide_password`, `screen_roomlist_a11y_create_message`;
- Strings for error message can start by `error_`, or contain `_error_` if used in a specific screen only. Example: `error_some_messages_have_not_been_sent`, `screen_change_server_error_invalid_homeserver`;

*Note*: those rules applies for `strings` and for `plurals`.

#### Special suffixes

- if a key is suffixed by `_ios`, it will not be imported in the Android project;
- if a key is suffixed by `_android`, it will not be imported in the iOS project.

So feel free to use those suffixes when necessary for instance when the string content is referring to something related to Android only, or iOS only.

#### Placeholders

Placeholders should have the form `%1$s`, `%1$d`, etc.. Please use numbered placeholders. Note that Localazy will take care of converting the placeholder to Android (-> `%1$s`) and iOS specific format (-> `%1$@`). Ideally add a comment on Localazy to explain with what the placeholder(s) will be replaced at runtime.

## CLI Installation

To install the Localazy client, follow the instructions from [here](https://localazy.com/docs/cli/installation).

## Download translations

In the root folder of the project, run:

```shell
./tools/localazy/downloadStrings.sh
```

It will update all the `localazy.xml` resource files. In case of merge conflicts, just erase the files and download again using the script.

To also include the translations, i.e. the `translations.xml` files, add `--all` argument:

```shell
./tools/localazy/downloadStrings.sh --all
```

## Add translations to a specific module

Edit the file [config.json](./config.json) to add a new module, or add a new item in `includeRegex` arrays, then run the script again to see the effect.

[generateLocalazyConfig.py](generateLocalazyConfig.py) is the Python script that convert `config.json` to a localazy configuration file. Generally you should not edit this file.
