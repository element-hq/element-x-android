/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.matrix.api.encryption.PasswordStrengthEstimate
import io.element.android.libraries.matrix.api.encryption.PasswordStrengthEstimator
import io.element.android.libraries.matrix.api.encryption.PasswordStrengthRanking
import org.matrix.rustcomponents.sdk.PasswordStrengthEstimate as SdkPasswordStrengthEstimate
import org.matrix.rustcomponents.sdk.PasswordStrengthEstimator as SdkPasswordStrengthEstimator
import org.matrix.rustcomponents.sdk.PasswordStrengthRanking as SdkPasswordStrengthRanking

@ContributesBinding(AppScope::class)
class RustPasswordStrengthEstimator : PasswordStrengthEstimator {
    // The SDK ranking is derived from caller-supplied thresholds. We use the "modern hardware (2025)"
    // preset so Android and iOS rank passphrases identically. The zxcvbn estimator holds no per-password
    // state, so a single app-scoped instance is reused for every keystroke rather than reconstructed.
    private val estimator by lazy { SdkPasswordStrengthEstimator.withModernDefaults2025() }

    override fun estimate(password: String, userInputs: List<String>): PasswordStrengthEstimate {
        return estimator.estimate(password, userInputs).toApiModel()
    }
}

internal fun SdkPasswordStrengthEstimate.toApiModel() = PasswordStrengthEstimate(
    ranking = ranking.toApiModel(),
    score = score,
    normalScore = normalScore,
)

internal fun SdkPasswordStrengthRanking.toApiModel() = when (this) {
    SdkPasswordStrengthRanking.VERY_WEAK -> PasswordStrengthRanking.VeryWeak
    SdkPasswordStrengthRanking.WEAK -> PasswordStrengthRanking.Weak
    SdkPasswordStrengthRanking.FAIR -> PasswordStrengthRanking.Fair
    SdkPasswordStrengthRanking.STRONG -> PasswordStrengthRanking.Strong
    SdkPasswordStrengthRanking.VERY_STRONG -> PasswordStrengthRanking.VeryStrong
}
