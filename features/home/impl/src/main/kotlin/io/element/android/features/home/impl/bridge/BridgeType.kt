/*
 * Copyright (c) 2025 Ravel.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.bridge

enum class BridgeType {
    WHATSAPP,
    SIGNAL,
    DISCORD,
    TELEGRAM,
    META,       // Facebook Messenger / Instagram
    IMESSAGE,
    SLACK,
    GOOGLE_CHAT,
    GENERIC,    // unknown bridge bot — fallback icon
}
