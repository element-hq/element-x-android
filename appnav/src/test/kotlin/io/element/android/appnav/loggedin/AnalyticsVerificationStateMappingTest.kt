/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.loggedin

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.CryptoSessionStateChange
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AnalyticsVerificationStateMappingTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `Test verification Mappings`() = runTest {
        assertThat(SessionVerifiedStatus.Verified.toAnalyticsUserPropertyValue())
            .isEqualTo(UserProperties.VerificationState.Verified)
        assertThat(SessionVerifiedStatus.NotVerified.toAnalyticsUserPropertyValue())
            .isEqualTo(UserProperties.VerificationState.NotVerified)

        assertThat(SessionVerifiedStatus.Verified.toAnalyticsStateChangeValue())
            .isEqualTo(CryptoSessionStateChange.VerificationState.Verified)
        assertThat(SessionVerifiedStatus.NotVerified.toAnalyticsStateChangeValue())
            .isEqualTo(CryptoSessionStateChange.VerificationState.NotVerified)
    }

    @Test
    fun `Test recovery state Mappings`() = runTest {
        assertThat(RecoveryState.UNKNOWN.toAnalyticsUserPropertyValue())
            .isNull()
        assertThat(RecoveryState.WAITING_FOR_SYNC.toAnalyticsUserPropertyValue())
            .isNull()
        assertThat(RecoveryState.INCOMPLETE.toAnalyticsUserPropertyValue())
            .isEqualTo(UserProperties.RecoveryState.Incomplete)
        assertThat(RecoveryState.ENABLED.toAnalyticsUserPropertyValue())
            .isEqualTo(UserProperties.RecoveryState.Enabled)
        assertThat(RecoveryState.DISABLED.toAnalyticsUserPropertyValue())
            .isEqualTo(UserProperties.RecoveryState.Disabled)

        assertThat(RecoveryState.UNKNOWN.toAnalyticsStateChangeValue())
            .isNull()
        assertThat(RecoveryState.WAITING_FOR_SYNC.toAnalyticsStateChangeValue())
            .isNull()
        assertThat(RecoveryState.INCOMPLETE.toAnalyticsStateChangeValue())
            .isEqualTo(CryptoSessionStateChange.RecoveryState.Incomplete)
        assertThat(RecoveryState.ENABLED.toAnalyticsStateChangeValue())
            .isEqualTo(CryptoSessionStateChange.RecoveryState.Enabled)
        assertThat(RecoveryState.DISABLED.toAnalyticsStateChangeValue())
            .isEqualTo(CryptoSessionStateChange.RecoveryState.Disabled)
    }
}
