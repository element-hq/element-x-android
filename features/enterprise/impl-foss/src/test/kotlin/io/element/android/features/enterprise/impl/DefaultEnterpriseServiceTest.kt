/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.features.enterprise.api.BugReportUrl
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
    fun `semanticColorsFlow always emits the same value`() = runTest {
        val defaultEnterpriseService = DefaultEnterpriseService()
        defaultEnterpriseService.semanticColorsFlow(null).test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(SemanticColorsLightDark.default)
            awaitComplete()
        }
    }

    @Test
    fun `brandColorsFlow always emits null`() = runTest {
        val defaultEnterpriseService = DefaultEnterpriseService()
        defaultEnterpriseService.brandColorsFlow(null).test {
            val initialState = awaitItem()
            assertThat(initialState).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `semanticColorsFlow always emits the same value for a session`() = runTest {
        val defaultEnterpriseService = DefaultEnterpriseService()
        defaultEnterpriseService.semanticColorsFlow(A_SESSION_ID).test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(SemanticColorsLightDark.default)
            awaitComplete()
        }
    }

    @Test
    fun `overrideBrandColor has no effect`() = runTest {
        val defaultEnterpriseService = DefaultEnterpriseService()
        defaultEnterpriseService.overrideBrandColor(A_SESSION_ID, "aColor")
    }

    @Test
    fun `firebasePushGateway returns null`() = runTest {
        val defaultEnterpriseService = DefaultEnterpriseService()
        assertThat(defaultEnterpriseService.firebasePushGateway()).isNull()
    }

    @Test
    fun `unifiedPushDefaultPushGateway returns null`() = runTest {
        val defaultEnterpriseService = DefaultEnterpriseService()
        assertThat(defaultEnterpriseService.unifiedPushDefaultPushGateway()).isNull()
    }

    @Test
    fun `bugReportUrlFlow only emits UseDefault`() = runTest {
        val defaultEnterpriseService = DefaultEnterpriseService()
        defaultEnterpriseService.bugReportUrlFlow(A_SESSION_ID).test {
            assertThat(awaitItem()).isEqualTo(BugReportUrl.UseDefault)
            awaitComplete()
        }
    }
}
