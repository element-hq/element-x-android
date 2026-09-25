#!/usr/bin/env python3
"""
Install the Diawi build attached to a GitHub PR onto a connected Android device.

Usage:
    ./install_pr_build.py https://github.com/element-hq/element-x-android/pull/7750
    ./install_pr_build.py <pr_url> --serial emulator-5554 --replace

Steps:
  1. Reads the PR description and comments through the GitHub API and finds the
     most recent link of the form https://i.diawi.com/<build_id>.
  2. Scrapes the Diawi page and finds the link labeled "Download application".
  3. Downloads the APK.
  4. Runs `adb install <apk_path>`.

Set GITHUB_TOKEN in the environment to avoid GitHub's anonymous rate limit
(60 requests/hour). Only the Python standard library is used.
"""

import argparse
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import urllib.error
import urllib.request
from html.parser import HTMLParser
from urllib.parse import urljoin

PR_URL_RE = re.compile(r"https?://github\.com/([^/]+)/([^/]+)/pull/(\d+)")
DIAWI_RE = re.compile(r"https://i\.diawi\.com/([A-Za-z0-9]+)")
DOWNLOAD_LABEL = "download application"
USER_AGENT = (
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/128.0 Safari/537.36"
)


def die(msg: str) -> None:
    print(f"error: {msg}", file=sys.stderr)
    sys.exit(1)


def http_get(url: str, headers: dict | None = None) -> urllib.request.addinfourl:
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT, **(headers or {})})
    try:
        return urllib.request.urlopen(req, timeout=60)
    except urllib.error.HTTPError as e:
        die(f"HTTP {e.code} while fetching {url}")
    except urllib.error.URLError as e:
        die(f"could not reach {url}: {e.reason}")


# --------------------------------------------------------------------------- #
# 1. Find the Diawi link in the PR
# --------------------------------------------------------------------------- #
def github_json(url: str):
    headers = {"Accept": "application/vnd.github+json"}
    token = os.environ.get("GITHUB_TOKEN")
    if token:
        headers["Authorization"] = f"Bearer {token}"
    with http_get(url, headers) as resp:
        return json.load(resp), resp.headers.get("Link", "")


def find_diawi_link(pr_url: str) -> str:
    m = PR_URL_RE.match(pr_url)
    if not m:
        die(f"not a GitHub PR URL: {pr_url}")
    owner, repo, number = m.groups()
    api = f"https://api.github.com/repos/{owner}/{repo}"

    texts = []
    pr, _ = github_json(f"{api}/pulls/{number}")
    texts.append(pr.get("body") or "")

    # Comments are returned oldest first; follow pagination to get them all.
    url = f"{api}/issues/{number}/comments?per_page=100"
    while url:
        comments, link_header = github_json(url)
        texts.extend(c.get("body") or "" for c in comments)
        nxt = re.search(r'<([^>]+)>;\s*rel="next"', link_header)
        url = nxt.group(1) if nxt else None

    links = [f"https://i.diawi.com/{bid}" for t in texts for bid in DIAWI_RE.findall(t)]
    if not links:
        die("no https://i.diawi.com/<build_id> link found in the PR")
    return links[-1]  # most recent one


# --------------------------------------------------------------------------- #
# 2. Scrape the Diawi page for the "Download application" link
# --------------------------------------------------------------------------- #
class LinkCollector(HTMLParser):
    """Collects (href, visible text) for every <a> tag."""

    def __init__(self):
        super().__init__()
        self.links: list[tuple[str, str]] = []
        self._href: str | None = None
        self._text: list[str] = []

    def handle_starttag(self, tag, attrs):
        if tag == "a":
            self._href = dict(attrs).get("href")
            self._text = []

    def handle_data(self, data):
        if self._href is not None:
            self._text.append(data)

    def handle_endtag(self, tag):
        if tag == "a" and self._href is not None:
            text = " ".join("".join(self._text).split())
            self.links.append((self._href, text))
            self._href = None


