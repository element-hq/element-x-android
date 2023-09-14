/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
