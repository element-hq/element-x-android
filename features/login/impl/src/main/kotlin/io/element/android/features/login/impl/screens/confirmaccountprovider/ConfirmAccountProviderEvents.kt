/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

sealed interface ConfirmAccountProviderEvents {
    data object Continue : ConfirmAccountProviderEvents
    data object ClearError : ConfirmAccountProviderEvents
}
