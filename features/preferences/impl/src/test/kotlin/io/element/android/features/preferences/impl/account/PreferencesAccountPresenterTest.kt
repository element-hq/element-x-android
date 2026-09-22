/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.account

import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.api.SessionEnterpriseService
import io.element.android.features.enterprise.test.FakeSessionEnterpriseService
import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.features.rageshake.api.RageshakeFeatureAvailability
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.indicator.api.IndicatorService
import io.element.android.libraries.indicator.test.FakeIndicatorService
import io.element.android.libraries.matrix.api.oauth.AccountManagementAction
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PreferencesAccountPresenterTest {
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
        createPreferencesAccountPresenter(
            matrixClient = matrixClient,
            sessionEnterpriseService = FakeSessionEnterpriseService(
                tweakMasUrlResult = { "tweaked $it" },
            ),
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.myUser).isEqualTo(
                MatrixUser(
                    userId = matrixClient.sessionId,
                    displayName = A_USER_NAME,
                    avatarUrl = AN_AVATAR_URL
                )
            )
            assertThat(initialState.showSecureBackupBadge).isFalse()
            assertThat(initialState.accountManagementUrl).isNull()
            assertThat(initialState.showLinkNewDevice).isFalse()
            assertThat(initialState.nbOfBlockedUsers).isEqualTo(0)
            assertThat(initialState.showBlockedUsersItem).isFalse()
            assertThat(initialState.directLogoutState).isEqualTo(aDirectLogoutState())
            assertThat(initialState.snackbarMessage).isNull()
            val loadedState = consumeItemsUntilPredicate { it.accountManagementUrl != null }.last()
            assertThat(loadedState.showSecureBackup).isFalse()
            assertThat(loadedState.canDeactivateAccount).isTrue()
            assertThat(loadedState.canReportBug).isTrue()
            assertThat(loadedState.accountManagementUrl).isEqualTo("tweaked null url")
            accountManagementUrlResult.assertions().isCalledOnce()
                .with(value(null))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - cannot report bug`() = runTest {
        createPreferencesAccountPresenter(
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { true },
                accountManagementUrlResult = { Result.success("") },
            ),
            rageshakeFeatureAvailability = { flowOf(false) },
        ).test {
            assertThat(awaitItem().canReportBug).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - number of blocked users`() = runTest {
        createPreferencesAccountPresenter(
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { true },
                accountManagementUrlResult = { Result.success("") },
                ignoredUsersFlow = MutableStateFlow(persistentListOf(A_USER_ID, A_USER_ID_2)),
            ),
        ).test {
            val state = consumeItemsUntilPredicate { it.nbOfBlockedUsers == 2 }.last()
            assertThat(state.showBlockedUsersItem).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - secure backup badge`() = runTest {
        val indicatorService = FakeIndicatorService()
        createPreferencesAccountPresenter(
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { true },
                accountManagementUrlResult = { Result.success("") },
            ),
            indicatorService = indicatorService,
        ).test {
            assertThat(awaitItem().showSecureBackupBadge).isFalse()
            indicatorService.setShowSettingChatBackupIndicator(true)
            consumeItemsUntilPredicate { it.showSecureBackupBadge }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - secure backup is shown when the session does not need to be verified`() = runTest {
        val sessionVerificationService = FakeSessionVerificationService()
        createPreferencesAccountPresenter(
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { true },
                accountManagementUrlResult = { Result.success("") },
            ),
            sessionVerificationService = sessionVerificationService,
        ).test {
            // The session needs to be verified, so the secure backup entry is hidden.
            consumeItemsUntilPredicate { !it.showSecureBackup }
            sessionVerificationService.emitNeedsSessionVerification(false)
            consumeItemsUntilPredicate { it.showSecureBackup }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - can deactivate account is false if the Matrix client say so`() = runTest {
        createPreferencesAccountPresenter(
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { false },
                accountManagementUrlResult = { Result.success(null) },
            ),
        ).test {
            assertThat(awaitItem().canDeactivateAccount).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - link new device`() = runTest {
        createPreferencesAccountPresenter(
            matrixClient = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                canDeactivateAccountResult = { true },
                accountManagementUrlResult = { Result.success(null) },
            ),
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.QrCodeLogin.key to true)
            ),
        ).test {
            consumeItemsUntilPredicate { it.showLinkNewDevice }
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createPreferencesAccountPresenter(
        matrixClient: FakeMatrixClient = FakeMatrixClient(),
        sessionVerificationService: FakeSessionVerificationService = FakeSessionVerificationService(),
        rageshakeFeatureAvailability: RageshakeFeatureAvailability = RageshakeFeatureAvailability { flowOf(true) },
        indicatorService: IndicatorService = FakeIndicatorService(),
        featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
        sessionEnterpriseService: SessionEnterpriseService = FakeSessionEnterpriseService(),
    ) = PreferencesAccountPresenter(
        matrixClient = matrixClient,
        sessionVerificationService = sessionVerificationService,
        snackbarDispatcher = SnackbarDispatcher(),
        indicatorService = indicatorService,
        directLogoutPresenter = { aDirectLogoutState() },
        rageshakeFeatureAvailability = rageshakeFeatureAvailability,
        featureFlagService = featureFlagService,
        sessionEnterpriseService = sessionEnterpriseService,
    )
}
