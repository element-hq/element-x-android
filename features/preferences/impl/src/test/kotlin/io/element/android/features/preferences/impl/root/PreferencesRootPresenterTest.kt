/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.features.preferences.impl.utils.ShowDeveloperSettingsProvider
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.indicator.impl.DefaultIndicatorService
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PreferencesRootPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val matrixClient = FakeMatrixClient(canDeactivateAccountResult = { true })
        val presenter = createPresenter(matrixClient = matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.myUser).isEqualTo(
                MatrixUser(
                    userId = matrixClient.sessionId,
                    displayName = A_USER_NAME,
                    avatarUrl = AN_AVATAR_URL
                )
            )
            assertThat(initialState.version).isEqualTo("A Version")
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
            assertThat(loadedState.showSecureBackupBadge).isTrue()
            assertThat(loadedState.accountManagementUrl).isNull()
            assertThat(loadedState.devicesManagementUrl).isNull()
            assertThat(loadedState.showAnalyticsSettings).isFalse()
            assertThat(loadedState.showDeveloperSettings).isTrue()
            assertThat(loadedState.showLockScreenSettings).isTrue()
            assertThat(loadedState.showNotificationSettings).isTrue()
            assertThat(loadedState.canDeactivateAccount).isTrue()
            assertThat(loadedState.directLogoutState).isEqualTo(aDirectLogoutState())
            assertThat(loadedState.snackbarMessage).isNull()
        }
    }

    @Test
    fun `present - can deactivate account is false if the Matrix client say so`() = runTest {
        val presenter = createPresenter(
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { false }
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val loadedState = awaitFirstItem()
            assertThat(loadedState.canDeactivateAccount).isFalse()
        }
    }

    @Test
    fun `present - developer settings is hidden by default in release builds`() = runTest {
        val presenter = createPresenter(
            showDeveloperSettingsProvider = ShowDeveloperSettingsProvider(aBuildMeta(BuildType.RELEASE))
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val loadedState = awaitFirstItem()
            assertThat(loadedState.showDeveloperSettings).isFalse()
        }
    }

    @Test
    fun `present - developer settings can be enabled in release builds`() = runTest {
        val presenter = createPresenter(
            showDeveloperSettingsProvider = ShowDeveloperSettingsProvider(aBuildMeta(BuildType.RELEASE))
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val loadedState = awaitFirstItem()
            repeat(times = ShowDeveloperSettingsProvider.DEVELOPER_SETTINGS_COUNTER) {
                assertThat(loadedState.showDeveloperSettings).isFalse()
                loadedState.eventSink(PreferencesRootEvents.OnVersionInfoClick)
            }
            assertThat(awaitItem().showDeveloperSettings).isTrue()
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        skipItems(1)
        return awaitItem()
    }

    private fun createPresenter(
        matrixClient: FakeMatrixClient = FakeMatrixClient(canDeactivateAccountResult = { true }),
        sessionVerificationService: FakeSessionVerificationService = FakeSessionVerificationService(),
        showDeveloperSettingsProvider: ShowDeveloperSettingsProvider = ShowDeveloperSettingsProvider(aBuildMeta(BuildType.DEBUG)),
    ) = PreferencesRootPresenter(
        matrixClient = matrixClient,
        sessionVerificationService = sessionVerificationService,
        analyticsService = FakeAnalyticsService(),
        versionFormatter = FakeVersionFormatter(),
        snackbarDispatcher = SnackbarDispatcher(),
        featureFlagService = FakeFeatureFlagService(),
        indicatorService = DefaultIndicatorService(
            sessionVerificationService = sessionVerificationService,
            encryptionService = FakeEncryptionService(),
        ),
        directLogoutPresenter = { aDirectLogoutState() },
        showDeveloperSettingsProvider = showDeveloperSettingsProvider,
    )
}
