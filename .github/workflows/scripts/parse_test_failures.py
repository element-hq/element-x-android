#!/usr/bin/env python3
import xml.etree.ElementTree as ET
import sys
import glob

def parse_test_failures(xml_file):
    """Parse XML test results and print failures."""
    tree = ET.parse(xml_file)
    root = tree.getroot()

    # Find all testcase elements with failure children
    if root.get("failures", "0") == "0":
        return

    name = root.get('name', 'Test Suite')
    is_screenshot_test = name.startswith('ui.Preview')

    if not is_screenshot_test:
        print(f"## {name}")

    for testcase in root.findall('.//testcase'):
        failure = testcase.find('failure')
        if failure is not None:
            # Get testcase attributes
            classname = testcase.get('classname', '')
            name = testcase.get('name', '')

            if is_screenshot_test:
                # For screenshot tests, we want to display the classname as well
                screenshot_test_failures.append(f"{classname}.{name}")
            else:
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

screenshot_test_failures = []

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: parse_test_failures.py <file>", file=sys.stderr)
        sys.exit(1)

    file = sys.argv[1]

    if file.endswith('xml'):
        parse_test_failures(file)
    else:
        files = glob.glob("**/build/test-results/*UnitTest/*.xml", root_dir = file, recursive = True)
        for file in files:
            parse_test_failures(file)

    if screenshot_test_failures:
        print("## Screenshot Test Failures")
        for failure in screenshot_test_failures:
            print(f"- {failure}")

