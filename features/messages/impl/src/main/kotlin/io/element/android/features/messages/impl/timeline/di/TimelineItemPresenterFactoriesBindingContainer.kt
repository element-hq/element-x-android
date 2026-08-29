/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.libraries.di.RoomScope
import kotlin.reflect.KClass

/**
 * Container that declares the [TimelineItemPresenterFactory] map multi binding.
 *
 * Its sole purpose is to support the case of an empty map multibinding.
 */
@BindingContainer
@ContributesTo(RoomScope::class)
interface TimelineItemPresenterFactoriesBindingContainer {
    @Multibinds
    fun multiBindTimelineItemPresenterFactories(): @JvmSuppressWildcards Map<KClass<out TimelineItemEventContent>, TimelineItemPresenterFactory<*, *>>
}
