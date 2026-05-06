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
import io.element.android.libraries.push.api.notifications.sound.NotificationSoundCopier
import io.element.android.libraries.push.api.notifications.sound.NotificationSoundCopier.CopyResult
import io.element.android.libraries.push.api.notifications.sound.NotificationSoundCopier.SoundSlot
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushstore.api.UserPushStore
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val notificationSoundCopier: NotificationSoundCopier,
    private val soundDisplayNameResolver: SoundDisplayNameResolver,
    private val stringProvider: StringProvider,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : Presenter<NotificationSettingsState> {
    // Serializes the pick → copy → persist → recreate pipeline per slot. The copier already locks
    // its `<slot>.tmp` file, but the persist+recreate window outside the copier is unprotected;
    // without this, two rapid picks (e.g. an automation or accessibility service firing back-to-
    // back events) could interleave and leave the channel id pointing at the older version while
    // DataStore reflects the newer one.
    private val messageSoundLock = Mutex()
    private val callRingtoneLock = Mutex()

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
        val persistedMessageSoundTitle by remember { appPreferencesStore.getMessageSoundDisplayNameFlow() }.collectAsState(initial = null)
        val persistedCallRingtoneTitle by remember { appPreferencesStore.getCallRingtoneDisplayNameFlow() }.collectAsState(initial = null)
        val defaultLabel = stringProvider.getString(R.string.screen_notification_settings_sound_system_default)

        // One-shot classification for the call-ringtone slot only: users who customised the
        // channel via Android system settings before the in-app picker existed (channel version 0,
        // persisted SystemDefault) get reflected here. The message-sound equivalent is handled by
        // the eager migration in DefaultNotificationChannels.createNotificationChannels(). Cleared
        // on any in-app pick so a stale read can't bleed into the row after the recreate.
        var legacyCallRingtone by remember { mutableStateOf<NotificationSound?>(null) }
        LaunchedEffect(Unit) {
            val config = appPreferencesStore.getNotificationSoundChannelConfig()
            if (config.callRingtone == NotificationSound.SystemDefault && config.callRingtoneVersion == 0) {
                legacyCallRingtone = notificationSoundUpdater.readRingingCallChannelSound()
            }
        }

        val effectiveMessageSound = messageSound
        val effectiveCallRingtone = if (callRingtone == NotificationSound.SystemDefault) {
            legacyCallRingtone ?: callRingtone
        } else {
            callRingtone
        }

        val messageSoundDisplayName = probeSoundDisplayName(effectiveMessageSound, persistedMessageSoundTitle, defaultLabel)
        val callRingtoneDisplayName = probeSoundDisplayName(effectiveCallRingtone, persistedCallRingtoneTitle, defaultLabel)

        var messageSoundCopyError by remember { mutableStateOf(false) }
        var callRingtoneCopyError by remember { mutableStateOf(false) }
        var showMessageSoundDialog by remember { mutableStateOf(false) }
        var pendingMessageSoundPickerLaunch by remember { mutableIntStateOf(0) }

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
                is NotificationSettingsEvents.SetMessageSound -> applyMessageSound(
                    sound = event.sound,
                    sessionCoroutineScope = sessionCoroutineScope,
                    onCopyError = { messageSoundCopyError = true },
                    onCopySuccess = { messageSoundCopyError = false },
                    onChannelFailure = { failure -> changeNotificationSettingAction.value = AsyncAction.Failure(failure) },
                )
                is NotificationSettingsEvents.SelectMessageSoundPreset -> {
                    showMessageSoundDialog = false
                    applyMessageSound(
                        sound = event.sound,
                        sessionCoroutineScope = sessionCoroutineScope,
                        onCopyError = { messageSoundCopyError = true },
                        onCopySuccess = { messageSoundCopyError = false },
                        onChannelFailure = { failure -> changeNotificationSettingAction.value = AsyncAction.Failure(failure) },
                    )
                }
                NotificationSettingsEvents.ShowMessageSoundDialog -> showMessageSoundDialog = true
                NotificationSettingsEvents.DismissMessageSoundDialog -> showMessageSoundDialog = false
                NotificationSettingsEvents.LaunchMessageSoundPicker -> {
                    showMessageSoundDialog = false
                    pendingMessageSoundPickerLaunch++
                }
                is NotificationSettingsEvents.SetCallRingtone -> {
                    legacyCallRingtone = null
                    applyCallRingtone(
                        sound = event.sound,
                        sessionCoroutineScope = sessionCoroutineScope,
                        onCopyError = { callRingtoneCopyError = true },
                        onCopySuccess = { callRingtoneCopyError = false },
                        onChannelFailure = { failure -> changeNotificationSettingAction.value = AsyncAction.Failure(failure) },
                    )
                }
                NotificationSettingsEvents.DismissMessageSoundCopyError -> {
                    messageSoundCopyError = false
                }
                NotificationSettingsEvents.DismissCallRingtoneCopyError -> {
                    callRingtoneCopyError = false
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
                sound = effectiveMessageSound,
                displayName = messageSoundDisplayName,
                copyError = messageSoundCopyError,
            ),
            callRingtone = NotificationSettingsState.SoundChannelUiState(
                sound = effectiveCallRingtone,
                displayName = callRingtoneDisplayName,
                copyError = callRingtoneCopyError,
            ),
            showMessageSoundDialog = showMessageSoundDialog,
            pendingMessageSoundPickerLaunch = pendingMessageSoundPickerLaunch,
            eventSink = ::handleEvent,
        )
    }

    private fun applyMessageSound(
        sound: NotificationSound,
        sessionCoroutineScope: CoroutineScope,
        onCopyError: () -> Unit,
        onCopySuccess: () -> Unit,
        onChannelFailure: (Throwable) -> Unit,
    ) {
        sessionCoroutineScope.launch {
            messageSoundLock.withLock {
                // Re-selecting the current non-Custom sound is a no-op: skip the channel churn
                // (delete + create round-trips through system_server) and the version bump. Custom
                // still falls through so the copier refreshes the file from the (possibly updated)
                // source URI.
                if (sound !is NotificationSound.Custom && appPreferencesStore.getMessageSoundFlow().first() == sound) {
                    onCopySuccess()
                    return@withLock
                }
                val resolved = resolvePickedSound(sound, SoundSlot.Message, onCopyError) ?: return@withLock
                runCatchingExceptions {
                    onCopySuccess()
                    val newVersion = appPreferencesStore.setMessageSoundAndIncrementVersion(resolved.first, resolved.second)
                    notificationSoundUpdater.recreateNoisyChannel(resolved.first, newVersion)
                    // Non-Custom picks bypass the copier, so they don't sweep the prior Custom file
                    // inline. Drop it now that the new channel no longer references it.
                    if (resolved.first !is NotificationSound.Custom) {
                        notificationSoundCopier.deleteStoredSoundFor(SoundSlot.Message)
                    }
                }.onFailure(onChannelFailure)
            }
        }
    }

    private fun applyCallRingtone(
        sound: NotificationSound,
        sessionCoroutineScope: CoroutineScope,
        onCopyError: () -> Unit,
        onCopySuccess: () -> Unit,
        onChannelFailure: (Throwable) -> Unit,
    ) {
        sessionCoroutineScope.launch {
            callRingtoneLock.withLock {
                if (sound !is NotificationSound.Custom && appPreferencesStore.getCallRingtoneFlow().first() == sound) {
                    onCopySuccess()
                    return@withLock
                }
                val resolved = resolvePickedSound(sound, SoundSlot.Call, onCopyError) ?: return@withLock
                runCatchingExceptions {
                    onCopySuccess()
                    val newVersion = appPreferencesStore.setCallRingtoneAndIncrementVersion(resolved.first, resolved.second)
                    notificationSoundUpdater.recreateRingingCallChannel(resolved.first, newVersion)
                    if (resolved.first !is NotificationSound.Custom) {
                        notificationSoundCopier.deleteStoredSoundFor(SoundSlot.Call)
                    }
                }.onFailure(onChannelFailure)
            }
        }
    }

    /**
     * Custom rows prefer [persistedTitle] (captured at copy time), fall back to a live resolver
     * probe for legacy data, then to a localised "Custom" — never to [defaultLabel], so an
     * unresolvable Custom isn't mislabelled as Default.
     */
    @Composable
    private fun probeSoundDisplayName(
        sound: NotificationSound,
        persistedTitle: String?,
        defaultLabel: String,
    ): String = when (sound) {
        NotificationSound.SystemDefault -> defaultLabel
        NotificationSound.ElementDefault -> stringProvider.getString(R.string.screen_notification_settings_sound_element_default)
        NotificationSound.Silent -> stringProvider.getString(R.string.screen_notification_settings_sound_silent)
        is NotificationSound.Custom -> {
            val nonBlankPersisted = persistedTitle?.takeUnless { it.isBlank() }
            if (nonBlankPersisted != null) {
                nonBlankPersisted
            } else {
                val customFallback = stringProvider.getString(R.string.screen_notification_settings_sound_custom_fallback)
                val resolved by produceState(initialValue = "", sound.uri) {
                    val title = soundDisplayNameResolver.resolveCustomSoundTitle(sound.uri)
                    value = title?.takeUnless { it.isBlank() } ?: customFallback
                }
                resolved
            }
        }
    }

    /**
     * For a Custom pick, copies into app-private storage and returns (FileProvider URI, title).
     * Returns null on copy failure after invoking [onCopyError]. Pass-through for SystemDefault /
     * Silent.
     */
    private suspend fun resolvePickedSound(
        requested: NotificationSound,
        slot: SoundSlot,
        onCopyError: () -> Unit,
    ): Pair<NotificationSound, String?>? {
        if (requested !is NotificationSound.Custom) return requested to null
        return when (val result = notificationSoundCopier.copyToAppFiles(requested.uri, slot)) {
            is CopyResult.Success -> NotificationSound.Custom(result.fileProviderUriString) to result.displayName
            is CopyResult.Failure -> {
                Timber.w(result.cause, "Notification sound copy failed for slot=%s", slot)
                onCopyError()
                null
            }
            CopyResult.UnplayableSource,
            CopyResult.UnplayableCopy,
            CopyResult.FileTooLarge -> {
                Timber.w("Notification sound rejected: result=%s slot=%s", result::class.simpleName, slot)
                onCopyError()
                null
            }
        }
    }

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
