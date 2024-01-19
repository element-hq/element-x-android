/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.dateformatter.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.di.AppScope
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import java.util.Locale

@Module
@ContributesTo(AppScope::class)
object DateFormatterModule {
    @Provides
    fun providesClock(): Clock = Clock.System

    @Provides
    fun providesLocale(): Locale = Locale.getDefault()

    @Provides
    fun providesTimezone(): TimeZone = TimeZone.currentSystemDefault()
}
