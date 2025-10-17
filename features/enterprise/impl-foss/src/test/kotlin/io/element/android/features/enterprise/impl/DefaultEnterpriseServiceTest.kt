/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.compound.tokens.generated.compoundColorsDark
import io.element.android.compound.tokens.generated.compoundColorsLight
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.libraries.matrix.test.A_SESSION_ID
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultEnterpriseServiceTest {
    @Test
    fun `isEnterpriseBuild is false`() {
        val defaultEnterpriseService = DefaultEnterpriseService()
        assertThat(defaultEnterpriseService.isEnterpriseBuild).isFalse()
    }

    @Test
    fun `defaultHomeserverList should return empty list`() {
        val defaultEnterpriseService = DefaultEnterpriseService()
        assertThat(defaultEnterpriseService.defaultHomeserverList()).isEmpty()
    }

    @Test
    fun `isAllowedToConnectToHomeserver is true for all homeserver urls`() = runTest {
        val defaultEnterpriseService = DefaultEnterpriseService()
        assertThat(defaultEnterpriseService.isAllowedToConnectToHomeserver(A_HOMESERVER_URL)).isTrue()
    }

    @Test
    fun `isEnterpriseUser always return false`() = runTest {
        val defaultEnterpriseService = DefaultEnterpriseService()
        assertThat(defaultEnterpriseService.isEnterpriseUser(A_SESSION_ID)).isFalse()
    }

    @Test
    fun `semanticColorsLight always emits the same value`() = runTest {
        val defaultEnterpriseService = DefaultEnterpriseService()
        moleculeFlow(RecompositionMode.Immediate) {
            defaultEnterpriseService.semanticColorsLight().value
        }.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(compoundColorsLight)
            defaultEnterpriseService.overrideBrandColor("#87654321")
            expectNoEvents()
        }
    }

    @Test
    fun `semanticColorsDark always emits the same value`() = runTest {
        val defaultEnterpriseService = DefaultEnterpriseService()
        moleculeFlow(RecompositionMode.Immediate) {
            defaultEnterpriseService.semanticColorsDark().value
        }.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(compoundColorsDark)
            defaultEnterpriseService.overrideBrandColor("#87654321")
            expectNoEvents()
        }
    }
}
