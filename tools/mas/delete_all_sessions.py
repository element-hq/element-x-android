#!/usr/bin/env python3

# Copyright (c) 2026 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.

"""End all the sessions of a Matrix account, using MAS (Matrix Authentication Service).

The script logs in to MAS using the web login form (username and password), and then uses
the MAS GraphQL API with the obtained browser session cookie to list and end all the active
compatibility sessions, OAuth 2.0 sessions and browser sessions of the account.

Requirements:
- MAS password login must be enabled, with no captcha on the login form.
- Python package `requests`.
"""

from __future__ import annotations

import argparse
import getpass
import os
import re
import sys
import time
from dataclasses import dataclass

import requests

PAGE_SIZE = 100
# Timeout in seconds for each HTTP request, so that the script never hangs forever.
TIMEOUT = 30

SESSION_TYPES = {
    # type: (User field, node fields, mutation, input field)
    "compat": ("compatSessions", "id deviceId lastActiveAt", "endCompatSession", "compatSessionId"),
    "oauth2": ("oauth2Sessions", "id lastActiveAt client { clientName clientId }", "endOauth2Session", "oauth2SessionId"),
    "browser": ("browserSessions", "id lastActiveAt userAgent { raw }", "endBrowserSession", "browserSessionId"),
}


@dataclass
class MasSession:
    type: str
    id: str
    description: str
    last_active_at: str | None


# Set by the --verbose option.
verbose_logs = False


def log(message: str, verbose: bool = False) -> None:
    """Log a message. Verbose messages are only displayed with the --verbose option."""
    if verbose and not verbose_logs:
        return
    print(f"[{time.strftime('%H:%M:%S')}] {message}", flush=True)


