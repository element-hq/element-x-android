/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

sealed interface OnBoardingEvents {
    data class OnSignIn(
        val defaultAccountProvider: String
    ) : OnBoardingEvents

    data object OnVersionClick : OnBoardingEvents
    data object ClearError : OnBoardingEvents
}
