/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x.di

import android.content.Context
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.features.rageshake.reporter.BugReporter
import io.element.android.features.rageshake.reporter.DefaultBugReporter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.plus
import java.io.File
import java.util.concurrent.Executors

@Module
@ContributesTo(AppScope::class)
object AppModule {

    @Provides
    fun providesBaseDirectory(@ApplicationContext context: Context): File {
        return File(context.filesDir, "sessions")
    }

    @Provides
    @SingleIn(AppScope::class)
    fun providesAppCoroutineScope(): CoroutineScope {
        return MainScope() + CoroutineName("ElementX Scope")
    }

    @Provides
    @SingleIn(AppScope::class)
    fun providesCoroutineDispatchers(): CoroutineDispatchers {
        return CoroutineDispatchers(
            io = Dispatchers.IO,
            computation = Dispatchers.Default,
            main = Dispatchers.Main,
            diffUpdateDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        )
    }

    @Provides
    fun providesBugReporter(bugReporter: DefaultBugReporter): BugReporter = bugReporter
}
