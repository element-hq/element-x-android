/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.preferences.impl.root

import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.features.preferences.impl.utils.ShowDeveloperSettingsProvider
import io.element.android.features.rageshake.api.RageshakeFeatureAvailability
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeature
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.indicator.api.IndicatorService
import io.element.android.libraries.indicator.test.FakeIndicatorService
import io.element.android.libraries.matrix.api.oidc.AccountManagementAction
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PreferencesRootPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val accountManagementUrlResult = lambdaRecorder<AccountManagementAction?, Result<String?>> { action ->
            Result.success("$action url")
        }
        val matrixClient = FakeMatrixClient(
            canDeactivateAccountResult = { true },
            accountManagementUrlResult = accountManagementUrlResult,
        )
        createPresenter(
            matrixClient = matrixClient,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.myUser).isEqualTo(
                MatrixUser(
                    userId = matrixClient.sessionId,
                    displayName = A_USER_NAME,
                    avatarUrl = AN_AVATAR_URL
                )
            )
            assertThat(initialState.version).isEqualTo("A Version")
            assertThat(initialState.isMultiAccountEnabled).isFalse()
            assertThat(initialState.otherSessions).isEmpty()
            val loadedState = awaitItem()
            assertThat(loadedState.myUser).isEqualTo(
                MatrixUser(
                    userId = matrixClient.sessionId,
                    displayName = A_USER_NAME,
                    avatarUrl = AN_AVATAR_URL
                )
            )
            assertThat(initialState.version).isEqualTo("A Version")
            assertThat(loadedState.showSecureBackup).isFalse()
            assertThat(loadedState.showSecureBackupBadge).isFalse()
            assertThat(loadedState.accountManagementUrl).isNull()
            assertThat(loadedState.devicesManagementUrl).isNull()
            assertThat(loadedState.showAnalyticsSettings).isFalse()
            assertThat(loadedState.showDeveloperSettings).isTrue()
            assertThat(loadedState.canDeactivateAccount).isTrue()
            assertThat(loadedState.canReportBug).isTrue()
            assertThat(loadedState.directLogoutState).isEqualTo(aDirectLogoutState())
            assertThat(loadedState.snackbarMessage).isNull()
            skipItems(1)
            val finalState = awaitItem()
            accountManagementUrlResult.assertions().isCalledExactly(2)
                .withSequence(
                    listOf(value(AccountManagementAction.Profile)),
                    listOf(value(AccountManagementAction.SessionsList)),
                )
            assertThat(finalState.accountManagementUrl).isEqualTo("Profile url")
            assertThat(finalState.devicesManagementUrl).isEqualTo("SessionsList url")
        }
    }

    @Test
    fun `present - cannot report bug`() = runTest {
        val matrixClient = FakeMatrixClient(
            canDeactivateAccountResult = { true },
            accountManagementUrlResult = { Result.success("") },
        )
        createPresenter(
            matrixClient = matrixClient,
            rageshakeFeatureAvailability = { flowOf(false) },
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.canReportBug).isFalse()
            skipItems(1)
        }
    }

    @Test
    fun `present - secure backup badge`() = runTest {
        val matrixClient = FakeMatrixClient(
            canDeactivateAccountResult = { true },
            accountManagementUrlResult = { Result.success("") },
        )
        val indicatorService = FakeIndicatorService()
        createPresenter(
            matrixClient = matrixClient,
            rageshakeFeatureAvailability = { flowOf(false) },
            indicatorService = indicatorService,
        ).test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.showSecureBackupBadge).isFalse()
            indicatorService.setShowSettingChatBackupIndicator(true)
            val finalState = awaitItem()
            assertThat(finalState.showSecureBackupBadge).isTrue()
        }
    }

    @Test
    fun `present - can deactivate account is false if the Matrix client say so`() = runTest {
        createPresenter(
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { false },
                accountManagementUrlResult = { Result.success(null) },
            ),
        ).test {
            val loadedState = awaitFirstItem()
            assertThat(loadedState.canDeactivateAccount).isFalse()
        }
    }

    @Test
    fun `present - developer settings is hidden by default in release builds`() = runTest {
        createPresenter(
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { true },
                accountManagementUrlResult = { Result.success(null) },
            ),
            showDeveloperSettingsProvider = ShowDeveloperSettingsProvider(aBuildMeta(BuildType.RELEASE))
        ).test {
            val loadedState = awaitFirstItem()
            assertThat(loadedState.showDeveloperSettings).isFalse()
        }
    }

    @Test
    fun `present - developer settings can be enabled in release builds`() = runTest {
        createPresenter(
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { true },
                accountManagementUrlResult = { Result.success(null) },
            ),
            showDeveloperSettingsProvider = ShowDeveloperSettingsProvider(aBuildMeta(BuildType.RELEASE))
        ).test {
            val loadedState = awaitFirstItem()
            repeat(times = ShowDeveloperSettingsProvider.DEVELOPER_SETTINGS_COUNTER) {
                assertThat(loadedState.showDeveloperSettings).isFalse()
                loadedState.eventSink(PreferencesRootEvents.OnVersionInfoClick)
            }
            assertThat(awaitItem().showDeveloperSettings).isTrue()
        }
    }

    @Test
    fun `present - labs can be shown if any feature flag is in labs and not finished`() = runTest {
        createPresenter(
            featureFlagService = FakeFeatureFlagService(
                getAvailableFeaturesResult = { _, _ ->
                    listOf(
                        FakeFeature(
                            key = "feature_1",
                            title = "Feature 1",
                            isInLabs = true,
                            isFinished = false,
                        )
                    )
                }
            ),
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { true },
                accountManagementUrlResult = { Result.success(null) },
            ),
        ).test {
            assertThat(awaitItem().showLabsItem).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - labs can't be shown if all feature flags in labs are finished`() = runTest {
        createPresenter(
            featureFlagService = FakeFeatureFlagService(
                getAvailableFeaturesResult = { _, _ ->
                    emptyList()
                }
            ),
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { true },
                accountManagementUrlResult = { Result.success(null) },
            ),
        ).test {
            skipItems(1)
            assertThat(awaitItem().showLabsItem).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - multiple accounts`() = runTest {
        createPresenter(
            matrixClient = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                canDeactivateAccountResult = { true },
            ),
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MultiAccount.key to true)
            ),
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = A_SESSION_ID.value),
                    aSessionData(
                        sessionId = A_SESSION_ID_2.value,
                        userDisplayName = "Bob",
                        userAvatarUrl = "avatarUrl",
                    ),
                )
            )
        ).test {
            val state = awaitFirstItem()
            assertThat(state.isMultiAccountEnabled).isTrue()
            assertThat(state.otherSessions).hasSize(1)
            assertThat(state.otherSessions[0]).isEqualTo(MatrixUser(userId = A_SESSION_ID_2, displayName = "Bob", avatarUrl = "avatarUrl"))
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        skipItems(1)
        return awaitItem()
    }

    private fun createPresenter(
        matrixClient: FakeMatrixClient = FakeMatrixClient(),
        sessionVerificationService: FakeSessionVerificationService = FakeSessionVerificationService(),
        showDeveloperSettingsProvider: ShowDeveloperSettingsProvider = ShowDeveloperSettingsProvider(aBuildMeta(BuildType.DEBUG)),
        rageshakeFeatureAvailability: RageshakeFeatureAvailability = RageshakeFeatureAvailability { flowOf(true) },
        indicatorService: IndicatorService = FakeIndicatorService(),
        featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
        sessionStore: SessionStore = InMemorySessionStore(),
    ) = PreferencesRootPresenter(
        matrixClient = matrixClient,
        sessionVerificationService = sessionVerificationService,
        analyticsService = FakeAnalyticsService(),
        versionFormatter = FakeVersionFormatter(),
        snackbarDispatcher = SnackbarDispatcher(),
        indicatorService = indicatorService,
        directLogoutPresenter = { aDirectLogoutState() },
        showDeveloperSettingsProvider = showDeveloperSettingsProvider,
        rageshakeFeatureAvailability = rageshakeFeatureAvailability,
        featureFlagService = featureFlagService,
        sessionStore = sessionStore,
    )
}
