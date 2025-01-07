/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.createroom

/**
 * Rules to override the default room join rules.
 */
sealed interface JoinRuleOverride {
    data object Knock : JoinRuleOverride
    data object None : JoinRuleOverride
}
