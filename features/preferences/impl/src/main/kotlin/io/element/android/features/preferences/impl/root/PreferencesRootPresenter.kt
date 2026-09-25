/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import io.element.android.compound.theme.Theme
import io.element.android.compound.theme.mapToTheme
import io.element.android.features.preferences.impl.account.PreferencesAccountState
import io.element.android.features.preferences.impl.userstatus.UserStatusState
import io.element.android.features.preferences.impl.utils.ShowDeveloperSettingsProvider
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Inject
class PreferencesRootPresenter(
    private val matrixClient: MatrixClient,
    private val analyticsService: AnalyticsService,
    private val versionFormatter: VersionFormatter,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val showDeveloperSettingsProvider: ShowDeveloperSettingsProvider,
    private val featureFlagService: FeatureFlagService,
    private val sessionStore: SessionStore,
    private val appPreferencesStore: AppPreferencesStore,
    private val userStatusPresenter: Presenter<UserStatusState>,
    private val preferencesAccountPresenter: Presenter<PreferencesAccountState>,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : Presenter<PreferencesRootState> {
    @Composable
    override fun present(): PreferencesRootState {
        val coroutineScope = rememberCoroutineScope()
        val matrixUser = matrixClient.userProfile.collectAsState()
        LaunchedEffect(Unit) {
            // Force a refresh of the profile
            matrixClient.getUserProfile()
        }
        val preferencesAccountState = preferencesAccountPresenter.present()
        val isMultiAccountEnabled by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.MultiAccount)
        }.collectAsState(initial = false)
        val isOtherAccountsSectionExpanded by remember {
            appPreferencesStore.isOtherAccountsExpandedFlow()
        }.collectAsState(initial = true)
        val isUserStatusSupported by produceState(false) {
            value = matrixClient.isUserStatusSupported().getOrDefault(false)
        }
        val userStatusState = if (isUserStatusSupported) userStatusPresenter.present() else null
        val isBlackThemeAllowed by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.AllowBlackTheme)
        }.collectAsState(initial = false)
        val theme = remember(isBlackThemeAllowed) {
            appPreferencesStore.getThemeFlow().mapToTheme(isBlackThemeAllowed)
        }.collectAsState(initial = Theme.System)

        val otherSessions by remember {
            sessionStore.sessionsFlow().map { list ->
                list
                    .filter { it.userId != matrixClient.sessionId.value }
                    .map {
                        MatrixUser(
                            userId = UserId(it.userId),
                            displayName = it.userDisplayName,
                            avatarUrl = it.userAvatarUrl,
                        )
                    }
                    .toImmutableList()
            }
        }.collectAsState(initial = persistentListOf())

        val themeOption by remember {
            derivedStateOf {
                when (theme.value) {
                    Theme.System -> ThemeOption.System
                    Theme.Dark -> ThemeOption.Dark
                    Theme.Black -> ThemeOption.Black
                    Theme.Light -> ThemeOption.Light
                }
            }
        }

        val availableThemeOptions = remember(isBlackThemeAllowed) {
            if (isBlackThemeAllowed) {
                ThemeOption.entries
            } else {
                ThemeOption.entries.filterNot { it == ThemeOption.Black }
            }.toImmutableList()
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
        val hasAnalyticsProviders = remember { analyticsService.getAvailableAnalyticsProviders().isNotEmpty() }

        val showLabsItem = remember { featureFlagService.getAvailableFeatures(isInLabs = true).isNotEmpty() }

        val showDeveloperSettings by showDeveloperSettingsProvider.showDeveloperSettings.collectAsState()

        fun handleEvent(event: PreferencesRootEvent) {
            when (event) {
                is PreferencesRootEvent.OnVersionInfoClick -> {
                    showDeveloperSettingsProvider.unlockDeveloperSettings(coroutineScope)
                }
                is PreferencesRootEvent.SwitchToSession -> coroutineScope.launch {
                    sessionStore.setLatestSession(event.sessionId.value)
                }
                PreferencesRootEvent.ToggleOtherAccountsExpanded -> coroutineScope.launch {
                    appPreferencesStore.setOtherAccountsExpanded(!isOtherAccountsSectionExpanded)
                }
                is PreferencesRootEvent.SetTheme -> sessionCoroutineScope.launch {
                    when (event.theme) {
                        ThemeOption.System -> appPreferencesStore.setTheme(Theme.System.name)
                        ThemeOption.Dark -> appPreferencesStore.setTheme(Theme.Dark.name)
                        ThemeOption.Black -> appPreferencesStore.setTheme(Theme.Black.name)
                        ThemeOption.Light -> appPreferencesStore.setTheme(Theme.Light.name)
                    }
                }
            }
        }

        return PreferencesRootState(
            myUser = matrixUser.value,
            userStatusState = userStatusState,
            preferencesAccountState = preferencesAccountState,
            theme = themeOption,
            availableThemeOptions = availableThemeOptions,
            version = remember { versionFormatter.get() },
            isMultiAccountEnabled = isMultiAccountEnabled,
            isOtherAccountsSectionExpanded = isOtherAccountsSectionExpanded,
            otherSessions = otherSessions,
            showAnalyticsSettings = hasAnalyticsProviders,
            showDeveloperSettings = showDeveloperSettings,
            showLabsItem = showLabsItem,
            snackbarMessage = snackbarMessage,
            eventSink = ::handleEvent,
        )
    }
}
