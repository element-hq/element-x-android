/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.encryption

/**
 * Estimates the strength of a password or passphrase using the zxcvbn algorithm,
 * as exposed by the Rust SDK. Backed by pattern matching (dictionary words,
 * keyboard walks, repeats, dates, l33t speak, …) rather than naive per-character
 * entropy, so it agrees with the iOS client for cross-platform parity.
 *
 * App-scoped: obtain an instance via dependency injection rather than constructing one.
 */
interface PasswordStrengthEstimator {
    /**
     * Estimate the strength of [password].
     *
     * @param password the password or passphrase to evaluate.
     * @param userInputs optional context (e.g. user ID, display name, email) that the
     *   estimator penalises when it appears inside the password, so personal information
     *   does not inflate the score.
     */
    fun estimate(password: String, userInputs: List<String> = emptyList()): PasswordStrengthEstimate
}

/**
 * The result of a [PasswordStrengthEstimator.estimate] call.
 */
data class PasswordStrengthEstimate(
    /** Overall ranking, from [PasswordStrengthRanking.VeryWeak] to [PasswordStrengthRanking.VeryStrong]. */
    val ranking: PasswordStrengthRanking,
    /**
     * `log10` of the estimated number of guesses needed to crack the password. Grows without an
     * upper bound and is independent of any threshold configuration.
     */
    val score: Double,
    /**
     * [score] normalized to `0.0..1.0` against the estimator's `VeryStrong` threshold
     * (`score / veryStrong`), suitable for driving a strength meter. Scores above the `VeryStrong`
     * threshold can exceed `1.0`, so clamp before use.
     */
    val normalScore: Double,
)

/**
 * A ranking of estimated password strength. Derived by the SDK from [PasswordStrengthEstimate.score]
 * against a fixed set of thresholds tuned for modern hardware.
 */
enum class PasswordStrengthRanking {
    VeryWeak,
    Weak,
    Fair,
    Strong,
    VeryStrong,
}
