# Element Call

<!--- TOC -->

* [Overview](#overview)
* [Setting a custom base URL](#setting-a-custom-base-url)
* [Why the application does not add the room path itself](#why-the-application-does-not-add-the-room-path-itself)
* [Where the value is read](#where-the-value-is-read)

<!--- END -->

## Overview

Element X embeds a copy of [Element Call](https://github.com/element-hq/element-call) in the
application assets and loads it from
`https://appassets.androidplatform.net/element-call/index.html`. Calls therefore work out of the box
with no extra deployment, and the embedded copy is the one every user gets.

Developers can point the application at another Element Call deployment instead — a local
development server, a staging deployment, or a self-hosted one — with the **Element Call base URL**
developer option.

## Setting a custom base URL

The option lives in *Settings* → *Developer options* → *Element Call*, under "Element Call base
URL". On a release build the *Developer options* entry is hidden until the version number at the
bottom of *Settings* is tapped seven times. Leaving the field empty restores the embedded copy.

The value is used verbatim as the widget base URL, so it must be the URL of the page that joins a
room, not the URL of the deployment root. For the public deployments that means:

| Deployment | Value to enter |
|---|---|
| `call.element.io` | `https://call.element.io/room` |
| `call.element.dev` | `https://call.element.dev/room` |

Entering `https://call.element.io` without the `/room` suffix loads the Element Call lobby rather
than the room, so the call is never joined.

## Why the application does not add the room path itself

The path that joins a room is a property of the deployment, not of Element Call. Unpacking the
Element Call release tarball behind a reverse proxy can put it at any path — `https://call.foo.tld`,
`https://call.foo.tld/some/path/somewhere` — and a proxy that already maps a subdomain onto
`https://1.2.3.4/room` would end up with the suffix twice. The application passes the value through
unchanged so that every one of those deployments can be reached.

## Where the value is read

`AppDeveloperSettingsView` renders the field and sends
`AppDeveloperSettingsEvent.SetCustomElementCallBaseUrl`, which `DefaultAppPreferencesStore` persists.
`DefaultCallWidgetProvider.getWidget` reads it back and falls back to the embedded copy when it is
unset.
