/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.common.nodes

import kotlin.time.Duration

/**
 * @property delayBeforeShowingContent when finite, a loading indicator fades in after this delay. Use it for
 * placeholders which can stay on screen for a long time, or forever, else the application looks frozen.
 */
data class EmptyState(
    val delayBeforeShowingContent: Duration,
)