class MasClient:
    def __init__(self, mas_url: str):
        self.mas_url = mas_url.rstrip("/")
        self.http = requests.Session()

    def login(self, username: str, password: str) -> None:
        log(f"Fetching the login form from {self.mas_url}/login", verbose=True)
        response = self.http.get(f"{self.mas_url}/login", timeout=TIMEOUT)
        log(f"Got HTTP {response.status_code}", verbose=True)
        response.raise_for_status()
        match = re.search(r'name="csrf"\s+value="([^"]+)"', response.text) or re.search(r'value="([^"]+)"\s+name="csrf"', response.text)
        if not match:
            raise RuntimeError("Could not find the CSRF token in the MAS login form. Is password login enabled?")
        log(f"Submitting the login form for {username}", verbose=True)
        response = self.http.post(
            f"{self.mas_url}/login",
            data={"csrf": match.group(1), "username": username, "password": password},
            allow_redirects=False,
            timeout=TIMEOUT,
        )
        log(f"Got HTTP {response.status_code}", verbose=True)
        # On success MAS redirects, on failure it renders the login form again.
        if not response.is_redirect:
            raise RuntimeError(f"Login failed (HTTP {response.status_code}). Check the username and password.")
        log(f"Logged in, redirected to {response.headers.get('location')}")

    def graphql(self, query: str, variables: dict | None = None) -> dict:
        response = self.http.post(f"{self.mas_url}/graphql", json={"query": query, "variables": variables or {}}, timeout=TIMEOUT)
        log(f"GraphQL request done: HTTP {response.status_code}", verbose=True)
        response.raise_for_status()
        body = response.json()
        if body.get("errors"):
            raise RuntimeError(f"GraphQL error: {body['errors']}")
        return body["data"]

    def current_browser_session_id(self) -> str | None:
        log("Fetching the current browser session", verbose=True)
        data = self.graphql("query { viewerSession { __typename ... on BrowserSession { id } } }")
        return data["viewerSession"].get("id")

    def list_sessions(self, session_type: str) -> tuple[list[MasSession], bool]:
        """Return the active sessions of the given type, and whether the list is complete."""
        field, node_fields, _, _ = SESSION_TYPES[session_type]
        query = f"""
            query ($first: Int!, $after: String) {{
              viewer {{
                __typename
                ... on User {{
                  {field}(state: ACTIVE, first: $first, after: $after) {{
                    edges {{ node {{ {node_fields} }} }}
                    pageInfo {{ hasNextPage endCursor }}
                  }}
                }}
              }}
            }}
        """
        sessions: dict[str, MasSession] = {}
        after = None
        while True:
            log(f"Fetching a page of active {session_type} sessions after cursor {after}", verbose=True)
            viewer = self.graphql(query, {"first": PAGE_SIZE, "after": after})["viewer"]
            if viewer["__typename"] != "User":
                raise RuntimeError("Not logged in to MAS.")
            connection = viewer[field]
            new_count = 0
            for edge in connection["edges"]:
                session = to_mas_session(session_type, edge["node"])
                if session.id not in sessions:
                    sessions[session.id] = session
                    new_count += 1
            page_info = connection["pageInfo"]
            log(
                f"Got {len(connection['edges'])} {session_type} session(s), {new_count} new, {len(sessions)} found so far, "
                f"hasNextPage: {page_info['hasNextPage']}, endCursor: {page_info['endCursor']}",
                verbose=True,
            )
            if not page_info["hasNextPage"]:
                log(f"Found {len(sessions)} active {session_type} session(s)", verbose=True)
                return list(sessions.values()), True
            # MAS can return the same results again for the next page (seen with browser sessions), which would loop forever.
            if new_count == 0 or page_info["endCursor"] in (None, after):
                log(f"Pagination of {session_type} sessions is not progressing, stopping at {len(sessions)} session(s)", verbose=True)
                return list(sessions.values()), False
            after = page_info["endCursor"]

    def list_all_sessions(self) -> tuple[list[MasSession], bool]:
        """Return the active sessions of all types, and whether the list is complete."""
        all_sessions = []
        all_complete = True
        for session_type in SESSION_TYPES:
            sessions, complete = self.list_sessions(session_type)
            all_sessions += sessions
            all_complete = all_complete and complete
        return all_sessions, all_complete

    def end_session(self, session: MasSession) -> str:
        _, _, mutation, input_field = SESSION_TYPES[session.type]
        query = f"""
            mutation ($id: ID!) {{
              {mutation}(input: {{ {input_field}: $id }}) {{ status }}
            }}
        """
        log(f"Ending {session.type} session {session.id} ({session.description})", verbose=True)
        status = self.graphql(query, {"id": session.id})[mutation]["status"]
        log(f"Status: {status}", verbose=True)
        return status


def to_mas_session(session_type: str, node: dict) -> MasSession:
    if session_type == "compat":
        description = f"device {node.get('deviceId')}"
    elif session_type == "oauth2":
        client = node.get("client") or {}
        description = client.get("clientName") or client.get("clientId") or "unknown client"
    else:
        description = (node.get("userAgent") or {}).get("raw") or "unknown user agent"
    return MasSession(session_type, node["id"], description, node.get("lastActiveAt"))


def discover_mas_url(homeserver: str) -> str:
    server = homeserver if homeserver.startswith("http") else f"https://{homeserver}"
    server = server.rstrip("/")
    base_url = server
    log(f"Fetching {server}/.well-known/matrix/client", verbose=True)
    try:
        response = requests.get(f"{server}/.well-known/matrix/client", timeout=10)
        if response.ok:
            base_url = response.json().get("m.homeserver", {}).get("base_url", server).rstrip("/")
        log(f"Got HTTP {response.status_code}, homeserver base URL: {base_url}", verbose=True)
    except (requests.RequestException, ValueError) as e:
        log(f"Could not read the .well-known file ({e}), using {base_url}", verbose=True)
    for path in ("/_matrix/client/v1/auth_metadata", "/_matrix/client/unstable/org.matrix.msc2965/auth_metadata"):
        log(f"Fetching {base_url}{path}", verbose=True)
        response = requests.get(f"{base_url}{path}", timeout=10)
        log(f"Got HTTP {response.status_code}", verbose=True)
        if response.ok:
            return response.json()["issuer"]
    raise RuntimeError(f"Could not discover the MAS URL from {base_url}. Is the homeserver using MAS? Use --mas-url otherwise.")


