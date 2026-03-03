/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface ShareIntentData : Parcelable {
    @Parcelize
    data class Uris(val text: String?, val uris: List<ShareIntentHandler.UriToShare>) : ShareIntentData

    @Parcelize
    data class PlainText(val content: String) : ShareIntentData
}
