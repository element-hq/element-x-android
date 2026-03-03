/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.api

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

interface ShareIntentHandler {
    @Parcelize
    data class UriToShare(
        val uri: Uri,
        val mimeType: String,
    ) : Parcelable

    /**
     * This methods aims to handle incoming share intents and parse its data.
     *
     * @return the [ShareIntentData] if it could be resolved, or null.
     */
    fun handleIncomingShareIntent(
        intent: Intent
    ): ShareIntentData?
}
