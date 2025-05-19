/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import io.element.android.features.login.impl.screens.confirmaccountprovider.LoginFlow
import io.element.android.libraries.architecture.AsyncData

data class OnBoardingState(
    val productionApplicationName: String,
    val defaultAccountProvider: String?,
    val canLoginWithQrCode: Boolean,
    val canCreateAccount: Boolean,
    val canReportBug: Boolean,
    val loginFlow: AsyncData<LoginFlow>,
    val eventSink: (OnBoardingEvents) -> Unit,
) {
    val submitEnabled: Boolean
        get() = defaultAccountProvider != null && (loginFlow is AsyncData.Uninitialized || loginFlow is AsyncData.Loading)
}
