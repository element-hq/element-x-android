/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.enterprise.api.SessionEnterpriseService
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.rageshake.api.RageshakeFeatureAvailability
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.indicator.api.IndicatorService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Inject
class PreferencesAccountPresenter(
    private val matrixClient: MatrixClient,
    private val sessionVerificationService: SessionVerificationService,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val indicatorService: IndicatorService,
    private val directLogoutPresenter: Presenter<DirectLogoutState>,
    private val rageshakeFeatureAvailability: RageshakeFeatureAvailability,
    private val featureFlagService: FeatureFlagService,
    private val sessionEnterpriseService: SessionEnterpriseService,
) : Presenter<PreferencesAccountState> {
    @Composable
    override fun present(): PreferencesAccountState {
        val matrixUser = matrixClient.userProfile.collectAsState()
        LaunchedEffect(Unit) {
            // Force a refresh of the profile
            matrixClient.getUserProfile()
        }
        val showLinkNewDevice by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.QrCodeLogin)
        }.collectAsState(initial = false)

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()

        // We should display the 'complete verification' option if the current session can be verified
        val canVerifyUserSession by sessionVerificationService.needsSessionVerification.collectAsState(false)

        val showSecureBackupIndicator by indicatorService.showSettingChatBackupIndicator()

        val accountManagementUrl: MutableState<String?> = remember {
            mutableStateOf(null)
        }
        var canDeactivateAccount by remember {
            mutableStateOf(false)
        }
        val canReportBug by remember { rageshakeFeatureAvailability.isAvailable() }.collectAsState(false)
        LaunchedEffect(Unit) {
            canDeactivateAccount = matrixClient.canDeactivateAccount()
        }

        val numberOfBlockedUsers by produceState(initialValue = 0) {
            matrixClient.ignoredUsersFlow
                .onEach { value = it.size }
                .launchIn(this)
        }

        val directLogoutState = directLogoutPresenter.present()

        LaunchedEffect(Unit) {
            initAccountManagementUrl(accountManagementUrl)
        }

        return PreferencesAccountState(
            myUser = matrixUser.value,
            showSecureBackup = !canVerifyUserSession,
            showSecureBackupBadge = showSecureBackupIndicator,
            accountManagementUrl = accountManagementUrl.value,
            canReportBug = canReportBug,
            showLinkNewDevice = showLinkNewDevice,
            canDeactivateAccount = canDeactivateAccount,
            numberOfBlockedUsers = numberOfBlockedUsers,
            directLogoutState = directLogoutState,
            snackbarMessage = snackbarMessage,
        )
    }

    private fun CoroutineScope.initAccountManagementUrl(
        accountManagementUrl: MutableState<String?>,
    ) = launch {
        accountManagementUrl.value = matrixClient.getAccountManagementUrl(null)
            .getOrNull()
            ?.let {
                sessionEnterpriseService.tweakMasUrl(it)
            }
    }
}
