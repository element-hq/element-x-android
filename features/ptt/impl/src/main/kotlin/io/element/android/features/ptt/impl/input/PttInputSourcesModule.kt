/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl.input

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds
import io.element.android.features.ptt.api.PttInputSourceFactory
import io.element.android.features.ptt.api.PttInputSourceId

/**
 * Declares the PTT input-source map multibinding. Present so the map is valid even when only some
 * sources are contributed (e.g. a FOSS build without the enterprise BLE module).
 */
@BindingContainer
@ContributesTo(AppScope::class)
interface PttInputSourcesModule {
    @Multibinds
    fun pttInputSourceFactories(): @JvmSuppressWildcards Map<PttInputSourceId, PttInputSourceFactory>
}
