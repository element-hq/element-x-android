/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.createaccount

sealed interface CreateAccountEvents {
    data class SetPageProgress(val progress: Int) : CreateAccountEvents
    data class OnMessageReceived(val message: String) : CreateAccountEvents
}
