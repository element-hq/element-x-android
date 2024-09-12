/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.searchaccountprovider

sealed interface SearchAccountProviderEvents {
    /**
     * The user has typed something, expect to get a list of matching account provider results
     * in the state.
     */
    data class UserInput(val input: String) : SearchAccountProviderEvents
}
