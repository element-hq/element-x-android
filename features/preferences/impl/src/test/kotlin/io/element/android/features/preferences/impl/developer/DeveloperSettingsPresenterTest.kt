/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.preferences.impl.developer

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.preferences.impl.developer.appsettings.anAppDeveloperSettingsState
import io.element.android.features.preferences.impl.tasks.FakeClearCacheUseCase
import io.element.android.features.preferences.impl.tasks.FakeComputeCacheSizeUseCase
import io.element.android.features.preferences.impl.tasks.VacuumStoresUseCase
import io.element.android.libraries.androidutils.filesize.FakeFileSizeFormatter
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.data.megaBytes
import io.element.android.libraries.matrix.api.analytics.GetDatabaseSizesUseCase
import io.element.android.libraries.matrix.api.analytics.SdkStoreSizes
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DeveloperSettingsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - ensures initial states are correct`() = runTest {
        val presenter = createDeveloperSettingsPresenter(
            databaseSizesUseCase = GetDatabaseSizesUseCase {
                Result.success(
                    SdkStoreSizes(stateStore = 10.megaBytes, eventCacheStore = 10.megaBytes, mediaStore = 10.megaBytes, cryptoStore = 10.megaBytes)
                )
            }
        )
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.appDeveloperSettingsState.features).isNotEmpty()
                assertThat(state.clearCacheAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(state.cacheSize).isEqualTo(AsyncData.Uninitialized)
                assertThat(state.isEnterpriseBuild).isFalse()
                assertThat(state.showColorPicker).isFalse()
            }
            awaitItem().also { state ->
                assertThat(state.cacheSize.isLoading()).isTrue()
            }
            awaitItem().also { state ->
                assertThat(state.cacheSize).isInstanceOf(AsyncData.Success::class.java)
                assertThat(state.databaseSizes.dataOrNull()).isEqualTo(
                    persistentMapOf(
                        "State store" to "10485760 Bytes",
                        "Event cache store" to "10485760 Bytes",
                        "Media store" to "10485760 Bytes",
                        "Crypto store" to "10485760 Bytes"
                    )
                )
            }
        }
    }

    @Test
    fun `present - clear cache`() = runTest {
        val clearCacheUseCase = FakeClearCacheUseCase()
        val presenter = createDeveloperSettingsPresenter(clearCacheUseCase = clearCacheUseCase)
        presenter.test {
            skipItems(2)
            assertThat(clearCacheUseCase.executeHasBeenCalled).isFalse()
            awaitItem().also { state ->
                state.eventSink(DeveloperSettingsEvents.ClearCache)
            }
            awaitItem().also { state ->
                assertThat(state.clearCacheAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.clearCacheAction).isInstanceOf(AsyncAction.Success::class.java)
                assertThat(clearCacheUseCase.executeHasBeenCalled).isTrue()
            }
            awaitItem().also { state ->
                assertThat(state.cacheSize).isInstanceOf(AsyncData.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.cacheSize).isInstanceOf(AsyncData.Success::class.java)
            }
        }
    }

    @Test
    fun `present - enterprise build can change the brand color`() = runTest {
        val overrideBrandColorResult = lambdaRecorder<SessionId?, String?, Unit> { _, _ -> }
        val presenter = createDeveloperSettingsPresenter(
            enterpriseService = FakeEnterpriseService(
                isEnterpriseBuild = true,
                overrideBrandColorResult = overrideBrandColorResult,
            )
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isEnterpriseBuild).isTrue()
            initialState.eventSink(DeveloperSettingsEvents.SetShowColorPicker(true))
            assertThat(awaitItem().showColorPicker).isTrue()
            initialState.eventSink(DeveloperSettingsEvents.SetShowColorPicker(false))
            assertThat(awaitItem().showColorPicker).isFalse()
            initialState.eventSink(DeveloperSettingsEvents.SetShowColorPicker(true))
            assertThat(awaitItem().showColorPicker).isTrue()
            initialState.eventSink(DeveloperSettingsEvents.ChangeBrandColor(Color.Green))
            assertThat(awaitItem().showColorPicker).isFalse()
            skipItems(1)
            overrideBrandColorResult.assertions().isCalledOnce()
                .with(value(A_SESSION_ID), value("#00FF00"))
        }
    }

    @Test
    fun `present - VacuumStores action invokes the VacuumStoresUseCase`() = runTest {
        var vacuumCalled = false
        val presenter = createDeveloperSettingsPresenter(
            vacuumStoresUseCase = VacuumStoresUseCase {
                vacuumCalled = true
            }
        )
        presenter.test {
            val state = awaitItem()
            assertThat(vacuumCalled).isFalse()
            state.eventSink(DeveloperSettingsEvents.VacuumStores)
            skipItems(1)
            assertThat(vacuumCalled).isTrue()
        }
    }

    private fun createDeveloperSettingsPresenter(
        sessionId: SessionId = A_SESSION_ID,
        cacheSizeUseCase: FakeComputeCacheSizeUseCase = FakeComputeCacheSizeUseCase(),
        clearCacheUseCase: FakeClearCacheUseCase = FakeClearCacheUseCase(),
        enterpriseService: EnterpriseService = FakeEnterpriseService(),
        vacuumStoresUseCase: VacuumStoresUseCase = VacuumStoresUseCase {},
        databaseSizesUseCase: GetDatabaseSizesUseCase = GetDatabaseSizesUseCase { Result.success(SdkStoreSizes(null, null, null, null)) },
    ): DeveloperSettingsPresenter {
        return DeveloperSettingsPresenter(
            appDeveloperSettingsPresenter = { anAppDeveloperSettingsState() },
            sessionId = sessionId,
            computeCacheSizeUseCase = cacheSizeUseCase,
            clearCacheUseCase = clearCacheUseCase,
            enterpriseService = enterpriseService,
            vacuumStoresUseCase = vacuumStoresUseCase,
            databaseSizesUseCase = databaseSizesUseCase,
            fileSizeFormatter = FakeFileSizeFormatter(),
        )
    }
}
