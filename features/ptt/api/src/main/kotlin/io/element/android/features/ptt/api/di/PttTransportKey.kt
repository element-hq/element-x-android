/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api.di

import dev.zacsweers.metro.MapKey
import io.element.android.features.ptt.api.PttTransportType

/**
 * [MapKey] for contributing a [io.element.android.features.ptt.api.PttTransportFactory] into the
 * transport map multibinding, keyed by the [PttTransportType] it fulfils. The dispatcher selects
 * the factory for a channel's [PttTransportType]; a build that doesn't contribute a given transport
 * (e.g. FOSS without the enterprise Mumble module) simply has no entry for it.
 */
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class PttTransportKey(val value: PttTransportType)
