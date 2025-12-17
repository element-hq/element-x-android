/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.time.Duration.Companion.seconds

/**
 * moleculeFlow can take time to initialise during the first test of any given
 * test class.
 *
 * Applying this test rule ensures that the slow initialisation is not done
 * inside runTest which has a short default timeout.
 */
class WarmUpRule : TestRule {
    companion object {
        init {
            warmUpMolecule()
        }
    }

    override fun apply(base: Statement, description: Description): Statement = base
}

private fun warmUpMolecule() {
    runTest(timeout = 60.seconds) {
        moleculeFlow(RecompositionMode.Immediate) {
            // Do nothing
        }.test {
            awaitItem() // Await a Unit composition
        }
    }
}
