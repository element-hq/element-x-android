/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api.di

import dev.zacsweers.metro.MapKey
import io.element.android.features.ptt.api.PttInputSourceId

/**
 * [MapKey] for contributing a [io.element.android.features.ptt.api.PttInputSourceFactory] into the
 * input-source map multibinding, keyed by the [PttInputSourceId] it provides. A build that doesn't
 * contribute a given source (e.g. FOSS without the enterprise BLE module) simply has no entry for it.
 */
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class PttInputSourceKey(val value: PttInputSourceId)
