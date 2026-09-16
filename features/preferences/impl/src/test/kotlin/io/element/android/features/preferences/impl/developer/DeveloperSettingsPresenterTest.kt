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
import io.element.android.features.preferences.impl.tasks.FakeMarkAllRoomsAsRead
import io.element.android.features.preferences.impl.tasks.VacuumStoresUseCase
import io.element.android.libraries.androidutils.filesize.FakeFileSizeFormatter
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.data.megaBytes
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.analytics.GetDatabaseSizesUseCase
import io.element.android.libraries.matrix.api.analytics.SdkStoreSizes
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.test.A_DEVICE_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private const val A_PUSH_RULES_CONTENT = """{"global":{"override":[{"rule_id":".m.rule.master","enabled":false}]}}"""
private const val A_PUSH_RULES_CONTENT_PRETTY_PRINTED = """{
  "global": {
    "override": [
      {
        "rule_id": ".m.rule.master",
        "enabled": false
      }
    ]
  }
}"""
private val AN_EXCEPTION = Exception("A failure")

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
                assertThat(state.deviceId).isEqualTo(A_DEVICE_ID)
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
                state.eventSink(DeveloperSettingsEvent.ClearCache)
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
            enterpriseService = FakeEnterpriseService(overrideBrandColorResult = overrideBrandColorResult),
            buildMeta = aBuildMeta(isEnterpriseBuild = true),
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isEnterpriseBuild).isTrue()
            initialState.eventSink(DeveloperSettingsEvent.SetShowColorPicker(true))
            assertThat(awaitItem().showColorPicker).isTrue()
            initialState.eventSink(DeveloperSettingsEvent.SetShowColorPicker(false))
            assertThat(awaitItem().showColorPicker).isFalse()
            initialState.eventSink(DeveloperSettingsEvent.SetShowColorPicker(true))
            assertThat(awaitItem().showColorPicker).isTrue()
            initialState.eventSink(DeveloperSettingsEvent.ChangeBrandColor(Color.Green))
            assertThat(awaitItem().showColorPicker).isFalse()
            skipItems(1)
            overrideBrandColorResult.assertions().isCalledOnce()
                .with(value(A_SESSION_ID), value("#00FF00"))
        }
    }

    @Test
    fun `present - confirm mark all rooms as read`() = runTest {
        val markAllRoomsAsRead = FakeMarkAllRoomsAsRead()
        val presenter = createDeveloperSettingsPresenter(markAllRoomsAsRead = markAllRoomsAsRead)
        presenter.test {
            skipItems(2)
            val initialState = awaitItem()
            initialState.eventSink(DeveloperSettingsEvent.MarkAllRoomsAsRead(needsConfirmation = true))
            val stateWithConfirmation = awaitItem()
            assertThat(stateWithConfirmation.markAllRoomsAsReadAction.isConfirming()).isTrue()
            stateWithConfirmation.eventSink(DeveloperSettingsEvent.MarkAllRoomsAsRead(needsConfirmation = false))
            awaitItem().also { state ->
                assertThat(state.markAllRoomsAsReadAction.isConfirming()).isFalse()
                assertThat(state.markAllRoomsAsReadAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.markAllRoomsAsReadAction).isInstanceOf(AsyncAction.Success::class.java)
                assertThat(markAllRoomsAsRead.invokeCallCount).isEqualTo(1)
            }
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
            state.eventSink(DeveloperSettingsEvent.VacuumStores)
            skipItems(1)
            assertThat(vacuumCalled).isTrue()
        }
    }

    @Test
    fun `present - OpenPushRules event navigates to the push rules content`() = runTest {
        val openPushRulesLambda = lambdaRecorder<String, String, Unit> { _, _ -> }
        val presenter = createDeveloperSettingsPresenter(
            navigator = DeveloperSettingsNavigator(openPushRulesLambda),
            notificationSettingsService = FakeNotificationSettingsService(
                getRawPushRulesResult = { Result.success(A_PUSH_RULES_CONTENT) },
            ),
        )
        presenter.test {
            val state = awaitItem()
            assertThat(state.pushRulesAction).isEqualTo(AsyncAction.Uninitialized)
            state.eventSink(DeveloperSettingsEvent.OpenPushRules)
            skipItems(1)
            awaitItem().also {
                assertThat(it.pushRulesAction).isEqualTo(AsyncAction.Uninitialized)
            }
            openPushRulesLambda.assertions().isCalledOnce().with(
                value("push_rules@alice_server.org.json"),
                value(A_PUSH_RULES_CONTENT_PRETTY_PRINTED),
            )
        }
    }

    @Test
    fun `present - OpenPushRules event keeps the content as is when it is not valid json`() = runTest {
        val openPushRulesLambda = lambdaRecorder<String, String, Unit> { _, _ -> }
        val presenter = createDeveloperSettingsPresenter(
            navigator = DeveloperSettingsNavigator(openPushRulesLambda),
            notificationSettingsService = FakeNotificationSettingsService(
                getRawPushRulesResult = { Result.success("not json") },
            ),
        )
        presenter.test {
            awaitItem().eventSink(DeveloperSettingsEvent.OpenPushRules)
            skipItems(2)
            openPushRulesLambda.assertions().isCalledOnce().with(
                value("push_rules@alice_server.org.json"),
                value("not json"),
            )
        }
    }

    @Test
    fun `present - OpenPushRules event failure can be dismissed`() = runTest {
        val presenter = createDeveloperSettingsPresenter(
            notificationSettingsService = FakeNotificationSettingsService(
                getRawPushRulesResult = { Result.failure(AN_EXCEPTION) },
            ),
        )
        presenter.test {
            val state = awaitItem()
            state.eventSink(DeveloperSettingsEvent.OpenPushRules)
            skipItems(1)
            awaitItem().also {
                assertThat(it.pushRulesAction).isEqualTo(AsyncAction.Failure(AN_EXCEPTION))
                it.eventSink(DeveloperSettingsEvent.DismissPushRulesError)
            }
            awaitItem().also {
                assertThat(it.pushRulesAction).isEqualTo(AsyncAction.Uninitialized)
            }
        }
    }

    private fun createDeveloperSettingsPresenter(
        navigator: DeveloperSettingsNavigator = DeveloperSettingsNavigator { _, _ -> lambdaError() },
        sessionId: SessionId = A_SESSION_ID,
        deviceId: DeviceId = A_DEVICE_ID,
        cacheSizeUseCase: FakeComputeCacheSizeUseCase = FakeComputeCacheSizeUseCase(),
        clearCacheUseCase: FakeClearCacheUseCase = FakeClearCacheUseCase(),
        enterpriseService: EnterpriseService = FakeEnterpriseService(),
        vacuumStoresUseCase: VacuumStoresUseCase = VacuumStoresUseCase {},
        databaseSizesUseCase: GetDatabaseSizesUseCase = GetDatabaseSizesUseCase { Result.success(SdkStoreSizes(null, null, null, null)) },
        markAllRoomsAsRead: FakeMarkAllRoomsAsRead = FakeMarkAllRoomsAsRead(),
        buildMeta: BuildMeta = aBuildMeta(),
        notificationSettingsService: NotificationSettingsService = FakeNotificationSettingsService(),
    ): DeveloperSettingsPresenter {
        return DeveloperSettingsPresenter(
            navigator = navigator,
            appDeveloperSettingsPresenter = { anAppDeveloperSettingsState() },
            sessionId = sessionId,
            deviceId = deviceId,
            computeCacheSizeUseCase = cacheSizeUseCase,
            clearCacheUseCase = clearCacheUseCase,
            enterpriseService = enterpriseService,
            vacuumStoresUseCase = vacuumStoresUseCase,
            databaseSizesUseCase = databaseSizesUseCase,
            fileSizeFormatter = FakeFileSizeFormatter(),
            markAllRoomsAsRead = markAllRoomsAsRead,
            buildMeta = buildMeta,
            notificationSettingsService = notificationSettingsService,
        )
    }
}