def find_apk_url(diawi_url: str) -> str:
    with http_get(diawi_url) as resp:
        html = resp.read().decode(resp.headers.get_content_charset() or "utf-8", "replace")

    parser = LinkCollector()
    parser.feed(html)

    for href, text in parser.links:
        if href and DOWNLOAD_LABEL in text.lower():
            return urljoin(diawi_url, href)

    # Fallback: anything that looks like a direct APK link.
    for href, _ in parser.links:
        if href and ".apk" in href.lower():
            return urljoin(diawi_url, href)

    die(f"no 'Download application' link found on {diawi_url} "
        "(the build may have expired)")


# --------------------------------------------------------------------------- #
# 3. Download the APK
# --------------------------------------------------------------------------- #
def download(url: str, dest: str) -> None:
    with http_get(url) as resp, open(dest, "wb") as out:
        total = int(resp.headers.get("Content-Length") or 0)
        done = 0
        while chunk := resp.read(1 << 16):
            out.write(chunk)
            done += len(chunk)
            if total:
                print(f"\r  {done * 100 // total:3d}%  {done / 1e6:.1f} MB", end="", flush=True)
    print()

    with open(dest, "rb") as f:
        if f.read(2) != b"PK":  # APKs are zip archives
            die("downloaded file is not an APK (Diawi may have returned an HTML page)")


# --------------------------------------------------------------------------- #
# 4. Install with adb
# --------------------------------------------------------------------------- #
def adb_install(apk: str, serial: str | None, replace: bool) -> int:
    if not shutil.which("adb"):
        die("adb not found in PATH")
    adb = ["adb"] + (["-s", serial] if serial else [])

    devices = subprocess.run(["adb", "devices"], capture_output=True, text=True).stdout
    attached = [l for l in devices.splitlines()[1:] if l.strip().endswith("device")]
    if not attached:
        die("no Android device connected (check `adb devices`)")

    cmd = adb + ["install"] + (["-r"] if replace else []) + [apk]
    print("Running:", " ".join(cmd))
    return subprocess.run(cmd).returncode

def adb_launch(serial: str | None) -> int:
    if not shutil.which("adb"):
        die("adb not found in PATH")
    adb = ["adb"] + (["-s", serial] if serial else [])

    devices = subprocess.run(["adb", "devices"], capture_output=True, text=True).stdout
    attached = [l for l in devices.splitlines()[1:] if l.strip().endswith("device")]
    if not attached:
        die("no Android device connected (check `adb devices`)")

    cmd = adb + ["shell", "am", "start", "-n", "io.element.android.x.debug/io.element.android.x.MainActivity"]

    print("Running:", " ".join(cmd))
    return subprocess.run(cmd).returncode

def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("pr_url", help="GitHub PR URL or PR number (e.g. 7750)")
    ap.add_argument("-s", "--serial", help="device serial, if several devices are connected")
    ap.add_argument("-r", "--replace", action="store_true", help="pass -r to adb install (reinstall, keep data)")
    ap.add_argument("-o", "--output", help="where to save the APK (default: temp dir, deleted afterwards)")
    args = ap.parse_args()

    if args.pr_url.isdigit():
        pr_url = f"https://github.com/element-hq/element-x-android/pull/{args.pr_url}"
    else:
        pr_url = args.pr_url
    print(f"Looking for a Diawi link in {pr_url} ...")
    diawi_url = find_diawi_link(pr_url)
    print(f"Found: {diawi_url}")

    apk_url = find_apk_url(diawi_url)
    print(f"APK link: {apk_url}")

    build_id = diawi_url.rsplit("/", 1)[-1]
    if args.output:
        apk_path = args.output
        tmpdir = None
    else:
        tmpdir = tempfile.mkdtemp(prefix="diawi-")
        apk_path = os.path.join(tmpdir, f"{build_id}.apk")

    print(f"Downloading to {apk_path} ...")
    try:
        download(apk_url, apk_path)
        if adb_install(apk_path, args.serial, args.replace) == 0:
            print("Installed successfully.")
        else:
            die("adb install failed.")
        if adb_launch(args.serial) == 0:
            print("Launched successfully.")
        else:
            die("adb launch failed.")
    finally:
        if tmpdir:
            shutil.rmtree(tmpdir, ignore_errors=True)


if __name__ == "__main__":
    main()
