/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.di

import dagger.MapKey
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import kotlin.reflect.KClass

/**
 * Annotation to add a factory of type [TimelineItemPresenterFactory] to a
 * Dagger map multi binding keyed with a subclass of [TimelineItemEventContent].
 */
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class TimelineItemEventContentKey(val value: KClass<out TimelineItemEventContent>)
