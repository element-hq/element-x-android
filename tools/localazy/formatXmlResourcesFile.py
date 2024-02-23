#!/usr/bin/env python3

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
    value = elem.firstChild.nodeValue
    # Continue if value is empty
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
        value = it.firstChild.nodeValue
        # Continue if value is empty
        if value == '""':
            # Print an error to stderr
            print('Warning: Empty item value for plural: ' + name + " in file " + file, file=sys.stderr)
            continue
        plural.appendChild(it.cloneNode(True))
    if plural.hasChildNodes():
        resource[name] = plural

for key in sorted(resource.keys()):
    resources.appendChild(resource[key])

result = newContent.toprettyxml(indent="  ") \
    .replace('<?xml version="1.0" ?>', '<?xml version="1.0" encoding="utf-8"?>') \
    .replace('&quot;', '"')

with open(file, "w") as text_file:
    text_file.write(result)
