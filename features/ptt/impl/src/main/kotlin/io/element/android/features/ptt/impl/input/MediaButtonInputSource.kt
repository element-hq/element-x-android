/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl.input

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.IntoMap
import io.element.android.features.ptt.api.PttInputSource
import io.element.android.features.ptt.api.PttInputSourceFactory
import io.element.android.features.ptt.api.PttInputSourceId
import io.element.android.features.ptt.api.di.PttInputSourceKey
import io.element.android.features.ptt.impl.services.PttMediaButtonController

/**
 * [PttInputSource] for Bluetooth/wired accessories that emulate media keys (Pryme/AINA-style),
 * wrapping the existing [PttMediaButtonController]. Always available; enabled by default.
 */
class MediaButtonInputSource(
    private val context: Context,
) : PttInputSource {
    override val id = PttInputSourceId.MediaButton

    private var controller: PttMediaButtonController? = null

    override fun start(onPressToTalk: () -> Unit, onReleaseToTalk: () -> Unit) {
        if (controller != null) return
        controller = PttMediaButtonController(
            context = context,
            onKeyDown = onPressToTalk,
            onKeyUp = onReleaseToTalk,
        )
    }

    override fun stop() {
        controller?.release()
        controller = null
    }
}

@Inject
class MediaButtonInputSourceFactory : PttInputSourceFactory {
    override val id = PttInputSourceId.MediaButton
    override fun isAvailable(context: Context): Boolean = true
    override fun create(context: Context): PttInputSource = MediaButtonInputSource(context)
}

@BindingContainer
@ContributesTo(AppScope::class)
interface MediaButtonInputSourceModule {
    @Binds
    @IntoMap
    @PttInputSourceKey(PttInputSourceId.MediaButton)
    fun bindMediaButtonInputSourceFactory(factory: MediaButtonInputSourceFactory): PttInputSourceFactory
}
