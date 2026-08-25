/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

sealed interface ConfirmAccountProviderEvent {
    data class UserInputChanged(val accountProvider: String) : ConfirmAccountProviderEvent

    // Carries the account provider to submit (the current field text, or the accepted autocomplete suggestion),
    // captured from the view at click time so it can't lag behind fast input (e.g. keyboard autofill).
    data class Continue(val accountProvider: String) : ConfirmAccountProviderEvent
    data object ClearError : ConfirmAccountProviderEvent
}
