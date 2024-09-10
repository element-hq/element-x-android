/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.x.di

import com.squareup.anvil.annotations.ContributesTo
import io.element.android.features.api.MigrationEntryPoint
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.features.rageshake.api.reporter.BugReporter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.tracing.TracingService
import io.element.android.libraries.preferences.api.store.AppPreferencesStore

@ContributesTo(AppScope::class)
interface AppBindings {
    fun snackbarDispatcher(): SnackbarDispatcher

    fun tracingService(): TracingService

    fun bugReporter(): BugReporter

    fun lockScreenService(): LockScreenService

    fun preferencesStore(): AppPreferencesStore

    fun migrationEntryPoint(): MigrationEntryPoint

    fun lockScreenEntryPoint(): LockScreenEntryPoint
}
