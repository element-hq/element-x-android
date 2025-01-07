/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import io.element.android.libraries.architecture.Presenter
import kotlin.time.Duration

suspend fun <State> Presenter<State>.test(
    timeout: Duration? = null,
    name: String? = null,
    validate: suspend TurbineTestContext<State>.() -> Unit,
) {
    moleculeFlow(RecompositionMode.Immediate) {
        present()
    }.test(timeout, name, validate)
}
