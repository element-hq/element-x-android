/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils

class FakeTextPillificationHelper(
    private val pillifyLambda: (CharSequence) -> CharSequence = { it }
) : TextPillificationHelper {
    override fun pillify(text: CharSequence): CharSequence {
        return pillifyLambda(text)
    }
}
