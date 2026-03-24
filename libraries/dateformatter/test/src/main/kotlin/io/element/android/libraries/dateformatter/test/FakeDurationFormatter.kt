/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.test

import io.element.android.libraries.dateformatter.api.DurationFormatter
import kotlin.time.Duration

class FakeDurationFormatter(
    private val formatLambda: (Duration) -> String = { it.toString() },
) : DurationFormatter {
    override fun format(duration: Duration): String {
        return formatLambda(duration)
    }
}
