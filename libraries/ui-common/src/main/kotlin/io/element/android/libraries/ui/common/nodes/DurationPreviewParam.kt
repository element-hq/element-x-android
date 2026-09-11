/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.common.nodes

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

open class DurationPreviewParam : PreviewParameterProvider<Duration> {
    override val values: Sequence<Duration>
        get() = sequenceOf(
            Duration.INFINITE,
            1.seconds,
        )
}
