#!/usr/bin/env python3

import sys
from xml.dom import minidom

file = sys.argv[1]

# Dict of forbidden terms, with exceptions for some String name
# Keys are the terms, values are the exceptions.
forbiddenTerms = {
    "Element": [
        # Those 2 strings are only used in debug version
        "screen_advanced_settings_element_call_base_url",
        "screen_advanced_settings_element_call_base_url_description",
        # only used for element.io homeserver, so it's fine
        "screen_server_confirmation_message_login_element_dot_io",
        # "Be in your element", will probably be changed on the forks, so we can ignore.
        "screen_onboarding_welcome_title",
        # Contains "Element Call"
        "screen_incoming_call_subtitle_android",
    ]
}

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
        if term in value and name not in exceptions:
            errors.append('Forbidden term "' + term + '" in string: "' + name + '": ' + value)

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
            if term in value and name not in exceptions:
                errors.append('Forbidden term "' + term + '" in plural: "' + name + '": ' + value)

# If errors is not empty print the report
if errors:
    print('Error(s) in file ' + file + ":", file=sys.stderr)
    for error in errors:
        print(" - " + error, file=sys.stderr)
    sys.exit(1)
