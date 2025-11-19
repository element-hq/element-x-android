/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api

import java.util.Objects

sealed class AnalyticsLongRunningTransaction(
    val name: String,
    val operation: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (other !is AnalyticsLongRunningTransaction) return false
        return name == other.name && operation == other.operation
    }

    override fun hashCode(): Int {
        return Objects.hash(name, operation)
    }

    data object FirstRoomsDisplayed : AnalyticsLongRunningTransaction("First rooms displayed after login or restoration", null)
}
