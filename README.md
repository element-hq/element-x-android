[![Build](https://github.com/zachatrocity/ravel/actions/workflows/ravel-release.yml/badge.svg)](https://github.com/zachatrocity/ravel/actions/workflows/ravel-release.yml)

# Ravel

Ravel is an Android Matrix client focused on making bridged messaging feel native. Built on [Element X Android](https://github.com/element-hq/element-x-android) and the [Matrix Rust SDK](https://github.com/matrix-org/matrix-rust-sdk).

## Goals

1. **Maintain alignment with upstream Element X** — Ravel tracks upstream to stay current with the Rust SDK, E2EE improvements, and Matrix spec support. We minimize divergence and rebase regularly.
2. **Messaging via bridges as a first-class citizen** — Self-hosted [mautrix](https://docs.mau.fi/bridges/) bridges (WhatsApp, Signal, iMessage, Telegram) are treated as a core use case, not an afterthought. Bridged contacts, rooms, and status are surfaced naturally in the UI.

## Install

[<img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="80">](https://apps.obtainium.imranr.dev/redirect?r=obtainium://app/%7B%22id%22%3A%22app.ravel.android%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fzachatrocity%2Fravel%22%7D)

Or add manually in Obtainium: `https://github.com/zachatrocity/ravel`

## What's Different from Element X

See [FEATURES.md](FEATURES.md) for a full list of divergences. Key areas:

- Phone contacts as a first-class way to start bridged chats
- Bridge-aware room list (bridged DMs treated as DMs, not group rooms)
- Custom UI and branding distinct from Element

## Upstream

Ravel is a fork of [element-hq/element-x-android](https://github.com/element-hq/element-x-android).

The [Matrix Rust SDK](https://github.com/matrix-org/matrix-rust-sdk) handles E2EE, sync, and all Matrix protocol complexity. We do not modify the SDK — it is a dependency, not a fork.

## Rust SDK

Ravel leverages the [Matrix Rust SDK](https://github.com/matrix-org/matrix-rust-sdk) through an FFI layer. All cryptographic and protocol work lives there.

## Minimum SDK

Android 7.0 (API 24) and above.

## Build Instructions

Clone the project and open it in Android Studio. Select the `app` configuration when building.

To build against a local copy of the Rust SDK, see the [Developer onboarding](docs/_developer_onboarding.md#building-the-sdk-locally) instructions.

For release signing setup, see [SIGNING.md](SIGNING.md).

## Contributing

Open issues and PRs welcome. Check [FEATURES.md](FEATURES.md) to understand the fork's scope before contributing.

## License

AGPL-3.0-only — see [LICENSE](LICENSE).

Ravel is a fork of Element X Android, copyright Element Creations Ltd and New Vector Ltd. Ravel modifications copyright their respective authors.
