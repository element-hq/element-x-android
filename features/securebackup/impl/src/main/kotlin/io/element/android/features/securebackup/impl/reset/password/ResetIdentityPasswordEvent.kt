/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.password

sealed interface ResetIdentityPasswordEvent {
    data class Reset(val password: String) : ResetIdentityPasswordEvent
    data object DismissError : ResetIdentityPasswordEvent
}
