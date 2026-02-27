#!/usr/bin/env python3
import xml.etree.ElementTree as ET
import sys

def parse_test_failures(xml_file):
    """Parse XML test results and print failures."""
    tree = ET.parse(xml_file)
    root = tree.getroot()

    # Find all testcase elements with failure children
    if root.get("failures", "0") == "0":
        return

    name = root.get('name', 'Test Suite')
    print(f"## {name}")

    printed_current = False
    for testcase in root.findall('.//testcase'):
        failure = testcase.find('failure')
        if failure is not None:
            if not printed_current:
                current = testcase.get('classname', '')
                print(f"## {current}")
                printed_current = True

            # Get testcase attributes
            classname = testcase.get('classname', '')
            name = testcase.get('name', '')

            # Get failure content (text inside the failure element)
            failure_message = failure.get('message', '')
            failure_content = failure.text if failure.text else ''

            # Print in the requested format
            print(f"### {name}")
            print("```")
            print(failure_message)
            print("```")
            print("<details><summary>Stacktrace</summary>")
            print(f"<pre><code>{failure_content}</code></pre>")
            print("</details>")
            print("\n\n")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: parse_test_failures.py <xml_file>", file=sys.stderr)
        sys.exit(1)

    xml_file = sys.argv[1]
    parse_test_failures(xml_file)

