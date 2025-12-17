/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.root

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ResetIdentityRootPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = ResetIdentityRootPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.displayConfirmationDialog).isFalse()
        }
    }

    @Test
    fun `present - Continue event displays the confirmation dialog`() = runTest {
        val presenter = ResetIdentityRootPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ResetIdentityRootEvent.Continue)

            assertThat(awaitItem().displayConfirmationDialog).isTrue()
        }
    }

    @Test
    fun `present - DismissDialog event hides the confirmation dialog`() = runTest {
        val presenter = ResetIdentityRootPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ResetIdentityRootEvent.Continue)
            assertThat(awaitItem().displayConfirmationDialog).isTrue()

            initialState.eventSink(ResetIdentityRootEvent.DismissDialog)
            assertThat(awaitItem().displayConfirmationDialog).isFalse()
        }
    }
}
