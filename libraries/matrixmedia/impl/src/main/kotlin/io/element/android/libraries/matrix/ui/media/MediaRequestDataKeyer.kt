/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import coil3.key.Keyer
import coil3.request.Options
import io.element.android.libraries.designsystem.components.avatar.AvatarData

internal class AvatarDataKeyer : Keyer<AvatarData> {
    override fun key(data: AvatarData, options: Options): String? {
        return data.toMediaRequestData().toKey()
    }
}

internal class MediaRequestDataKeyer : Keyer<MediaRequestData> {
    override fun key(data: MediaRequestData, options: Options): String? {
        return data.toKey()
    }
}

private fun MediaRequestData.toKey(): String? {
    return source?.let { "${it.url}_$kind" }
}
