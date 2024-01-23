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

import com.squareup.anvil.annotations.ContributesTo
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.features.preferences.api.store.PreferencesStore
import io.element.android.features.rageshake.api.reporter.BugReporter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.tracing.TracingService

@ContributesTo(AppScope::class)
interface AppBindings {
    fun snackbarDispatcher(): SnackbarDispatcher

    fun tracingService(): TracingService

    fun bugReporter(): BugReporter

    fun lockScreenService(): LockScreenService

    fun preferencesStore(): PreferencesStore
}
