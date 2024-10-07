/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.preferences.impl.utils.ShowDeveloperSettingsProvider
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.indicator.api.IndicatorService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.oidc.AccountManagementAction
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class PreferencesRootPresenter @Inject constructor(
    private val matrixClient: MatrixClient,
    private val sessionVerificationService: SessionVerificationService,
    private val analyticsService: AnalyticsService,
    private val versionFormatter: VersionFormatter,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val featureFlagService: FeatureFlagService,
    private val indicatorService: IndicatorService,
    private val directLogoutPresenter: Presenter<DirectLogoutState>,
    private val showDeveloperSettingsProvider: ShowDeveloperSettingsProvider,
) : Presenter<PreferencesRootState> {
    @Composable
    override fun present(): PreferencesRootState {
        val matrixUser = matrixClient.userProfile.collectAsState()
        LaunchedEffect(Unit) {
            // Force a refresh of the profile
            matrixClient.getUserProfile()
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
        val hasAnalyticsProviders = remember { analyticsService.getAvailableAnalyticsProviders().isNotEmpty() }

        val showNotificationSettings = remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            showNotificationSettings.value = featureFlagService.isFeatureEnabled(FeatureFlags.NotificationSettings)
        }
        val showLockScreenSettings = remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            showLockScreenSettings.value = featureFlagService.isFeatureEnabled(FeatureFlags.PinUnlock)
        }

        // We should display the 'complete verification' option if the current session can be verified
        val canVerifyUserSession by sessionVerificationService.needsSessionVerification.collectAsState(false)

        val showSecureBackupIndicator by indicatorService.showSettingChatBackupIndicator()

        val accountManagementUrl: MutableState<String?> = remember {
            mutableStateOf(null)
        }
        val devicesManagementUrl: MutableState<String?> = remember {
            mutableStateOf(null)
        }
        var canDeactivateAccount by remember {
            mutableStateOf(false)
        }
        LaunchedEffect(Unit) {
            canDeactivateAccount = matrixClient.canDeactivateAccount()
        }

        val showBlockedUsersItem by produceState(initialValue = false) {
            matrixClient.ignoredUsersFlow
                .onEach { value = it.isNotEmpty() }
                .launchIn(this)
        }

        val directLogoutState = directLogoutPresenter.present()

        LaunchedEffect(Unit) {
            initAccountManagementUrl(accountManagementUrl, devicesManagementUrl)
        }

        val showDeveloperSettings by showDeveloperSettingsProvider.showDeveloperSettings.collectAsState()

        fun handleEvent(event: PreferencesRootEvents) {
            when (event) {
                is PreferencesRootEvents.OnVersionInfoClick -> {
                    showDeveloperSettingsProvider.unlockDeveloperSettings()
                }
            }
        }

        return PreferencesRootState(
            myUser = matrixUser.value,
            version = versionFormatter.get(),
            deviceId = matrixClient.deviceId,
            showSecureBackup = !canVerifyUserSession,
            showSecureBackupBadge = showSecureBackupIndicator,
            accountManagementUrl = accountManagementUrl.value,
            devicesManagementUrl = devicesManagementUrl.value,
            showAnalyticsSettings = hasAnalyticsProviders,
            showDeveloperSettings = showDeveloperSettings,
            canDeactivateAccount = canDeactivateAccount,
            showNotificationSettings = showNotificationSettings.value,
            showLockScreenSettings = showLockScreenSettings.value,
            showBlockedUsersItem = showBlockedUsersItem,
            directLogoutState = directLogoutState,
            snackbarMessage = snackbarMessage,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.initAccountManagementUrl(
        accountManagementUrl: MutableState<String?>,
        devicesManagementUrl: MutableState<String?>,
    ) = launch {
        accountManagementUrl.value = matrixClient.getAccountManagementUrl(AccountManagementAction.Profile).getOrNull()
        devicesManagementUrl.value = matrixClient.getAccountManagementUrl(AccountManagementAction.SessionsList).getOrNull()
    }
}
