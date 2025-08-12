/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.api

interface WellknownRetriever {
    suspend fun getWellKnown(baseUrl: String): WellKnown?
    suspend fun getElementWellKnown(baseUrl: String): ElementWellKnown?
}
