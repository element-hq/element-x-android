/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.services.analytics.api.ScreenTracker
import io.element.android.tests.testutils.lambda.lambdaError

class FakeScreenTracker(
    private val trackScreenLambda: (MobileScreen.ScreenName) -> Unit = { lambdaError() }
) : ScreenTracker {
    @Composable
    override fun TrackScreen(screen: MobileScreen.ScreenName) {
        LaunchedEffect(Unit) {
            trackScreenLambda(screen)
        }
    }
}
