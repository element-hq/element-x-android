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

package io.element.android.features.preferences.impl.root

import androidx.compose.runtime.Composable
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.logout.api.direct.DirectLogoutPresenter
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.indicator.impl.DefaultIndicatorService
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PreferencesRootPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val aDirectLogoutState = DirectLogoutState(
        canDoDirectSignOut = true,
        logoutAction = AsyncAction.Uninitialized,
        eventSink = {},
    )

    @Test
    fun `present - initial state`() = runTest {
        val matrixClient = FakeMatrixClient()
        val sessionVerificationService = FakeSessionVerificationService()
        val presenter = PreferencesRootPresenter(
            matrixClient = matrixClient,
            sessionVerificationService = sessionVerificationService,
            analyticsService = FakeAnalyticsService(),
            buildType = BuildType.DEBUG,
            versionFormatter = FakeVersionFormatter(),
            snackbarDispatcher = SnackbarDispatcher(),
            featureFlagService = FakeFeatureFlagService(),
            indicatorService = DefaultIndicatorService(
                sessionVerificationService = sessionVerificationService,
                encryptionService = FakeEncryptionService(),
                featureFlagService = FakeFeatureFlagService(),
            ),
            directLogoutPresenter = object : DirectLogoutPresenter {
                @Composable
                override fun present() = aDirectLogoutState
            },
            sessionStore = InMemorySessionStore()
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.myUser).isNull()
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
            assertThat(loadedState.showCompleteVerification).isTrue()
            assertThat(loadedState.showSecureBackup).isFalse()
            assertThat(loadedState.showSecureBackupBadge).isTrue()
            assertThat(loadedState.accountManagementUrl).isNull()
            assertThat(loadedState.devicesManagementUrl).isNull()
            assertThat(loadedState.showAnalyticsSettings).isFalse()
            assertThat(loadedState.showDeveloperSettings).isTrue()
            assertThat(loadedState.showLockScreenSettings).isTrue()
            assertThat(loadedState.showNotificationSettings).isTrue()
            assertThat(loadedState.directLogoutState).isEqualTo(aDirectLogoutState)
            assertThat(loadedState.snackbarMessage).isNull()
        }
    }
}
