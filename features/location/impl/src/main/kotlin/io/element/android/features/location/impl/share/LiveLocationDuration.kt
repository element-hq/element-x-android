/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.share

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

enum class LiveLocationDuration(
    val duration: Duration,
    val label: String,
) {
    FifteenMinutes(15.minutes, "15 minutes"),
    OneHour(1.hours, "1 hour"),
    EightHours(8.hours, "8 hours");
}
