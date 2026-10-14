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
import io.element.android.features.location.api.LocationService
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
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.totalUnreadCountFlow
import io.element.android.libraries.matrix.api.user.toMatrixUser
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class PreferencesRootPresenter(
    private val matrixClient: MatrixClient,
    private val analyticsService: AnalyticsService,
    private val versionFormatter: VersionFormatter,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val showDeveloperSettingsProvider: ShowDeveloperSettingsProvider,
    private val featureFlagService: FeatureFlagService,
    private val locationService: LocationService,
    private val sessionStore: SessionStore,
    private val matrixClientProvider: MatrixClientProvider,
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
            sessionStore.sessionsFlow()
                .map { list -> list.filter { it.userId != matrixClient.sessionId.value } }
                .flatMapLatest { sessions ->
                    if (sessions.isEmpty()) {
                        flowOf(persistentListOf())
                    } else {
                        combine(
                            sessions.map { sessionData ->
                                val user = sessionData.toMatrixUser()
                                unreadCountFlow(user.userId).map { unreadCount -> OtherAccount(user, unreadCount) }
                            }
                        ) { it.toImmutableList() }
                    }
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

        val isLocationServiceAvailable by remember { locationService.isServiceAvailable() }.collectAsState()

        fun handleEvent(event: PreferencesRootEvent) {
            when (event) {
                is PreferencesRootEvent.OnVersionInfoClick -> {
                    showDeveloperSettingsProvider.unlockDeveloperSettings(coroutineScope)
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
            showLocationSettings = isLocationServiceAvailable,
            snackbarMessage = snackbarMessage,
            eventSink = ::handleEvent,
        )
    }

    private fun unreadCountFlow(sessionId: SessionId): Flow<Int> {
        val client = matrixClientProvider.getOrNull(sessionId) ?: return flowOf(0)
        return client.totalUnreadCountFlow()
    }
}
