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
import io.element.android.compound.theme.Theme
import io.element.android.features.location.api.LocationService
import io.element.android.features.location.test.FakeLocationService
import io.element.android.features.preferences.impl.account.aPreferencesAccountState
import io.element.android.features.preferences.impl.userstatus.aUserStatusState
import io.element.android.features.preferences.impl.utils.ShowDeveloperSettingsProvider
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeature
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.user.UserStatus
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PreferencesRootPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val matrixClient = FakeMatrixClient()
        createPreferencesRootPresenter(matrixClient = matrixClient).test {
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
            assertThat(initialState.isOtherAccountsSectionExpanded).isTrue()
            assertThat(initialState.otherSessions).isEmpty()
            assertThat(initialState.preferencesAccountState).isEqualTo(aPreferencesAccountState())
            val loadedState = awaitItem()
            assertThat(loadedState.theme).isEqualTo(ThemeOption.System)
            assertThat(loadedState.availableThemeOptions).isEqualTo(
                listOf(ThemeOption.System, ThemeOption.Light, ThemeOption.Dark).toImmutableList()
            )
            assertThat(loadedState.userStatusState).isNotNull()
            assertThat(loadedState.showAnalyticsSettings).isFalse()
            assertThat(loadedState.showDeveloperSettings).isTrue()
            assertThat(loadedState.showLabsItem).isFalse()
            assertThat(loadedState.showLocationSettings).isTrue()
            assertThat(loadedState.snackbarMessage).isNull()
        }
    }

    @Test
    fun `present - location settings are hidden when the location service is not available`() = runTest {
        createPreferencesRootPresenter(
            locationService = FakeLocationService(isServiceAvailable = false),
        ).test {
            assertThat(awaitItem().showLocationSettings).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - user status state is null when the server does not support it`() = runTest {
        createPreferencesRootPresenter(
            matrixClient = FakeMatrixClient().apply {
                isUserStatusSupportedResult = Result.success(false)
            },
        ).test {
            consumeItemsUntilPredicate { it.userStatusState == null }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - change theme`() = runTest {
        createPreferencesRootPresenter().test {
            // Skip until the initial data is loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat(theme).isEqualTo(ThemeOption.System)
                eventSink(PreferencesRootEvent.SetTheme(ThemeOption.Dark))
            }
            with(awaitItem()) {
                assertThat(theme).isEqualTo(ThemeOption.Dark)
                eventSink(PreferencesRootEvent.SetTheme(ThemeOption.Light))
            }
            with(awaitItem()) {
                assertThat(theme).isEqualTo(ThemeOption.Light)
                eventSink(PreferencesRootEvent.SetTheme(ThemeOption.System))
            }
            with(awaitItem()) {
                assertThat(theme).isEqualTo(ThemeOption.System)
            }
        }
    }

    @Test
    fun `present - black theme option shown when feature flag enabled`() = runTest {
        createPreferencesRootPresenter(
            featureFlagService = FakeFeatureFlagService().apply {
                setFeatureEnabled(FeatureFlags.AllowBlackTheme, true)
            }
        ).test {
            val state = consumeItemsUntilPredicate { it.availableThemeOptions.contains(ThemeOption.Black) }.last()
            assertThat(state.availableThemeOptions).isEqualTo(ThemeOption.entries.toImmutableList())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - stored black theme falls back to dark when feature flag disabled`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore().apply {
            setTheme(Theme.Black.name)
        }
        createPreferencesRootPresenter(appPreferencesStore = appPreferencesStore).test {
            consumeItemsUntilPredicate { it.theme == ThemeOption.Dark }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - developer settings is hidden by default in release builds`() = runTest {
        createPreferencesRootPresenter(
            showDeveloperSettingsProvider = ShowDeveloperSettingsProvider(aBuildMeta(BuildType.RELEASE))
        ).test {
            val loadedState = awaitFirstItem()
            assertThat(loadedState.showDeveloperSettings).isFalse()
        }
    }

    @Test
    fun `present - developer settings can be enabled in release builds`() = runTest {
        createPreferencesRootPresenter(
            showDeveloperSettingsProvider = ShowDeveloperSettingsProvider(aBuildMeta(BuildType.RELEASE))
        ).test {
            val loadedState = awaitFirstItem()
            repeat(times = ShowDeveloperSettingsProvider.DEVELOPER_SETTINGS_COUNTER) {
                assertThat(loadedState.showDeveloperSettings).isFalse()
                loadedState.eventSink(PreferencesRootEvent.OnVersionInfoClick)
            }
            assertThat(awaitItem().showDeveloperSettings).isTrue()
        }
    }

    @Test
    fun `present - labs can be shown if any feature flag is in labs and not finished`() = runTest {
        createPreferencesRootPresenter(
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
        ).test {
            assertThat(awaitItem().showLabsItem).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - labs can't be shown if all feature flags in labs are finished`() = runTest {
        createPreferencesRootPresenter(
            featureFlagService = FakeFeatureFlagService(
                getAvailableFeaturesResult = { _, _ ->
                    emptyList()
                }
            ),
        ).test {
            skipItems(1)
            assertThat(awaitItem().showLabsItem).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - multiple accounts`() = runTest {
        createPreferencesRootPresenter(
            matrixClient = FakeMatrixClient(sessionId = A_SESSION_ID),
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
                        userAvatarData = "avatarData",
                    ),
                )
            )
        ).test {
            val state = awaitFirstItem()
            assertThat(state.isMultiAccountEnabled).isTrue()
            assertThat(state.otherSessions).hasSize(1)
            assertThat(state.otherSessions[0]).isEqualTo(
                OtherAccount(
                    user = MatrixUser(
                        userId = A_SESSION_ID_2,
                        displayName = "Bob",
                        avatarUrl = "avatarUrl",
                        avatarThumbnail = "avatarData",
                    ),
                    unreadCount = 0,
                )
            )
        }
    }

    @Test
    fun `present - other accounts keep the session store order and get their unread count`() = runTest {
        var bobUnreadCount = 3L
        val bobSyncService = FakeSyncService()
        val bobClient = FakeMatrixClient(
            sessionId = A_SESSION_ID_2,
            syncService = bobSyncService,
            totalUnreadCountResult = { Result.success(bobUnreadCount) },
        )
        createPreferencesRootPresenter(
            matrixClient = FakeMatrixClient(sessionId = A_SESSION_ID),
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = A_USER_ID_3.value),
                    aSessionData(sessionId = A_SESSION_ID.value),
                    aSessionData(sessionId = A_SESSION_ID_2.value),
                )
            ),
            matrixClientProvider = FakeMatrixClientProvider { sessionId ->
                when (sessionId) {
                    A_SESSION_ID_2 -> Result.success(bobClient)
                    // Client for the third account is not in memory
                    else -> Result.failure(AN_EXCEPTION)
                }
            },
        ).test {
            val state = consumeItemsUntilPredicate { it.otherSessions.isNotEmpty() }.last()
            assertThat(state.otherSessions.map { it.user.userId to it.unreadCount }).containsExactly(
                A_USER_ID_3 to 0,
                A_SESSION_ID_2 to 3,
            ).inOrder()
            // The unread count is refreshed on sync heartbeat
            bobUnreadCount = 5
            bobSyncService.emitHeartBeat()
            val updatedState = consumeItemsUntilPredicate { state -> state.otherSessions.any { it.unreadCount == 5 } }.last()
            assertThat(updatedState.otherSessions.map { it.user.userId to it.unreadCount }).containsExactly(
                A_USER_ID_3 to 0,
                A_SESSION_ID_2 to 5,
            ).inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - other accounts render the status of their client, and keep their stored user data`() = runTest {
        val bobStatus = DisplayedStatus.UserSet(UserStatus(emoji = "🌴", text = "Away"))
        createPreferencesRootPresenter(
            matrixClient = FakeMatrixClient(sessionId = A_SESSION_ID),
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = A_SESSION_ID.value),
                    aSessionData(
                        sessionId = A_SESSION_ID_2.value,
                        userDisplayName = "Bob",
                        userAvatarUrl = "avatarUrl",
                        userAvatarData = "avatarData",
                    ),
                    aSessionData(sessionId = A_USER_ID_3.value),
                )
            ),
            matrixClientProvider = FakeMatrixClientProvider { sessionId ->
                when (sessionId) {
                    A_SESSION_ID_2 -> Result.success(
                        FakeMatrixClient(
                            sessionId = A_SESSION_ID_2,
                            // The client knows about the status, but not about the avatar thumbnail
                            userDisplayName = "Bob from the client",
                            userDisplayedStatus = bobStatus,
                        )
                    )
                    // Client for the third account cannot be restored
                    else -> Result.failure(AN_EXCEPTION)
                }
            },
        ).test {
            val state = consumeItemsUntilPredicate { it.otherSessions.size == 2 }.last()
            assertThat(state.otherSessions[0]).isEqualTo(
                OtherAccount(
                    user = MatrixUser(
                        userId = A_SESSION_ID_2,
                        displayName = "Bob",
                        avatarUrl = "avatarUrl",
                        avatarThumbnail = "avatarData",
                        displayedStatus = bobStatus,
                    ),
                    unreadCount = 0,
                )
            )
            // No client, so no status
            assertThat(state.otherSessions[1].user.displayedStatus).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - other accounts section is collapsed if the preference says so`() = runTest {
        createPreferencesRootPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(isOtherAccountsExpanded = false),
        ).test {
            consumeItemsUntilPredicate { !it.isOtherAccountsSectionExpanded }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - toggle other accounts expanded stores the new value`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore()
        createPreferencesRootPresenter(appPreferencesStore = appPreferencesStore).test {
            val initialState = awaitFirstItem()
            assertThat(initialState.isOtherAccountsSectionExpanded).isTrue()
            initialState.eventSink(PreferencesRootEvent.ToggleOtherAccountsExpanded)
            val collapsedState = consumeItemsUntilPredicate { !it.isOtherAccountsSectionExpanded }.last()
            assertThat(appPreferencesStore.isOtherAccountsExpandedFlow().first()).isFalse()
            // Toggling again must expand the section back
            collapsedState.eventSink(PreferencesRootEvent.ToggleOtherAccountsExpanded)
            consumeItemsUntilPredicate { it.isOtherAccountsSectionExpanded }
            assertThat(appPreferencesStore.isOtherAccountsExpandedFlow().first()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        skipItems(1)
        return awaitItem()
    }

    private fun CoroutineScope.createPreferencesRootPresenter(
        matrixClient: FakeMatrixClient = FakeMatrixClient(),
        showDeveloperSettingsProvider: ShowDeveloperSettingsProvider = ShowDeveloperSettingsProvider(aBuildMeta(BuildType.DEBUG)),
        featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
        sessionStore: SessionStore = InMemorySessionStore(),
        appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
        locationService: LocationService = FakeLocationService(isServiceAvailable = true),
        matrixClientProvider: MatrixClientProvider = FakeMatrixClientProvider(),
    ) = PreferencesRootPresenter(
        matrixClient = matrixClient,
        analyticsService = FakeAnalyticsService(),
        versionFormatter = FakeVersionFormatter(),
        snackbarDispatcher = SnackbarDispatcher(),
        showDeveloperSettingsProvider = showDeveloperSettingsProvider,
        featureFlagService = featureFlagService,
        locationService = locationService,
        sessionStore = sessionStore,
        matrixClientProvider = matrixClientProvider,
        appPreferencesStore = appPreferencesStore,
        userStatusPresenter = { aUserStatusState() },
        preferencesAccountPresenter = { aPreferencesAccountState() },
        sessionCoroutineScope = this,
    )
}
