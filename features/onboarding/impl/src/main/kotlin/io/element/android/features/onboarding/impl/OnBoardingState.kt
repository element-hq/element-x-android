/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.onboarding.impl

data class OnBoardingState(
    val productionApplicationName: String,
    val canLoginWithQrCode: Boolean,
    val canCreateAccount: Boolean,
    val canReportBug: Boolean,
)
