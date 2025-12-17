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

content = minidom.parse(file)

# sort content by value of tag name
newContent = minidom.Document()
resources = newContent.createElement('resources')
resources.setAttribute('xmlns:xliff', 'urn:oasis:names:tc:xliff:document:1.2')
newContent.appendChild(resources)

resource = dict()

### Strings
for elem in content.getElementsByTagName('string'):
    name = elem.attributes['name'].value
    # Continue if value is empty
    child = elem.firstChild
    if child is None:
        # Print an error to stderr
        print('Warning: Empty content for string: ' + name + " in file " + file, file=sys.stderr)
        continue
    value = child.nodeValue
    # Continue if string is empty
    if value == '""':
        # Print an error to stderr
        print('Warning: Empty string value for string: ' + name + " in file " + file, file=sys.stderr)
        continue
    resource[name] = elem.cloneNode(True)

### Plurals
for elem in content.getElementsByTagName('plurals'):
    plural = newContent.createElement('plurals')
    name = elem.attributes['name'].value
    plural.setAttribute('name', name)
    for it in elem.childNodes:
        if it.nodeType != it.ELEMENT_NODE:
            continue
        # Continue if value is empty
        child = it.firstChild
        if child is None:
            # Print an error to stderr
            print('Warning: Empty content for plurals: ' + name + " in file " + file, file=sys.stderr)
            continue
        value = child.nodeValue
        # Continue if string is empty
        if value == '""':
            # Print an error to stderr
            print('Warning: Empty item value for plurals: ' + name + " in file " + file, file=sys.stderr)
            continue
        plural.appendChild(it.cloneNode(True))
    if plural.hasChildNodes():
        resource[name] = plural

for key in sorted(resource.keys()):
    resources.appendChild(resource[key])

result = newContent.toprettyxml(indent="  ") \
    .replace('<?xml version="1.0" ?>', '<?xml version="1.0" encoding="utf-8"?>') \
    .replace('&quot;', '"') \
    .replace('...', '…')

## Replace space by unbreakable space before punctuation
result = re.sub(r" ([\?\!\:…])", r" \1", result)

# Special treatment for French wording
if 'values-fr' in file:
    ## Replace ' with ’
    result = re.sub(r"([cdjlmnsu])\\\'", r"\1’", result, flags=re.IGNORECASE)

with open(file, "w") as text_file:
    text_file.write(result)
