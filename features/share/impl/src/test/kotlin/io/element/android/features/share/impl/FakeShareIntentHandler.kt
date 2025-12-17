/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import android.content.Intent

class FakeShareIntentHandler(
    private val onIncomingShareIntent: suspend (
        Intent,
        suspend (List<ShareIntentHandler.UriToShare>) -> Boolean,
        suspend (String) -> Boolean,
    ) -> Boolean = { _, _, _ -> false },
) : ShareIntentHandler {
    override suspend fun handleIncomingShareIntent(
        intent: Intent,
        onUris: suspend (List<ShareIntentHandler.UriToShare>) -> Boolean,
        onPlainText: suspend (String) -> Boolean,
    ): Boolean {
        return onIncomingShareIntent(intent, onUris, onPlainText)
    }
}
