/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.element.android.libraries.dateformatter.impl.TimezoneProvider
import kotlinx.datetime.TimeZone
import java.util.Locale
import kotlin.time.Clock

@BindingContainer
@ContributesTo(AppScope::class)
object DateFormatterModule {
    @Provides
    fun providesClock(): Clock = Clock.System

    @Provides
    fun providesLocale(): Locale = Locale.getDefault()

    @Provides
    fun providesTimezone(): TimezoneProvider = TimezoneProvider { TimeZone.currentSystemDefault() }
}
