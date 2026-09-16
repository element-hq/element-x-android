/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.deeplink.api

import android.content.Intent

/**
 * Extracts the app's own deep link data from an incoming intent; see [DeepLinkCreator] for the other direction.
 */
fun interface DeeplinkParser {
    /**
     * @param intent the intent the app was started or resumed with.
     * @return the session, room and event to open, or `null` when the intent is not one of the app's deep links.
     */
    fun getFromIntent(intent: Intent): DeeplinkData?
}
