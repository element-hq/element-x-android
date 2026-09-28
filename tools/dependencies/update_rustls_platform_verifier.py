#!/usr/bin/env python3

# Copyright (c) 2026 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

"""
Update the rustls-platform-verifier dependency to the given version and regenerate
app/src/main/res/xml/network_security_config.xml.

The rustls-platform-verifier AAR ships a network_security_config.xml allowing cleartext traffic
to the domains used by certificate authorities to publish their CRLs. Since the app has its own
network security config, those domains are merged into a copy of network_security_config_element.xml.

Usage: ./tools/dependencies/update_rustls_platform_verifier.py <version>
"""

import io
import os
import re
import sys
import urllib.request
import xml.etree.ElementTree as ET
import zipfile

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
LIBS_VERSIONS_TOML = os.path.join(ROOT_DIR, "gradle", "libs.versions.toml")
XML_DIR = os.path.join(ROOT_DIR, "app", "src", "main", "res", "xml")
SOURCE_CONFIG = os.path.join(XML_DIR, "network_security_config_element.xml")
TARGET_CONFIG = os.path.join(XML_DIR, "network_security_config.xml")

AAR_URL = (
    "https://raw.githubusercontent.com/rustls/rustls-platform-verifier/maven-archive/android-release-support/"
    "maven/org/rustls/rustls-platform-verifier/{version}/rustls-platform-verifier-{version}.aar"
)
AAR_CONFIG_PATH = "res/xml/network_security_config.xml"

CLEARTEXT_DOMAIN_CONFIG_REGEX = re.compile(
    r'(<domain-config\s+cleartextTrafficPermitted="true"\s*>.*?)(\n?[ \t]*</domain-config>)',
    re.DOTALL,
)


def update_libs_versions(version):
    with open(LIBS_VERSIONS_TOML, "r") as f:
        content = f.read()
    new_content, count = re.subn(
        r'^(rustls_platform_verifier\s*=\s*"org\.rustls:rustls-platform-verifier:)[^"]+(")',
        rf"\g<1>{version}\g<2>",
        content,
        flags=re.MULTILINE,
    )
    if count != 1:
        sys.exit(f"Error: could not find the rustls_platform_verifier dependency in {LIBS_VERSIONS_TOML}")
    with open(LIBS_VERSIONS_TOML, "w") as f:
        f.write(new_content)
    print(f"Updated rustls_platform_verifier to {version} in {os.path.relpath(LIBS_VERSIONS_TOML, ROOT_DIR)}")


def download_aar(version):
    url = AAR_URL.format(version=version)
    print(f"Downloading {url}")
    try:
        with urllib.request.urlopen(url) as response:
            return response.read()
    except urllib.error.HTTPError as e:
        sys.exit(f"Error: could not download the AAR for version {version} ({e.code} {e.reason})")


def extract_cleartext_domains(aar_bytes):
    with zipfile.ZipFile(io.BytesIO(aar_bytes)) as aar:
        try:
            config = aar.read(AAR_CONFIG_PATH)
        except KeyError:
            sys.exit(f"Error: {AAR_CONFIG_PATH} not found in the AAR")
    root = ET.fromstring(config)
    domains = []
    for domain_config in root.iter("domain-config"):
        if domain_config.get("cleartextTrafficPermitted") != "true":
            continue
        for domain in domain_config.findall("domain"):
            domains.append((domain.get("includeSubdomains"), domain.text.strip()))
    if not domains:
        sys.exit("Error: no cleartext domains found in the AAR network_security_config.xml")
    return domains


def write_network_security_config(domains, version):
    with open(SOURCE_CONFIG, "r") as f:
        content = f.read()

    lines = [
        "",
        "",
        f"        <!-- Domains below are imported from rustls-platform-verifier {version} by tools/dependencies/update_rustls_platform_verifier.py -->",
        "        <!-- They are needed to fetch the certificate revocation lists over cleartext -->",
    ]
    for include_subdomains, name in domains:
        attributes = f' includeSubdomains="{include_subdomains}"' if include_subdomains is not None else ""
        lines.append(f"        <domain{attributes}>{name}</domain>")
    extra = "\n".join(lines)

    new_content, count = CLEARTEXT_DOMAIN_CONFIG_REGEX.subn(lambda m: m.group(1) + extra + m.group(2), content, count=1)
    if count != 1:
        sys.exit(f'Error: could not find <domain-config cleartextTrafficPermitted="true"> in {SOURCE_CONFIG}')

    with open(TARGET_CONFIG, "w") as f:
        f.write(new_content)
    print(f"Wrote {os.path.relpath(TARGET_CONFIG, ROOT_DIR)} with {len(domains)} extra domains")


def main():
    if len(sys.argv) != 2:
        sys.exit(f"Usage: {sys.argv[0]} <version>")
    version = sys.argv[1].strip()
    if not re.fullmatch(r"[0-9A-Za-z.\-]+", version):
        sys.exit(f"Error: invalid version '{version}'")

    # Download first, so nothing is modified if the version does not exist
    aar_bytes = download_aar(version)
    domains = extract_cleartext_domains(aar_bytes)
    update_libs_versions(version)
    write_network_security_config(domains, version)


if __name__ == "__main__":
    main()
