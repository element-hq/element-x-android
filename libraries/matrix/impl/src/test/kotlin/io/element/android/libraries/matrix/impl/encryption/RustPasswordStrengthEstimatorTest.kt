/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.encryption.PasswordStrengthRanking
import io.element.android.libraries.matrix.test.encryption.FakePasswordStrengthEstimator
import io.element.android.libraries.matrix.test.encryption.aPasswordStrengthEstimate
import org.junit.Test
import org.matrix.rustcomponents.sdk.PasswordStrengthEstimate as SdkPasswordStrengthEstimate
import org.matrix.rustcomponents.sdk.PasswordStrengthRanking as SdkPasswordStrengthRanking

class RustPasswordStrengthEstimatorTest {
    @Test
    fun `each SDK ranking maps to the matching api ranking`() {
        assertThat(SdkPasswordStrengthRanking.VERY_WEAK.toApiModel()).isEqualTo(PasswordStrengthRanking.VeryWeak)
        assertThat(SdkPasswordStrengthRanking.WEAK.toApiModel()).isEqualTo(PasswordStrengthRanking.Weak)
        assertThat(SdkPasswordStrengthRanking.FAIR.toApiModel()).isEqualTo(PasswordStrengthRanking.Fair)
        assertThat(SdkPasswordStrengthRanking.STRONG.toApiModel()).isEqualTo(PasswordStrengthRanking.Strong)
        assertThat(SdkPasswordStrengthRanking.VERY_STRONG.toApiModel()).isEqualTo(PasswordStrengthRanking.VeryStrong)
    }

    @Test
    fun `an SDK estimate forwards its ranking, score and normalScore to the api model`() {
        val sdkEstimate = SdkPasswordStrengthEstimate(
            ranking = SdkPasswordStrengthRanking.FAIR,
            guesses = 1_000_000uL,
            score = 6.0,
            normalScore = 0.35,
            feedback = null,
        )
        assertThat(sdkEstimate.toApiModel()).isEqualTo(
            aPasswordStrengthEstimate(
                ranking = PasswordStrengthRanking.Fair,
                score = 6.0,
                normalScore = 0.35,
            )
        )
    }

    @Test
    fun `the fake estimator returns its configured estimate`() {
        val estimate = aPasswordStrengthEstimate(ranking = PasswordStrengthRanking.Strong)
        val fake = FakePasswordStrengthEstimator { _, _ -> estimate }
        assertThat(fake.estimate("hunter2")).isEqualTo(estimate)
    }
}
