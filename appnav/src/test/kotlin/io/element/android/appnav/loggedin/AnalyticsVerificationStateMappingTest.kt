/*
 * Copyright (c) 2024 New Vector Ltd
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
