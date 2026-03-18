/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.bridge

enum class BridgeType {
    NONE,           // sentinel: checked, confirmed not a bridge room
    WHATSAPP,
    SIGNAL,
    DISCORD,
    TELEGRAM,
    META,       // Facebook Messenger / Instagram
    IMESSAGE,
    SLACK,
    GOOGLE_CHAT,
    GOOGLE_MESSAGES, // RCS via mautrix-gmessages
    GENERIC,    // unknown bridge bot — fallback icon
}
