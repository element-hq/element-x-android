/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import androidx.annotation.DrawableRes
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.libraries.architecture.AsyncData

data class OnBoardingState(
    val isAddingAccount: Boolean,
    val productionApplicationName: String,
    val defaultAccountProvider: String?,
    val mustChooseAccountProvider: Boolean,
    val canLoginWithQrCode: Boolean,
    val canCreateAccount: Boolean,
    val canReportBug: Boolean,
    val version: String,
    @DrawableRes
    val onBoardingLogoResId: Int?,
    val loginMode: AsyncData<LoginMode>,
    val eventSink: (OnBoardingEvents) -> Unit,
) {
    val submitEnabled: Boolean
        get() = defaultAccountProvider != null && (loginMode is AsyncData.Uninitialized || loginMode is AsyncData.Loading)
}
