/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.noop

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.MobileScreen
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NoopScreenTrackerTest {
    @Test
    fun `TrackScreen is no op`() = runTest {
        val sut = NoopScreenTracker()
        moleculeFlow(RecompositionMode.Immediate) {
            sut.TrackScreen(MobileScreen.ScreenName.RoomMembers)
        }.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }
}
