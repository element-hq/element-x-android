/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Multibinds
import io.element.android.features.ptt.api.PttChannelConfig
import io.element.android.features.ptt.api.PttTransport
import io.element.android.features.ptt.api.PttTransportFactory
import io.element.android.features.ptt.api.PttTransportType

/**
 * Declares the transport map multibinding. Present so the map is valid even when no transport is
 * contributed (e.g. a FOSS build without the enterprise Mumble module).
 */
@BindingContainer
@ContributesTo(AppScope::class)
interface PttTransportFactoriesModule {
    @Multibinds
    fun pttTransportFactories(): @JvmSuppressWildcards Map<PttTransportType, PttTransportFactory>
}

/**
 * The [PttTransportFactory] the app injects: dispatches to the per-transport factory registered for
 * the channel's [PttChannelConfig.transport]. Throws if that transport isn't available in this
 * build, which the caller surfaces as "push-to-talk unavailable" rather than a crash.
 */
@ContributesBinding(AppScope::class)
@Inject
class DefaultPttTransportFactory(
    private val factories: @JvmSuppressWildcards Map<PttTransportType, PttTransportFactory>,
) : PttTransportFactory {
    override fun create(config: PttChannelConfig): PttTransport {
        val factory = factories[config.transport]
            ?: error("No PTT transport registered for ${config.transport} in this build")
        return factory.create(config)
    }

    /** Whether this build can service [transport] (drives the "PTT available?" check). */
    fun supports(transport: PttTransportType): Boolean = factories.containsKey(transport)
}
