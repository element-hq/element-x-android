/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api

sealed class AnalyticsLongRunningTransaction(
    val name: String,
    val operation: String?,
) {
    data object FirstRoomsDisplayed : AnalyticsLongRunningTransaction("First rooms displayed after login or restoration", null)
}
