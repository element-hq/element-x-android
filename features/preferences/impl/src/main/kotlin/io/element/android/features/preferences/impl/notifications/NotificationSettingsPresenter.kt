/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingStateNoSuccess
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.api.store.NotificationSound
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.api.notifications.NotificationSoundUpdater
import io.element.android.libraries.push.api.notifications.SoundDisplayNameResolver
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushstore.api.UserPushStore
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@Inject
class NotificationSettingsPresenter(
    private val notificationSettingsService: NotificationSettingsService,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val matrixClient: MatrixClient,
    private val pushService: PushService,
    private val systemNotificationsEnabledProvider: SystemNotificationsEnabledProvider,
    private val fullScreenIntentPermissionsPresenter: Presenter<FullScreenIntentPermissionsState>,
    private val appPreferencesStore: AppPreferencesStore,
    private val notificationSoundUpdater: NotificationSoundUpdater,
    private val soundDisplayNameResolver: SoundDisplayNameResolver,
    private val stringProvider: StringProvider,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : Presenter<NotificationSettingsState> {
    @Composable
    override fun present(): NotificationSettingsState {
        val userPushStore = remember { userPushStoreFactory.getOrCreate(matrixClient.sessionId) }
        val systemNotificationsEnabled: MutableState<Boolean> = remember {
            mutableStateOf(systemNotificationsEnabledProvider.notificationsEnabled())
        }
        val changeNotificationSettingAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        val localCoroutineScope = rememberCoroutineScope()
        val appNotificationsEnabled by remember {
            userPushStore.getNotificationEnabledForDevice()
        }.collectAsState(initial = false)

        val matrixSettings: MutableState<NotificationSettingsState.MatrixSettings> = remember {
            mutableStateOf(NotificationSettingsState.MatrixSettings.Uninitialized)
        }

        // Used to force a recomposition
        var refreshFullScreenIntentSettings by remember { mutableIntStateOf(0) }

        LaunchedEffect(Unit) {
            fetchSettings(matrixSettings)
            observeNotificationSettings(matrixSettings, changeNotificationSettingAction)
        }

        // List of PushProvider -> Distributor
        val distributors = remember {
            pushService.getAvailablePushProviders()
                .flatMap { pushProvider ->
                    pushProvider.getDistributors().map { distributor ->
                        pushProvider to distributor
                    }
                }
        }
        // List of Distributors
        val availableDistributors = remember {
            distributors.map { it.second }.toImmutableList()
        }

        var currentDistributor by remember { mutableStateOf<AsyncData<Distributor>>(AsyncData.Uninitialized) }
        var refreshPushProvider by remember { mutableIntStateOf(0) }

        LaunchedEffect(refreshPushProvider) {
            val p = pushService.getCurrentPushProvider(matrixClient.sessionId)
            val distributor = p?.getCurrentDistributor(matrixClient.sessionId)
            currentDistributor = if (distributor != null) {
                AsyncData.Success(distributor)
            } else {
                AsyncData.Failure(Exception("Failed to get current push provider"))
            }
        }

        var showChangePushProviderDialog by remember { mutableStateOf(false) }

        val messageSound by remember { appPreferencesStore.getMessageSoundFlow() }.collectAsState(initial = NotificationSound.SystemDefault)
        val callRingtone by remember { appPreferencesStore.getCallRingtoneFlow() }.collectAsState(initial = NotificationSound.SystemDefault)
        val defaultLabel = stringProvider.getString(R.string.screen_notification_settings_sound_default)
        val messageSoundProbe = probeSound(messageSound, defaultLabel)
        val callRingtoneProbe = probeSound(callRingtone, defaultLabel)

        // Mid-session detection: if a previously persisted Custom URI fails to resolve while this
        // screen is open, auto-revert to SystemDefault and surface an inline alert under the row.
        // The home banner (driven by boot-time sanitization) is intentionally not used here — the
        // user is already on the screen where the fix lives. The flag is in-memory only; once the
        // sound is reverted to SystemDefault, the resolver path won't run again for that channel.
        var messageSoundWasReverted by remember { mutableStateOf(false) }
        var callRingtoneWasReverted by remember { mutableStateOf(false) }

        LaunchedEffect(messageSound, messageSoundProbe.isUnavailable) {
            if (messageSound is NotificationSound.Custom && messageSoundProbe.isUnavailable) {
                messageSoundWasReverted = true
                // Hand off to sessionCoroutineScope (not LaunchedEffect's own scope) so the persisted
                // revert and channel recreate complete even if the user navigates away mid-flight.
                sessionCoroutineScope.launch {
                    runCatchingExceptions {
                        val newVersion = appPreferencesStore.setMessageSoundAndIncrementVersion(NotificationSound.SystemDefault)
                        notificationSoundUpdater.recreateNoisyChannel(NotificationSound.SystemDefault, newVersion)
                        // Also drop any pre-existing persisted unavailable bit for this channel —
                        // the boot-time banner has done its job; the inline alert takes over.
                        appPreferencesStore.clearMessageSoundUnavailable()
                    }.onFailure { failure ->
                        Timber.w(failure, "Failed to auto-revert unresolved message sound")
                    }
                }
            }
        }
        LaunchedEffect(callRingtone, callRingtoneProbe.isUnavailable) {
            if (callRingtone is NotificationSound.Custom && callRingtoneProbe.isUnavailable) {
                callRingtoneWasReverted = true
                sessionCoroutineScope.launch {
                    runCatchingExceptions {
                        val newVersion = appPreferencesStore.setCallRingtoneAndIncrementVersion(NotificationSound.SystemDefault)
                        notificationSoundUpdater.recreateRingingCallChannel(NotificationSound.SystemDefault, newVersion)
                        appPreferencesStore.clearCallRingtoneUnavailable()
                    }.onFailure { failure ->
                        Timber.w(failure, "Failed to auto-revert unresolved call ringtone")
                    }
                }
            }
        }

        fun CoroutineScope.changePushProvider(
            data: Pair<PushProvider, Distributor>?
        ) = launch {
            showChangePushProviderDialog = false
            data ?: return@launch
            val (pushProvider, distributor) = data
            // No op if the distributor is the same.
            if (distributor == currentDistributor.dataOrNull()) return@launch
            currentDistributor = AsyncData.Loading(currentDistributor.dataOrNull())
            pushService.registerWith(
                matrixClient = matrixClient,
                pushProvider = pushProvider,
                distributor = distributor
            )
                .fold(
                    {
                        refreshPushProvider++
                    },
                    {
                        currentDistributor = AsyncData.Failure(it)
                    }
                )
        }

        fun handleEvent(event: NotificationSettingsEvents) {
            when (event) {
                is NotificationSettingsEvents.SetAtRoomNotificationsEnabled -> {
                    localCoroutineScope.setAtRoomNotificationsEnabled(event.enabled, changeNotificationSettingAction)
                }
                is NotificationSettingsEvents.SetCallNotificationsEnabled -> {
                    localCoroutineScope.setCallNotificationsEnabled(event.enabled, changeNotificationSettingAction)
                }
                is NotificationSettingsEvents.SetInviteForMeNotificationsEnabled -> {
                    localCoroutineScope.setInviteForMeNotificationsEnabled(event.enabled, changeNotificationSettingAction)
                }
                is NotificationSettingsEvents.SetNotificationsEnabled -> sessionCoroutineScope.setNotificationsEnabled(userPushStore, event.enabled)
                NotificationSettingsEvents.ClearConfigurationMismatchError -> {
                    matrixSettings.value = NotificationSettingsState.MatrixSettings.Invalid(fixFailed = false)
                }
                NotificationSettingsEvents.FixConfigurationMismatch -> localCoroutineScope.fixConfigurationMismatch(matrixSettings)
                NotificationSettingsEvents.RefreshSystemNotificationsEnabled -> {
                    systemNotificationsEnabled.value = systemNotificationsEnabledProvider.notificationsEnabled()
                    refreshFullScreenIntentSettings++
                }
                NotificationSettingsEvents.ClearNotificationChangeError -> changeNotificationSettingAction.value = AsyncAction.Uninitialized
                NotificationSettingsEvents.ChangePushProvider -> showChangePushProviderDialog = true
                NotificationSettingsEvents.CancelChangePushProvider -> showChangePushProviderDialog = false
                is NotificationSettingsEvents.SetPushProvider -> localCoroutineScope.changePushProvider(distributors.getOrNull(event.index))
                is NotificationSettingsEvents.SetMessageSound -> {
                    // Explicit pick clears the inline reverted-alert; the user has acknowledged.
                    messageSoundWasReverted = false
                    sessionCoroutineScope.launch {
                        runCatchingExceptions {
                            val newVersion = appPreferencesStore.setMessageSoundAndIncrementVersion(event.sound)
                            notificationSoundUpdater.recreateNoisyChannel(event.sound, newVersion)
                            // Auto-resolve: a successful pick drops the message-sound bit from the
                            // persisted unavailable state. If the call-ringtone half is still set the
                            // banner downgrades from Both → CallRingtone; otherwise it disappears once
                            // the user dismisses the announcement.
                            appPreferencesStore.clearMessageSoundUnavailable()
                        }.onFailure { failure ->
                            changeNotificationSettingAction.value = AsyncAction.Failure(failure)
                        }
                    }
                }
                is NotificationSettingsEvents.SetCallRingtone -> {
                    callRingtoneWasReverted = false
                    sessionCoroutineScope.launch {
                        runCatchingExceptions {
                            val newVersion = appPreferencesStore.setCallRingtoneAndIncrementVersion(event.sound)
                            notificationSoundUpdater.recreateRingingCallChannel(event.sound, newVersion)
                            appPreferencesStore.clearCallRingtoneUnavailable()
                        }.onFailure { failure ->
                            changeNotificationSettingAction.value = AsyncAction.Failure(failure)
                        }
                    }
                }
                NotificationSettingsEvents.DismissMessageSoundRevertedAlert -> {
                    messageSoundWasReverted = false
                }
                NotificationSettingsEvents.DismissCallRingtoneRevertedAlert -> {
                    callRingtoneWasReverted = false
                }
            }
        }

        return NotificationSettingsState(
            matrixSettings = matrixSettings.value,
            appSettings = NotificationSettingsState.AppSettings(
                systemNotificationsEnabled = systemNotificationsEnabled.value,
                appNotificationsEnabled = appNotificationsEnabled,
            ),
            changeNotificationSettingAction = changeNotificationSettingAction.value,
            currentPushDistributor = currentDistributor,
            availablePushDistributors = availableDistributors,
            showChangePushProviderDialog = showChangePushProviderDialog,
            fullScreenIntentPermissionsState = key(refreshFullScreenIntentSettings) { fullScreenIntentPermissionsPresenter.present() },
            messageSound = NotificationSettingsState.SoundChannelUiState(
                sound = messageSound,
                displayName = messageSoundProbe.displayName,
                wasReverted = messageSoundWasReverted,
            ),
            callRingtone = NotificationSettingsState.SoundChannelUiState(
                sound = callRingtone,
                displayName = callRingtoneProbe.displayName,
                wasReverted = callRingtoneWasReverted,
            ),
            eventSink = ::handleEvent,
        )
    }

    /**
     * Probes [sound] once to derive both its display label and a flag for whether the underlying
     * Custom URI is unresolvable. SystemDefault and Silent resolve synchronously and are never
     * unavailable; only Custom hits [produceState] and the resolver. Unifying both into a single
     * lookup avoids probing the resolver twice per recomposition.
     *
     * The row shows an empty label until the lookup settles instead of flashing the default label
     * for Custom sounds.
     */
    @Composable
    private fun probeSound(
        sound: NotificationSound,
        defaultLabel: String,
    ): SoundProbeResult = when (sound) {
        NotificationSound.SystemDefault -> SoundProbeResult(displayName = defaultLabel, isUnavailable = false)
        NotificationSound.Silent -> SoundProbeResult(
            displayName = stringProvider.getString(R.string.screen_notification_settings_sound_silent),
            isUnavailable = false,
        )
        is NotificationSound.Custom -> {
            val resolved by produceState(
                initialValue = SoundProbeResult(displayName = "", isUnavailable = false),
                sound.uri,
                defaultLabel,
            ) {
                val title = soundDisplayNameResolver.resolveCustomSoundTitle(sound.uri)
                value = if (title == null) {
                    SoundProbeResult(displayName = defaultLabel, isUnavailable = true)
                } else {
                    SoundProbeResult(displayName = title, isUnavailable = false)
                }
            }
            resolved
        }
    }

    private data class SoundProbeResult(
        val displayName: String,
        val isUnavailable: Boolean,
    )

    @OptIn(FlowPreview::class)
    private fun CoroutineScope.observeNotificationSettings(
        target: MutableState<NotificationSettingsState.MatrixSettings>,
        changeNotificationSettingAction: MutableState<AsyncAction<Unit>>,
    ) {
        notificationSettingsService.notificationSettingsChangeFlow
            .debounce(0.5.seconds)
            .onEach {
                fetchSettings(target)
                changeNotificationSettingAction.value = AsyncAction.Uninitialized
            }
            .launchIn(this)
    }

    private fun CoroutineScope.fetchSettings(target: MutableState<NotificationSettingsState.MatrixSettings>) = launch {
        val groupDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = false, isOneToOne = false).getOrThrow()
        val encryptedGroupDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = true, isOneToOne = false).getOrThrow()

        val oneToOneDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = false, isOneToOne = true).getOrThrow()
        val encryptedOneToOneDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = true, isOneToOne = true).getOrThrow()

        if (groupDefaultMode != encryptedGroupDefaultMode || oneToOneDefaultMode != encryptedOneToOneDefaultMode) {
            target.value = NotificationSettingsState.MatrixSettings.Invalid(fixFailed = false)
            return@launch
        }

        val callNotificationsEnabled = notificationSettingsService.isCallEnabled().getOrThrow()
        val atRoomNotificationsEnabled = notificationSettingsService.isRoomMentionEnabled().getOrThrow()
        val inviteForMeNotificationsEnabled = notificationSettingsService.isInviteForMeEnabled().getOrThrow()

        target.value = NotificationSettingsState.MatrixSettings.Valid(
            atRoomNotificationsEnabled = atRoomNotificationsEnabled,
            callNotificationsEnabled = callNotificationsEnabled,
            inviteForMeNotificationsEnabled = inviteForMeNotificationsEnabled,
            defaultGroupNotificationMode = encryptedGroupDefaultMode,
            defaultOneToOneNotificationMode = encryptedOneToOneDefaultMode,
        )
    }

    private fun CoroutineScope.fixConfigurationMismatch(target: MutableState<NotificationSettingsState.MatrixSettings>) = launch {
        runCatchingExceptions {
            val groupDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = false, isOneToOne = false).getOrThrow()
            val encryptedGroupDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = true, isOneToOne = false).getOrThrow()

            if (groupDefaultMode != encryptedGroupDefaultMode) {
                notificationSettingsService.setDefaultRoomNotificationMode(
                    isEncrypted = encryptedGroupDefaultMode != RoomNotificationMode.ALL_MESSAGES,
                    mode = RoomNotificationMode.ALL_MESSAGES,
                    isOneToOne = false,
                )
            }

            val oneToOneDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = false, isOneToOne = true).getOrThrow()
            val encryptedOneToOneDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = true, isOneToOne = true).getOrThrow()

            if (oneToOneDefaultMode != encryptedOneToOneDefaultMode) {
                notificationSettingsService.setDefaultRoomNotificationMode(
                    isEncrypted = encryptedOneToOneDefaultMode != RoomNotificationMode.ALL_MESSAGES,
                    mode = RoomNotificationMode.ALL_MESSAGES,
                    isOneToOne = true,
                )
            }
        }.fold(
            onSuccess = {},
            onFailure = {
                target.value = NotificationSettingsState.MatrixSettings.Invalid(fixFailed = true)
            }
        )
    }

    private fun CoroutineScope.setAtRoomNotificationsEnabled(enabled: Boolean, action: MutableState<AsyncAction<Unit>>) = launch {
        action.runUpdatingStateNoSuccess {
            notificationSettingsService.setRoomMentionEnabled(enabled)
        }
    }

    private fun CoroutineScope.setCallNotificationsEnabled(enabled: Boolean, action: MutableState<AsyncAction<Unit>>) = launch {
        action.runUpdatingStateNoSuccess {
            notificationSettingsService.setCallEnabled(enabled)
        }
    }

    private fun CoroutineScope.setInviteForMeNotificationsEnabled(enabled: Boolean, action: MutableState<AsyncAction<Unit>>) = launch {
        action.runUpdatingStateNoSuccess {
            notificationSettingsService.setInviteForMeEnabled(enabled)
        }
    }

    private fun CoroutineScope.setNotificationsEnabled(userPushStore: UserPushStore, enabled: Boolean) = launch {
        userPushStore.setNotificationEnabledForDevice(enabled)
        if (enabled) {
            pushService.ensurePusherIsRegistered(matrixClient)
        } else {
            pushService.getCurrentPushProvider(matrixClient.sessionId)?.unregister(matrixClient)
        }
    }
}