def main() -> int:
    parser = argparse.ArgumentParser(description="End all the sessions of a Matrix account, using MAS.")
    parser.add_argument("--homeserver", help="Homeserver name or URL, e.g. matrix.example.org. Used to discover the MAS URL.")
    parser.add_argument("--mas-url", help="MAS URL, e.g. https://account.example.org. Overrides the discovery.")
    parser.add_argument("--username", required=True, help="Username (localpart) of the account.")
    parser.add_argument("--password", help="Password of the account. Defaults to the MAS_PASSWORD env variable, or prompted.")
    parser.add_argument("--dry-run", action="store_true", help="Only list the sessions, do not end them.")
    parser.add_argument("--keep-browser-session", action="store_true", help="Do not end the browser session created by this script.")
    parser.add_argument("--verbose", action="store_true", help="Log every request and every session, and list all the sessions.")
    args = parser.parse_args()

    global verbose_logs
    verbose_logs = args.verbose

    if not args.homeserver and not args.mas_url:
        parser.error("one of --homeserver or --mas-url is required")

    password = args.password or os.environ.get("MAS_PASSWORD") or getpass.getpass(f"Password for {args.username}: ")

    try:
        mas_url = args.mas_url or discover_mas_url(args.homeserver)
        log(f"Using MAS at {mas_url}")
        client = MasClient(mas_url)
        client.login(args.username, password)
        own_session_id = client.current_browser_session_id()
        if args.dry_run:
            sessions, complete = client.list_all_sessions()
    except (requests.RequestException, RuntimeError) as e:
        print(f"Error: {e}", file=sys.stderr)
        return 1

    if args.dry_run:
        log(f"Found {len(sessions)} active session(s)")
        for session in sessions:
            own = " (this script)" if session.id == own_session_id else ""
            print(f"  {session.type:<8} {session.id}  last active: {session.last_active_at or '-':<30} {session.description}{own}")
        if not complete:
            log("Warning: MAS pagination is not progressing, so the list is incomplete. More sessions will be found once these are ended.")
        return 0

    # MAS pagination does not always progress, so fetch the sessions, end them, and repeat until none are left.
    # The session used by this script is ended at the end, so that it can end all the others.
    ended = 0
    failed = 0
    attempted: set[str] = set()
    batch_number = 0
    while True:
        batch_number += 1
        try:
            sessions, complete = client.list_all_sessions()
        except (requests.RequestException, RuntimeError) as e:
            print(f"Error: {e}", file=sys.stderr)
            failed += 1
            break
        batch = [session for session in sessions if session.id != own_session_id and session.id not in attempted]
        if not batch:
            if not complete:
                log("Warning: stopping, the remaining sessions cannot be fetched because the previous ones could not be ended")
            break
        log(f"Batch {batch_number}: ending {len(batch)} session(s)" + ("" if complete else ", more will be fetched afterwards"))
        for session in batch:
            attempted.add(session.id)
            if args.verbose:
                print(f"  {session.type:<8} {session.id}  last active: {session.last_active_at or '-':<30} {session.description}")
            if end_session(client, session):
                ended += 1
            else:
                failed += 1
            if (ended + failed) % PAGE_SIZE == 0:
                log(f"Progress: {ended + failed} session(s) processed, {failed} failure(s)")
        if complete:
            break

    if own_session_id:
        if args.keep_browser_session:
            log(f"Keeping browser session {own_session_id} used by this script")
        elif end_session(client, MasSession("browser", own_session_id, "used by this script", None)):
            ended += 1
        else:
            failed += 1

    print(f"Ended {ended} session(s), {failed} failure(s).")
    return 0 if failed == 0 else 1


def end_session(client: MasClient, session: MasSession) -> bool:
    try:
        status = client.end_session(session)
    except (requests.RequestException, RuntimeError) as e:
        status = str(e)
    if status != "ENDED":
        print(f"Failed to end {session.type} session {session.id}: {status}", file=sys.stderr)
    return status == "ENDED"

if __name__ == "__main__":
    sys.exit(main())
