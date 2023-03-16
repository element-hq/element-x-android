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

package io.element.android.x.di

import android.content.Context
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.x.BuildConfig
import io.element.android.x.R
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
    fun providesBuildMeta(@ApplicationContext context: Context) = BuildMeta(
        isDebug = BuildConfig.DEBUG,
        applicationName = context.getString(R.string.app_name),
        applicationId = BuildConfig.APPLICATION_ID,
        lowPrivacyLoggingEnabled = false, // TODO EAx Config.LOW_PRIVACY_LOG_ENABLE,
        versionName = BuildConfig.VERSION_NAME,
        gitRevision = "TODO", // BuildConfig.GIT_REVISION,
        gitRevisionDate = "TODO", //  BuildConfig.GIT_REVISION_DATE,
        gitBranchName = "TODO", //  BuildConfig.GIT_BRANCH_NAME,
        flavorDescription = "TODO", //  BuildConfig.FLAVOR_DESCRIPTION,
        flavorShortDescription = "TODO", //  BuildConfig.SHORT_FLAVOR_DESCRIPTION,
    )

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
}
