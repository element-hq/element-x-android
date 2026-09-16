#!/usr/bin/env python3

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2024, 2025 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

import re
import sys
from xml.dom import minidom

file = sys.argv[1]

# Dict of forbidden terms, with exceptions for some String name
# Keys are the terms, values are the exceptions.
forbiddenTerms = {
    r"\bElement\b": [
        # Those 2 strings are only used in debug version
        "screen_advanced_settings_element_call_base_url",
        "screen_advanced_settings_element_call_base_url_description",
        # only used for element.io homeserver, so it's fine
        "screen_server_confirmation_message_login_element_dot_io",
        # "Be in your element", will probably be changed on the forks, so we can ignore.
        "screen_onboarding_welcome_title",
        # Contains "Element Call"
        "screen_incoming_call_subtitle_android",
        "call_invalid_audio_device_bluetooth_devices_disabled",
        # Contains "Element X"
        "screen_room_timeline_legacy_call",
        # We explicitly want to mention Element Pro in these 2:
        "screen_change_server_error_element_pro_required_title",
        "screen_change_server_error_element_pro_required_message",
        # Contains "Element Classic"
        "screen_missing_key_backup_open_element_classic",
        "screen_missing_key_backup_step_1",
        # These are notification sound names
        "screen_notification_settings_sound_element_default",
        "screen_notification_settings_sound_element_fade",
        # This contains the word 'element' in some languages
        "screen_media_upload_preview_item_count",
    ]
}

# A complete, VALID placeholder, anchored to consume the whole token:
#   %%              -> literal percent (allowed)
#   %s %d %f ...    -> simple conversion
#   %1$s %2$d ...   -> positional
#   %.2f %1$.2f     -> optional precision
_VALID_PLACEHOLDER = re.compile(
    r'%(?:\d+\$)?(?:\.\d+)?[a-zA-Z]'
)

# Grab each candidate token: a '%' plus the run of chars that could belong to a
# specifier (digits, '$', '.', and letters). This makes '%sd' and '%1d' surface
# as single tokens instead of being partially swallowed.
_PLACEHOLDER_MATCHES = re.compile(r'%%|%[0-9$.A-Za-z]*')

def find_invalid_placeholders(value):
    """Return the list of malformed placeholder tokens found in `value`."""
    invalid = []
    # The % character is special: when using the strings without placeholder replacement, it will be just printed as-is.
    # However, if the string is used with placeholder replacement, a single '%' will be interpreted as an invalid placeholder,
    # and will cause a crash at runtime unless it's escaped as '%%'.
    has_unescaped_percent = False
    has_escaped_percent = False
    has_valid_placeholders = False
    placeholders = _PLACEHOLDER_MATCHES.findall(value)
    for token in placeholders:
        if token == '%%':
            has_escaped_percent = True
            continue  # escaped percent is fine
        if token == '%':
            has_unescaped_percent = True # We'll check later if having this token alongside valid placeholders is an error
            continue
        if not _VALID_PLACEHOLDER.fullmatch(token):
            invalid.append(token)
        else:
            has_valid_placeholders = True

    # If we found a single unescaped '%' and there are valid placeholders, we assume it'll be interpreted as an invalid placeholder,
    # so this is an error.
    if has_unescaped_percent and has_valid_placeholders:
        invalid.append('%')

    # If we found an escaped percent but no valid placeholders, this is probably a mistake, so we warn about it.
    if has_escaped_percent and not has_valid_placeholders:
        print('Warning: string `' + value + '` contains escaped percent (%%) but no valid placeholders. This is probably a mistake.', file=sys.stderr)

    return invalid


content = minidom.parse(file)

errors = []

### Strings
for elem in content.getElementsByTagName('string'):
    name = elem.attributes['name'].value
    # Continue if value is empty
    child = elem.firstChild
    if child is None:
        # Should not happen
        continue
    value = child.nodeValue
    # If value contains a forbidden term, add the error to errors
    for (term, exceptions) in forbiddenTerms.items():
        matches = re.search(term, value)
        if matches and name not in exceptions:
            errors.append('Forbidden term "' + term + '" in string: "' + name + '": ' + value)

    invalid_placeholders = find_invalid_placeholders(value)
    for placeholder in invalid_placeholders:
        errors.append('Invalid placeholder "' + placeholder + '" in string: "' + name + '": ' + value)

### Plurals
for elem in content.getElementsByTagName('plurals'):
    name = elem.attributes['name'].value
    for it in elem.childNodes:
        if it.nodeType != it.ELEMENT_NODE:
            continue
        # Continue if value is empty
        child = it.firstChild
        if child is None:
            # Should not happen
            continue
        value = child.nodeValue
        # If value contains a forbidden term, add the error to errors
        for (term, exceptions) in forbiddenTerms.items():
            matches = re.search(term, value)
            if matches and name not in exceptions:
                errors.append('Forbidden term "' + term + '" in plural: "' + name + '": ' + value)

        invalid_placeholders = find_invalid_placeholders(value)
        for placeholder in invalid_placeholders:
            errors.append('Invalid placeholder "' + placeholder + '" in plural: "' + name + '": ' + value)

# If errors is not empty print the report
if errors:
    print('Error(s) in file ' + file + ":", file=sys.stderr)
    for error in errors:
        print(" - " + error, file=sys.stderr)
    sys.exit(1)
