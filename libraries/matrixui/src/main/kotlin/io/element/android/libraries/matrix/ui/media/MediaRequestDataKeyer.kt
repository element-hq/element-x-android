/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import coil.key.Keyer
import coil.request.Options
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
    if (source == null) return null
    return "${source.url}_$kind"
}
