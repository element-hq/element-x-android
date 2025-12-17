/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.loginpassword

sealed interface LoginPasswordEvents {
    data class SetLogin(val login: String) : LoginPasswordEvents
    data class SetPassword(val password: String) : LoginPasswordEvents
    data object Submit : LoginPasswordEvents
    data object ClearError : LoginPasswordEvents
}
