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

package io.element.android.features.preferences.impl.advanced

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.compound.theme.Theme
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.test.FakePushService
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.test.FakePushProvider
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.awaitLastSequentialItem
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AdvancedSettingsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.isDeveloperModeEnabled).isFalse()
            assertThat(initialState.showChangeThemeDialog).isFalse()
            assertThat(initialState.isSharePresenceEnabled).isTrue()
            assertThat(initialState.theme).isEqualTo(Theme.System)
        }
    }

    @Test
    fun `present - developer mode on off`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.isDeveloperModeEnabled).isFalse()
            initialState.eventSink.invoke(AdvancedSettingsEvents.SetDeveloperModeEnabled(true))
            assertThat(awaitItem().isDeveloperModeEnabled).isTrue()
            initialState.eventSink.invoke(AdvancedSettingsEvents.SetDeveloperModeEnabled(false))
            assertThat(awaitItem().isDeveloperModeEnabled).isFalse()
        }
    }

    @Test
    fun `present - share presence off on`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.isSharePresenceEnabled).isTrue()
            initialState.eventSink.invoke(AdvancedSettingsEvents.SetSharePresenceEnabled(false))
            assertThat(awaitItem().isSharePresenceEnabled).isFalse()
            initialState.eventSink.invoke(AdvancedSettingsEvents.SetSharePresenceEnabled(true))
            assertThat(awaitItem().isSharePresenceEnabled).isTrue()
        }
    }

    @Test
    fun `present - change theme`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            initialState.eventSink.invoke(AdvancedSettingsEvents.ChangeTheme)
            val withDialog = awaitItem()
            assertThat(withDialog.showChangeThemeDialog).isTrue()
            // Cancel
            withDialog.eventSink(AdvancedSettingsEvents.CancelChangeTheme)
            val withoutDialog = awaitItem()
            assertThat(withoutDialog.showChangeThemeDialog).isFalse()
            withDialog.eventSink.invoke(AdvancedSettingsEvents.ChangeTheme)
            assertThat(awaitItem().showChangeThemeDialog).isTrue()
            withDialog.eventSink(AdvancedSettingsEvents.SetTheme(Theme.Light))
            val withNewTheme = awaitItem()
            assertThat(withNewTheme.showChangeThemeDialog).isFalse()
            assertThat(withNewTheme.theme).isEqualTo(Theme.Light)
        }
    }

    @Test
    fun `present - change push provider`() = runTest {
        val presenter = createAdvancedSettingsPresenter(
            pushService = createFakePushService(),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.pushDistributor).isEqualTo(AsyncAction.Success("aDistributorName0"))
            assertThat(initialState.pushDistributors).containsExactly("aDistributorName0", "aDistributorName1")
            initialState.eventSink.invoke(AdvancedSettingsEvents.ChangePushProvider)
            val withDialog = awaitItem()
            assertThat(withDialog.showChangePushProviderDialog).isTrue()
            // Cancel
            withDialog.eventSink(AdvancedSettingsEvents.CancelChangePushProvider)
            val withoutDialog = awaitItem()
            assertThat(withoutDialog.showChangePushProviderDialog).isFalse()
            withDialog.eventSink.invoke(AdvancedSettingsEvents.ChangePushProvider)
            assertThat(awaitItem().showChangePushProviderDialog).isTrue()
            withDialog.eventSink(AdvancedSettingsEvents.SetPushProvider(1))
            val withNewProvider = awaitItem()
            assertThat(withNewProvider.showChangePushProviderDialog).isFalse()
            assertThat(withNewProvider.pushDistributor).isEqualTo(AsyncAction.Loading)
            val lastItem = awaitItem()
            assertThat(lastItem.pushDistributor).isEqualTo(AsyncAction.Success("aDistributorName1"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - change push provider error`() = runTest {
        val presenter = createAdvancedSettingsPresenter(
            pushService = createFakePushService(
                registerWithLambda = { _, _, _ ->
                    Result.failure(Exception("An error"))
                },
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            initialState.eventSink.invoke(AdvancedSettingsEvents.ChangePushProvider)
            val withDialog = awaitItem()
            assertThat(withDialog.showChangePushProviderDialog).isTrue()
            withDialog.eventSink(AdvancedSettingsEvents.SetPushProvider(1))
            val withNewProvider = awaitItem()
            assertThat(withNewProvider.showChangePushProviderDialog).isFalse()
            assertThat(withNewProvider.pushDistributor).isEqualTo(AsyncAction.Loading)
            val lastItem = awaitItem()
            assertThat(lastItem.pushDistributor).isInstanceOf(AsyncAction.Failure::class.java)
        }
    }

    private fun createFakePushService(
        registerWithLambda: suspend (MatrixClient, PushProvider, Distributor) -> Result<Unit> = { _, _, _ ->
            Result.success(Unit)
        }
    ): PushService {
        val pushProvider1 = FakePushProvider(
            index = 0,
            name = "aFakePushProvider0",
            isAvailable = true,
            distributors = listOf(Distributor("aDistributorValue0", "aDistributorName0")),
        )
        val pushProvider2 = FakePushProvider(
            index = 1,
            name = "aFakePushProvider1",
            isAvailable = true,
            distributors = listOf(Distributor("aDistributorValue1", "aDistributorName1")),
        )
        return FakePushService(
            availablePushProviders = listOf(pushProvider1, pushProvider2),
            registerWithLambda = registerWithLambda,
        )
    }

    private fun createAdvancedSettingsPresenter(
        appPreferencesStore: InMemoryAppPreferencesStore = InMemoryAppPreferencesStore(),
        sessionPreferencesStore: InMemorySessionPreferencesStore = InMemorySessionPreferencesStore(),
        matrixClient: MatrixClient = FakeMatrixClient(),
        pushService: PushService = FakePushService(),
    ) = AdvancedSettingsPresenter(
        appPreferencesStore = appPreferencesStore,
        sessionPreferencesStore = sessionPreferencesStore,
        matrixClient = matrixClient,
        pushService = pushService,
    )
}
