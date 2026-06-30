/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

/**
 * Device-local setting controlling when URL previews are shown in the timeline.
 * - [On] shows previews in every room.
 * - [Off] never shows previews.
 * - [UnencryptedOnly] shows previews only in unencrypted rooms.
 */
enum class UrlPreviewValue {
    On,
    Off,
    UnencryptedOnly;

    companion object {
        val DEFAULT = UnencryptedOnly
    }
}

// TODO (deferred): confirm null/unknown-encryption handling with the team before finalizing.
fun UrlPreviewValue?.isUrlPreviewEnabled(isEncrypted: Boolean): Boolean = when (this) {
    UrlPreviewValue.On -> true
    UrlPreviewValue.Off -> false
    null, UrlPreviewValue.UnencryptedOnly -> !isEncrypted
}
