/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.features.rageshake.api.crash.CrashDetectionPresenter
import io.element.android.features.rageshake.api.crash.CrashDetectionState
import io.element.android.features.rageshake.api.detection.RageshakeDetectionPresenter
import io.element.android.features.rageshake.api.detection.RageshakeDetectionState
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesPresenter
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.AppScope

@ContributesTo(AppScope::class)
@Module
interface RageshakeModule {
    @Binds
    fun bindRageshakePreferencesPresenter(presenter: RageshakePreferencesPresenter): Presenter<RageshakePreferencesState>

    @Binds
    fun bindRageshakeDetectionPresenter(presenter: RageshakeDetectionPresenter): Presenter<RageshakeDetectionState>

    @Binds
    fun bindCrashDetectionPresenter(presenter: CrashDetectionPresenter): Presenter<CrashDetectionState>
}
