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
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.appconfig.ApplicationConfig
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.messages.impl.timeline.components.customreaction.DefaultEmojibaseProvider
import io.element.android.features.messages.impl.timeline.components.customreaction.EmojibaseProvider
import io.element.android.libraries.androidutils.system.getVersionCodeFromManifest
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.CacheDirectory
import io.element.android.libraries.di.SingleIn
import io.element.android.x.BuildConfig
import io.element.android.x.R
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus
import java.io.File

@Module
@ContributesTo(AppScope::class)
object AppModule {
    @Provides
    fun providesBaseDirectory(@ApplicationContext context: Context): File {
        return File(context.filesDir, "sessions")
    }

    @Provides
    @CacheDirectory
    fun providesCacheDirectory(@ApplicationContext context: Context): File {
        return context.cacheDir
    }

    @Provides
    fun providesResources(@ApplicationContext context: Context): Resources {
        return context.resources
    }

    @Provides
    @SingleIn(AppScope::class)
    fun providesAppCoroutineScope(): CoroutineScope {
        return MainScope() + CoroutineName("ElementX Scope")
    }

    @Provides
    @SingleIn(AppScope::class)
    fun providesBuildType(): BuildType {
        return BuildType.valueOf(BuildConfig.BUILD_TYPE.uppercase())
    }

    @Provides
    @SingleIn(AppScope::class)
    fun providesBuildMeta(
        @ApplicationContext context: Context,
        buildType: BuildType,
        enterpriseService: EnterpriseService,
    ): BuildMeta = BuildMeta(
        isDebuggable = BuildConfig.DEBUG,
        buildType = buildType,
        applicationName = ApplicationConfig.APPLICATION_NAME.takeIf { it.isNotEmpty() } ?: context.getString(R.string.app_name),
        productionApplicationName = ApplicationConfig.PRODUCTION_APPLICATION_NAME,
        desktopApplicationName = ApplicationConfig.DESKTOP_APPLICATION_NAME,
        applicationId = BuildConfig.APPLICATION_ID,
        isEnterpriseBuild = enterpriseService.isEnterpriseBuild,
        // TODO EAx Config.LOW_PRIVACY_LOG_ENABLE,
        lowPrivacyLoggingEnabled = false,
        versionName = BuildConfig.VERSION_NAME,
        versionCode = context.getVersionCodeFromManifest(),
        gitRevision = BuildConfig.GIT_REVISION,
        gitBranchName = BuildConfig.GIT_BRANCH_NAME,
        flavorDescription = BuildConfig.FLAVOR_DESCRIPTION,
        flavorShortDescription = BuildConfig.SHORT_FLAVOR_DESCRIPTION,
    )

    @Provides
    @SingleIn(AppScope::class)
    fun providesSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @SingleIn(AppScope::class)
    fun providesCoroutineDispatchers(): CoroutineDispatchers {
        return CoroutineDispatchers(
            io = Dispatchers.IO,
            computation = Dispatchers.Default,
            main = Dispatchers.Main,
        )
    }

    @Provides
    @SingleIn(AppScope::class)
    fun provideSnackbarDispatcher(): SnackbarDispatcher {
        return SnackbarDispatcher()
    }

    @Provides
    @SingleIn(AppScope::class)
    fun providesEmojibaseProvider(@ApplicationContext context: Context): EmojibaseProvider {
        return DefaultEmojibaseProvider(context)
    }
}
