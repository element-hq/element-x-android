/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

/**
 * Intent extra keys used to carry a direct-share target between the shortcut's [android.content.Intent]
 * and [DefaultShareIntentHandler]. Internal to this module: nothing outside it needs to know these keys.
 */
internal object ShareIntentExtras {
    const val EXTRA_SHARE_TARGET_ROOM_ID = "io.element.android.features.share.extra.TARGET_ROOM_ID"
    const val EXTRA_SHARE_TARGET_SESSION_ID = "io.element.android.features.share.extra.TARGET_SESSION_ID"
}
