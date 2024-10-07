/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.loginpassword

sealed interface LoginPasswordEvents {
    data class SetLogin(val login: String) : LoginPasswordEvents
    data class SetPassword(val password: String) : LoginPasswordEvents
    data object Submit : LoginPasswordEvents
    data object ClearError : LoginPasswordEvents
}
