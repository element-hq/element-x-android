/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.fullscreenintent.api.aFullScreenIntentPermissionsState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.api.store.NotificationSound
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.api.notifications.NotificationSoundUpdater
import io.element.android.libraries.push.api.notifications.sound.NotificationSoundCopier
import io.element.android.libraries.push.test.FakePushService
import io.element.android.libraries.push.test.notifications.FakeNotificationSoundUpdater
import io.element.android.libraries.push.test.notifications.FakeSoundDisplayNameResolver
import io.element.android.libraries.push.test.notifications.sound.FakeNotificationSoundCopier
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.test.FakePushProvider
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStoreFactory
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@Suppress("LargeClass")
class NotificationSettingsPresenterTest {
    @Test
    fun `present - ensures initial state is correct`() = runTest {
        val presenter = createNotificationSettingsPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.appSettings.appNotificationsEnabled).isFalse()
            assertThat(initialState.appSettings.systemNotificationsEnabled).isTrue()
            assertThat(initialState.matrixSettings).isEqualTo(NotificationSettingsState.MatrixSettings.Uninitialized)
            val loadedState = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()
            assertThat(loadedState.appSettings.appNotificationsEnabled).isTrue()
            assertThat(loadedState.appSettings.systemNotificationsEnabled).isTrue()
            val valid = loadedState.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid
            assertThat(valid?.atRoomNotificationsEnabled).isFalse()
            assertThat(valid?.callNotificationsEnabled).isFalse()
            assertThat(valid?.inviteForMeNotificationsEnabled).isFalse()
            assertThat(valid?.defaultGroupNotificationMode).isEqualTo(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
            assertThat(valid?.defaultOneToOneNotificationMode).isEqualTo(RoomNotificationMode.ALL_MESSAGES)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - default group notification mode changed`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService()
        val presenter = createNotificationSettingsPresenter(notificationSettingsService)
        presenter.test {
            notificationSettingsService.setDefaultRoomNotificationMode(isEncrypted = true, isOneToOne = false, mode = RoomNotificationMode.ALL_MESSAGES)
            notificationSettingsService.setDefaultRoomNotificationMode(isEncrypted = false, isOneToOne = false, mode = RoomNotificationMode.ALL_MESSAGES)
            val updatedState = consumeItemsUntilPredicate {
                (it.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid)
                    ?.defaultGroupNotificationMode == RoomNotificationMode.ALL_MESSAGES
            }.last()
            val valid = updatedState.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid
            assertThat(valid?.defaultGroupNotificationMode).isEqualTo(RoomNotificationMode.ALL_MESSAGES)
        }
    }

    @Test
    fun `present - notification settings mismatched`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService()
        val presenter = createNotificationSettingsPresenter(notificationSettingsService)
        presenter.test {
            notificationSettingsService.setDefaultRoomNotificationMode(
                isEncrypted = true,
                isOneToOne = false,
                mode = RoomNotificationMode.ALL_MESSAGES
            )
            notificationSettingsService.setDefaultRoomNotificationMode(
                isEncrypted = false,
                isOneToOne = false,
                mode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            )
            val updatedState = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Invalid
            }.last()
            assertThat(updatedState.matrixSettings).isEqualTo(NotificationSettingsState.MatrixSettings.Invalid(fixFailed = false))
        }
    }

    @Test
    fun `present - fix notification settings mismatched`() = runTest {
        // Start with a mismatched configuration
        val notificationSettingsService = FakeNotificationSettingsService(
            initialEncryptedGroupDefaultMode = RoomNotificationMode.ALL_MESSAGES,
            initialGroupDefaultMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
            initialEncryptedOneToOneDefaultMode = RoomNotificationMode.ALL_MESSAGES,
            initialOneToOneDefaultMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
        )
        val presenter = createNotificationSettingsPresenter(notificationSettingsService)
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(NotificationSettingsEvents.FixConfigurationMismatch)
            val fixedState = consumeItemsUntilPredicate(timeout = 2000.milliseconds) {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()
            val fixedMatrixState = fixedState.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid
            assertThat(fixedMatrixState?.defaultGroupNotificationMode).isEqualTo(RoomNotificationMode.ALL_MESSAGES)
        }
    }

    @Test
    fun `present - set notifications enabled`() = runTest {
        val unregisterWithResult = lambdaRecorder<MatrixClient, Result<Unit>> { Result.success(Unit) }
        val ensurePusherIsRegisteredResult = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val presenter = createNotificationSettingsPresenter(
            pushService = FakePushService(
                currentPushProvider = {
                    FakePushProvider(
                        unregisterWithResult = unregisterWithResult,
                    )
                },
                ensurePusherIsRegisteredResult = ensurePusherIsRegisteredResult,
            )
        )
        presenter.test {
            val loadedState = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()
            assertThat(loadedState.appSettings.appNotificationsEnabled).isTrue()
            loadedState.eventSink(NotificationSettingsEvents.SetNotificationsEnabled(false))
            val updatedState = consumeItemsUntilPredicate {
                !it.appSettings.appNotificationsEnabled
            }.last()
            assertThat(updatedState.appSettings.appNotificationsEnabled).isFalse()
            unregisterWithResult.assertions().isCalledOnce()
            // Enable notification again
            loadedState.eventSink(NotificationSettingsEvents.SetNotificationsEnabled(true))
            val updatedState2 = consumeItemsUntilPredicate {
                it.appSettings.appNotificationsEnabled
            }.last()
            assertThat(updatedState2.appSettings.appNotificationsEnabled).isTrue()
            ensurePusherIsRegisteredResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - set call notifications enabled`() = runTest {
        val presenter = createNotificationSettingsPresenter()
        presenter.test {
            val loadedState = consumeItemsUntilPredicate {
                (it.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid)?.callNotificationsEnabled == false
            }.last()
            val validMatrixState = loadedState.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid
            assertThat(validMatrixState?.callNotificationsEnabled).isFalse()
            loadedState.eventSink(NotificationSettingsEvents.SetCallNotificationsEnabled(true))
            val updatedState = consumeItemsUntilPredicate {
                (it.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid)?.callNotificationsEnabled == true
            }.last()
            val updatedMatrixState = updatedState.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid
            assertThat(updatedMatrixState?.callNotificationsEnabled).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - set invite for me notifications enabled`() = runTest {
        val presenter = createNotificationSettingsPresenter()
        presenter.test {
            val loadedState = consumeItemsUntilPredicate {
                (it.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid)?.inviteForMeNotificationsEnabled == false
            }.last()
            val validMatrixState = loadedState.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid
            assertThat(validMatrixState?.inviteForMeNotificationsEnabled).isFalse()
            loadedState.eventSink(NotificationSettingsEvents.SetInviteForMeNotificationsEnabled(true))
            val updatedState = consumeItemsUntilPredicate {
                (it.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid)?.inviteForMeNotificationsEnabled == true
            }.last()
            val updatedMatrixState = updatedState.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid
            assertThat(updatedMatrixState?.inviteForMeNotificationsEnabled).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - set atRoom notifications enabled`() = runTest {
        val presenter = createNotificationSettingsPresenter()
        presenter.test {
            val loadedState = consumeItemsUntilPredicate {
                (it.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid)?.atRoomNotificationsEnabled == false
            }.last()
            val validMatrixState = loadedState.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid
            assertThat(validMatrixState?.atRoomNotificationsEnabled).isFalse()
            loadedState.eventSink(NotificationSettingsEvents.SetAtRoomNotificationsEnabled(true))
            val updatedState = consumeItemsUntilPredicate {
                (it.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid)?.atRoomNotificationsEnabled == true
            }.last()
            val updatedMatrixState = updatedState.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid
            assertThat(updatedMatrixState?.atRoomNotificationsEnabled).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - clear notification settings change error`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService()
        val presenter = createNotificationSettingsPresenter(notificationSettingsService)
        notificationSettingsService.givenSetAtRoomError(AN_EXCEPTION)
        presenter.test {
            val loadedState = consumeItemsUntilPredicate {
                (it.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid)?.atRoomNotificationsEnabled == false
            }.last()
            val validMatrixState = loadedState.matrixSettings as? NotificationSettingsState.MatrixSettings.Valid
            assertThat(validMatrixState?.atRoomNotificationsEnabled).isFalse()
            loadedState.eventSink(NotificationSettingsEvents.SetAtRoomNotificationsEnabled(true))
            val errorState = consumeItemsUntilPredicate {
                it.changeNotificationSettingAction.isFailure()
            }.last()
            assertThat(errorState.changeNotificationSettingAction.isFailure()).isTrue()
            errorState.eventSink(NotificationSettingsEvents.ClearNotificationChangeError)
            val clearErrorState = consumeItemsUntilPredicate {
                it.changeNotificationSettingAction.isUninitialized()
            }.last()
            assertThat(clearErrorState.changeNotificationSettingAction.isUninitialized()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - change push provider`() = runTest {
        val presenter = createNotificationSettingsPresenter(
            pushService = createFakePushService(),
        )
        presenter.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.currentPushDistributor).isEqualTo(AsyncData.Success(Distributor(value = "aDistributorValue0", name = "aDistributorName0")))
            assertThat(initialState.availablePushDistributors).containsExactly(
                Distributor(value = "aDistributorValue0", name = "aDistributorName0"),
                Distributor(value = "aDistributorValue1", name = "aDistributorName1"),
            )
            initialState.eventSink.invoke(NotificationSettingsEvents.ChangePushProvider)
            val withDialog = awaitItem()
            assertThat(withDialog.showChangePushProviderDialog).isTrue()
            // Cancel
            withDialog.eventSink(NotificationSettingsEvents.CancelChangePushProvider)
            val withoutDialog = awaitItem()
            assertThat(withoutDialog.showChangePushProviderDialog).isFalse()
            withDialog.eventSink.invoke(NotificationSettingsEvents.ChangePushProvider)
            assertThat(awaitItem().showChangePushProviderDialog).isTrue()
            withDialog.eventSink(NotificationSettingsEvents.SetPushProvider(1))
            val withNewProvider = awaitItem()
            assertThat(withNewProvider.showChangePushProviderDialog).isFalse()
            assertThat(withNewProvider.currentPushDistributor).isInstanceOf(AsyncData.Loading::class.java)
            skipItems(1)
            val lastItem = awaitItem()
            assertThat(lastItem.currentPushDistributor).isEqualTo(AsyncData.Success(Distributor(value = "aDistributorValue1", name = "aDistributorName1")))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - change push provider to the same value is no op`() = runTest {
        val presenter = createNotificationSettingsPresenter(
            pushService = createFakePushService(),
        )
        presenter.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.currentPushDistributor).isEqualTo(AsyncData.Success(Distributor(value = "aDistributorValue0", name = "aDistributorName0")))
            assertThat(initialState.availablePushDistributors).containsExactly(
                Distributor(value = "aDistributorValue0", name = "aDistributorName0"),
                Distributor(value = "aDistributorValue1", name = "aDistributorName1"),
            )
            initialState.eventSink.invoke(NotificationSettingsEvents.ChangePushProvider)
            assertThat(awaitItem().showChangePushProviderDialog).isTrue()
            // Choose the same value (index 0)
            initialState.eventSink(NotificationSettingsEvents.SetPushProvider(0))
            val withNewProvider = awaitItem()
            assertThat(withNewProvider.showChangePushProviderDialog).isFalse()
            expectNoEvents()
        }
    }

    @Test
    fun `present - RefreshSystemNotificationsEnabled also refreshes fullScreenIntentState`() = runTest {
        var lambdaResult = aFullScreenIntentPermissionsState(permissionGranted = false)
        val fullScreenIntentPermissionsStateLambda = { lambdaResult }
        val presenter = createNotificationSettingsPresenter(
            pushService = createFakePushService(),
            fullScreenIntentPermissionsStateLambda = fullScreenIntentPermissionsStateLambda,
        )
        presenter.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.fullScreenIntentPermissionsState.permissionGranted).isFalse()

            // Change the notification settings
            lambdaResult = lambdaResult.copy(permissionGranted = true)
            // Check it's not changed unless we refresh
            expectNoEvents()

            // Refresh
            initialState.eventSink.invoke(NotificationSettingsEvents.RefreshSystemNotificationsEnabled)
            assertThat(awaitItem().fullScreenIntentPermissionsState.permissionGranted).isTrue()
        }
    }

    @Test
    fun `present - change push provider error`() = runTest {
        val presenter = createNotificationSettingsPresenter(
            pushService = createFakePushService(
                registerWithLambda = { _, _, _ ->
                    Result.failure(Exception("An error"))
                },
            ),
        )
        presenter.test {
            val initialState = awaitLastSequentialItem()
            initialState.eventSink.invoke(NotificationSettingsEvents.ChangePushProvider)
            val withDialog = awaitItem()
            assertThat(withDialog.showChangePushProviderDialog).isTrue()
            withDialog.eventSink(NotificationSettingsEvents.SetPushProvider(1))
            val withNewProvider = awaitItem()
            assertThat(withNewProvider.showChangePushProviderDialog).isFalse()
            assertThat(withNewProvider.currentPushDistributor).isInstanceOf(AsyncData.Loading::class.java)
            val lastItem = awaitItem()
            assertThat(lastItem.currentPushDistributor).isInstanceOf(AsyncData.Failure::class.java)
        }
    }

    private fun createFakePushService(
        registerWithLambda: (MatrixClient, PushProvider, Distributor) -> Result<Unit> = { _, _, _ ->
            Result.success(Unit)
        }
    ): PushService {
        val pushProvider1 = FakePushProvider(
            index = 0,
            name = "aFakePushProvider0",
            distributors = listOf(Distributor("aDistributorValue0", "aDistributorName0")),
        )
        val pushProvider2 = FakePushProvider(
            index = 1,
            name = "aFakePushProvider1",
            distributors = listOf(Distributor("aDistributorValue1", "aDistributorName1")),
        )
        return FakePushService(
            availablePushProviders = listOf(pushProvider1, pushProvider2),
            registerWithLambda = registerWithLambda,
        )
    }

    @Test
    fun `present - SetMessageSound atomically persists, increments version, and recreates channel`() = runTest {
        val recreateRecorder = lambdaRecorder<NotificationSound, Int, Unit> { _, _ -> }
        val appPreferencesStore = InMemoryAppPreferencesStore()
        val customSound = NotificationSound.Custom("content://media/internal/audio/media/42")
        val presenter = createNotificationSettingsPresenter(
            appPreferencesStore = appPreferencesStore,
            notificationSoundUpdater = FakeNotificationSoundUpdater(
                recreateNoisyChannelLambda = { sound, version -> recreateRecorder(sound, version) },
            ),
        )
        presenter.test {
            val initialState = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()
            assertThat(initialState.messageSound.sound).isEqualTo(NotificationSound.SystemDefault)

            initialState.eventSink(NotificationSettingsEvents.SetMessageSound(customSound))

            val updatedState = consumeItemsUntilPredicate { it.messageSound.sound == customSound }.last()
            assertThat(updatedState.messageSound.sound).isEqualTo(customSound)
            assertThat(appPreferencesStore.getNotificationSoundChannelConfig().messageSoundVersion).isEqualTo(1)
            recreateRecorder.assertions().isCalledOnce().with(value(customSound), value(1))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - SetCallRingtone with Silent atomically persists and recreates channel`() = runTest {
        val recreateRecorder = lambdaRecorder<NotificationSound, Int, Unit> { _, _ -> }
        val appPreferencesStore = InMemoryAppPreferencesStore()
        val presenter = createNotificationSettingsPresenter(
            appPreferencesStore = appPreferencesStore,
            notificationSoundUpdater = FakeNotificationSoundUpdater(
                recreateRingingCallChannelLambda = { sound, version -> recreateRecorder(sound, version) },
            ),
        )
        presenter.test {
            val initialState = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()
            assertThat(initialState.callRingtone.sound).isEqualTo(NotificationSound.SystemDefault)

            initialState.eventSink(NotificationSettingsEvents.SetCallRingtone(NotificationSound.Silent))

            val updatedState = consumeItemsUntilPredicate { it.callRingtone.sound == NotificationSound.Silent }.last()
            assertThat(updatedState.callRingtone.sound).isEqualTo(NotificationSound.Silent)
            assertThat(appPreferencesStore.getNotificationSoundChannelConfig().callRingtoneVersion).isEqualTo(1)
            recreateRecorder.assertions().isCalledOnce().with(value(NotificationSound.Silent), value(1))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - successive message sound changes increment version each time`() = runTest {
        val versions = mutableListOf<Int>()
        val appPreferencesStore = InMemoryAppPreferencesStore()
        val presenter = createNotificationSettingsPresenter(
            appPreferencesStore = appPreferencesStore,
            notificationSoundUpdater = FakeNotificationSoundUpdater(
                recreateNoisyChannelLambda = { _, version -> versions += version },
            ),
        )
        presenter.test {
            val initialState = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()

            initialState.eventSink(NotificationSettingsEvents.SetMessageSound(NotificationSound.Custom("content://a")))
            consumeItemsUntilPredicate { it.messageSound.sound == NotificationSound.Custom("content://a") }
            initialState.eventSink(NotificationSettingsEvents.SetMessageSound(NotificationSound.Custom("content://b")))
            consumeItemsUntilPredicate { it.messageSound.sound == NotificationSound.Custom("content://b") }
            initialState.eventSink(NotificationSettingsEvents.SetMessageSound(NotificationSound.SystemDefault))
            consumeItemsUntilPredicate { it.messageSound.sound == NotificationSound.SystemDefault }

            assertThat(versions).containsExactly(1, 2, 3).inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - SetMessageSound with SystemDefault clears existing custom URI`() = runTest {
        val recreateRecorder = lambdaRecorder<NotificationSound, Int, Unit> { _, _ -> }
        val appPreferencesStore = InMemoryAppPreferencesStore(messageSound = NotificationSound.Custom("content://existing"))
        val presenter = createNotificationSettingsPresenter(
            appPreferencesStore = appPreferencesStore,
            notificationSoundUpdater = FakeNotificationSoundUpdater(
                recreateNoisyChannelLambda = { sound, version -> recreateRecorder(sound, version) },
            ),
        )
        presenter.test {
            consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid &&
                    it.messageSound.sound == NotificationSound.Custom("content://existing")
            }.last().eventSink(NotificationSettingsEvents.SetMessageSound(NotificationSound.SystemDefault))
            consumeItemsUntilPredicate { it.messageSound.sound == NotificationSound.SystemDefault }
            assertThat(appPreferencesStore.getNotificationSoundChannelConfig().messageSound).isEqualTo(NotificationSound.SystemDefault)
            recreateRecorder.assertions().isCalledOnce().with(value(NotificationSound.SystemDefault), value(1))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - SetMessageSound surfaces DataStore failure via changeNotificationSettingAction`() = runTest {
        val backing = InMemoryAppPreferencesStore()
        val store = object : AppPreferencesStore by backing {
            override suspend fun setMessageSoundAndIncrementVersion(sound: NotificationSound, title: String?): Int {
                error("Simulated DataStore failure")
            }
        }
        val presenter = createNotificationSettingsPresenter(appPreferencesStore = store)
        presenter.test {
            val initialState = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()

            initialState.eventSink(NotificationSettingsEvents.SetMessageSound(NotificationSound.Custom("content://x")))

            val errorState = consumeItemsUntilPredicate {
                it.changeNotificationSettingAction.isFailure()
            }.last()
            assertThat(errorState.changeNotificationSettingAction.isFailure()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - SystemDefault and Silent display names do not invoke the resolver`() = runTest {
        val resolverCalls = mutableListOf<String>()
        val presenter = createNotificationSettingsPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(
                messageSound = NotificationSound.SystemDefault,
                callRingtone = NotificationSound.Silent,
            ),
            soundDisplayNameResolver = FakeSoundDisplayNameResolver(
                resolveLambda = { uri ->
                    resolverCalls += uri
                    "Should not appear"
                },
            ),
        )
        presenter.test {
            // The synchronous branches (SystemDefault / Silent) feed the label directly without
            // routing through produceState — so the resolver is never invoked, and the label
            // never flashes from the default to itself.
            consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()
            assertThat(resolverCalls).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - resolves Custom display name via SoundDisplayNameResolver`() = runTest {
        val resolverCalls = mutableListOf<String>()
        val presenter = createNotificationSettingsPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(messageSound = NotificationSound.Custom("content://media/42")),
            soundDisplayNameResolver = FakeSoundDisplayNameResolver(
                resolveLambda = { uri ->
                    resolverCalls += uri
                    "Pixel notification"
                },
            ),
        )
        presenter.test {
            val state = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid &&
                    it.messageSound.displayName == "Pixel notification"
            }.last()
            assertThat(state.messageSound.displayName).isEqualTo("Pixel notification")
            assertThat(resolverCalls).contains("content://media/42")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - Custom display name falls back to localized Custom string when no persisted title and resolver returns null`() = runTest {
        val resolverCalls = mutableListOf<String>()
        val presenter = createNotificationSettingsPresenter(
            // No persisted display name (legacy data path). Resolver returns null. Final fallback
            // is the localised string resource (FakeStringProvider returns "A string" for any
            // resource id) — never the default label, so the row is not mislabelled as "Default".
            appPreferencesStore = InMemoryAppPreferencesStore(messageSound = NotificationSound.Custom("content://gone")),
            soundDisplayNameResolver = FakeSoundDisplayNameResolver(
                resolveLambda = { uri ->
                    resolverCalls += uri
                    null
                },
            ),
        )
        presenter.test {
            val state = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid &&
                    it.messageSound.sound is NotificationSound.Custom &&
                    it.messageSound.displayName == "A string"
            }.last()
            assertThat(state.messageSound.displayName).isEqualTo("A string")
            assertThat(resolverCalls).contains("content://gone")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - empty DataStore surfaces channel-derived Custom sound in the row`() = runTest {
        // Legacy upgrade path: user customised the noisy channel via Android system settings on a
        // prior version. DataStore has no in-app pick (defaults to SystemDefault). The presenter
        // reads the live channel sound, classifies it as Custom, and the resolver supplies the title.
        val resolverCalls = mutableListOf<String>()
        val channelUri = "content://media/internal/audio/media/42"
        val presenter = createNotificationSettingsPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(messageSound = NotificationSound.SystemDefault),
            notificationSoundUpdater = FakeNotificationSoundUpdater(
                readNoisyChannelSoundLambda = { NotificationSound.Custom(channelUri) },
            ),
            soundDisplayNameResolver = FakeSoundDisplayNameResolver(
                resolveLambda = { uri ->
                    resolverCalls += uri
                    "Argon"
                },
            ),
        )
        presenter.test {
            val state = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid &&
                    it.messageSound.sound == NotificationSound.Custom(channelUri) &&
                    it.messageSound.displayName == "Argon"
            }.last()
            assertThat(state.messageSound.sound).isEqualTo(NotificationSound.Custom(channelUri))
            assertThat(state.messageSound.displayName).isEqualTo("Argon")
            assertThat(resolverCalls).contains(channelUri)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - empty DataStore surfaces channel-derived Silent in the row`() = runTest {
        // User set the noisy channel to Silent via Android system settings. Channel read returns
        // Silent; the row labels it accordingly without a resolver call.
        val presenter = createNotificationSettingsPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(messageSound = NotificationSound.SystemDefault),
            notificationSoundUpdater = FakeNotificationSoundUpdater(
                readNoisyChannelSoundLambda = { NotificationSound.Silent },
            ),
        )
        presenter.test {
            val state = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid &&
                    it.messageSound.sound == NotificationSound.Silent
            }.last()
            assertThat(state.messageSound.sound).isEqualTo(NotificationSound.Silent)
            // FakeStringProvider returns "A string" for any resource, including the Silent label.
            assertThat(state.messageSound.displayName).isEqualTo("A string")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - persisted in-app pick takes precedence over channel read`() = runTest {
        // When DataStore holds a real in-app pick, the channel read must not run. Persisted
        // value is what the user explicitly chose; channel state is only a fallback.
        var channelReadCalls = 0
        val presenter = createNotificationSettingsPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(
                messageSound = NotificationSound.Custom("content://persisted"),
                messageSoundDisplayName = "Persisted Tone",
            ),
            notificationSoundUpdater = FakeNotificationSoundUpdater(
                readNoisyChannelSoundLambda = {
                    channelReadCalls++
                    NotificationSound.Custom("content://channel/should-not-appear")
                },
            ),
        )
        presenter.test {
            val state = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid &&
                    it.messageSound.displayName == "Persisted Tone"
            }.last()
            assertThat(state.messageSound.sound).isEqualTo(NotificationSound.Custom("content://persisted"))
            assertThat(channelReadCalls).isEqualTo(0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - persisted display name takes precedence over live resolver probe`() = runTest {
        val resolverCalls = mutableListOf<String>()
        val presenter = createNotificationSettingsPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(
                messageSound = NotificationSound.Custom("content://media/42"),
                messageSoundDisplayName = "Captured at copy time",
            ),
            soundDisplayNameResolver = FakeSoundDisplayNameResolver(
                resolveLambda = { uri ->
                    resolverCalls += uri
                    "Live probe (should not appear)"
                },
            ),
        )
        presenter.test {
            val state = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid &&
                    it.messageSound.displayName == "Captured at copy time"
            }.last()
            assertThat(state.messageSound.displayName).isEqualTo("Captured at copy time")
            // Live resolver must not be invoked when a persisted title is available.
            assertThat(resolverCalls).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - SetMessageSound with Custom copies via copier and persists the FileProvider URI`() = runTest {
        val recreateRecorder = lambdaRecorder<NotificationSound, Int, Unit> { _, _ -> }
        val appPreferencesStore = InMemoryAppPreferencesStore()
        val presenter = createNotificationSettingsPresenter(
            appPreferencesStore = appPreferencesStore,
            notificationSoundUpdater = FakeNotificationSoundUpdater(
                recreateNoisyChannelLambda = { sound, version -> recreateRecorder(sound, version) },
            ),
            notificationSoundCopier = FakeNotificationSoundCopier { _, _ ->
                NotificationSoundCopier.CopyResult.Success(
                    fileProviderUriString = "content://my.app.fileprovider/notification_sounds/message_sound.ogg",
                    displayName = "Cool Tone",
                )
            },
        )
        presenter.test {
            val initialState = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()
            initialState.eventSink(NotificationSettingsEvents.SetMessageSound(NotificationSound.Custom("content://media/source")))
            advanceUntilIdle()

            val expectedSound = NotificationSound.Custom("content://my.app.fileprovider/notification_sounds/message_sound.ogg")
            assertThat(appPreferencesStore.getNotificationSoundChannelConfig().messageSound).isEqualTo(expectedSound)
            assertThat(appPreferencesStore.getMessageSoundDisplayNameFlow().first()).isEqualTo("Cool Tone")
            recreateRecorder.assertions().isCalledOnce().with(value(expectedSound), value(1))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - SetMessageSound copy failure surfaces inline error and leaves prior choice intact`() = runTest {
        val priorChoice = NotificationSound.Custom("content://prior")
        val appPreferencesStore = InMemoryAppPreferencesStore(messageSound = priorChoice)
        val presenter = createNotificationSettingsPresenter(
            appPreferencesStore = appPreferencesStore,
            notificationSoundCopier = FakeNotificationSoundCopier { _, _ ->
                NotificationSoundCopier.CopyResult.UnplayableSource
            },
        )
        presenter.test {
            val initialState = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()
            initialState.eventSink(NotificationSettingsEvents.SetMessageSound(NotificationSound.Custom("content://broken")))

            val errorState = consumeItemsUntilPredicate { it.messageSound.copyError }.last()
            assertThat(errorState.messageSound.copyError).isTrue()
            // Persisted state is unchanged.
            assertThat(appPreferencesStore.getNotificationSoundChannelConfig().messageSound).isEqualTo(priorChoice)

            // Dismissing the inline error clears the flag without altering persistence.
            errorState.eventSink(NotificationSettingsEvents.DismissMessageSoundCopyError)
            val cleared = consumeItemsUntilPredicate { !it.messageSound.copyError }.last()
            assertThat(cleared.messageSound.copyError).isFalse()
            assertThat(appPreferencesStore.getNotificationSoundChannelConfig().messageSound).isEqualTo(priorChoice)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - SetCallRingtone copy failure surfaces inline error and leaves prior choice intact`() = runTest {
        val priorChoice = NotificationSound.Custom("content://prior")
        val appPreferencesStore = InMemoryAppPreferencesStore(callRingtone = priorChoice)
        val presenter = createNotificationSettingsPresenter(
            appPreferencesStore = appPreferencesStore,
            notificationSoundCopier = FakeNotificationSoundCopier { _, _ ->
                NotificationSoundCopier.CopyResult.UnplayableSource
            },
        )
        presenter.test {
            val initialState = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()
            initialState.eventSink(NotificationSettingsEvents.SetCallRingtone(NotificationSound.Custom("content://broken")))

            val errorState = consumeItemsUntilPredicate { it.callRingtone.copyError }.last()
            assertThat(errorState.callRingtone.copyError).isTrue()
            // Persisted state is unchanged.
            assertThat(appPreferencesStore.getNotificationSoundChannelConfig().callRingtone).isEqualTo(priorChoice)

            // Dismissing the inline error clears the flag without altering persistence.
            errorState.eventSink(NotificationSettingsEvents.DismissCallRingtoneCopyError)
            val cleared = consumeItemsUntilPredicate { !it.callRingtone.copyError }.last()
            assertThat(cleared.callRingtone.copyError).isFalse()
            assertThat(appPreferencesStore.getNotificationSoundChannelConfig().callRingtone).isEqualTo(priorChoice)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - SystemDefault and Silent picks bypass the copier`() = runTest {
        val copierCalls = mutableListOf<String>()
        val presenter = createNotificationSettingsPresenter(
            notificationSoundUpdater = FakeNotificationSoundUpdater(
                recreateNoisyChannelLambda = { _, _ -> },
                recreateRingingCallChannelLambda = { _, _ -> },
            ),
            notificationSoundCopier = FakeNotificationSoundCopier { source, _ ->
                copierCalls += source
                NotificationSoundCopier.CopyResult.Success(source, "x")
            },
        )
        presenter.test {
            val initialState = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()
            initialState.eventSink(NotificationSettingsEvents.SetMessageSound(NotificationSound.SystemDefault))
            initialState.eventSink(NotificationSettingsEvents.SetCallRingtone(NotificationSound.Silent))
            advanceUntilIdle()
            assertThat(copierCalls).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - non-Custom picks delete the stored slot file`() = runTest {
        val deleteCalls = mutableListOf<NotificationSoundCopier.SoundSlot>()
        val presenter = createNotificationSettingsPresenter(
            notificationSoundUpdater = FakeNotificationSoundUpdater(
                recreateNoisyChannelLambda = { _, _ -> },
                recreateRingingCallChannelLambda = { _, _ -> },
            ),
            notificationSoundCopier = FakeNotificationSoundCopier(
                copyLambda = { _, _ -> NotificationSoundCopier.CopyResult.Failure(IllegalStateException("not expected")) },
                deleteStoredSoundForLambda = { slot -> deleteCalls += slot },
            ),
        )
        presenter.test {
            val initialState = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()
            initialState.eventSink(NotificationSettingsEvents.SetMessageSound(NotificationSound.SystemDefault))
            initialState.eventSink(NotificationSettingsEvents.SetCallRingtone(NotificationSound.Silent))
            advanceUntilIdle()
            assertThat(deleteCalls).containsExactly(
                NotificationSoundCopier.SoundSlot.Message,
                NotificationSoundCopier.SoundSlot.Call,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - Custom pick does not invoke deleteStoredSoundFor (copier sweeps inline)`() = runTest {
        val deleteCalls = mutableListOf<NotificationSoundCopier.SoundSlot>()
        val presenter = createNotificationSettingsPresenter(
            notificationSoundUpdater = FakeNotificationSoundUpdater(
                recreateNoisyChannelLambda = { _, _ -> },
            ),
            notificationSoundCopier = FakeNotificationSoundCopier(
                copyLambda = { _, _ ->
                    NotificationSoundCopier.CopyResult.Success(
                        fileProviderUriString = "content://my.app.fileprovider/notification_sounds/message_sound.mp3",
                        displayName = "New Tone",
                    )
                },
                deleteStoredSoundForLambda = { slot -> deleteCalls += slot },
            ),
        )
        presenter.test {
            val initialState = consumeItemsUntilPredicate {
                it.matrixSettings is NotificationSettingsState.MatrixSettings.Valid
            }.last()
            initialState.eventSink(NotificationSettingsEvents.SetMessageSound(NotificationSound.Custom("content://media/source")))
            advanceUntilIdle()
            assertThat(deleteCalls).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun TestScope.createNotificationSettingsPresenter(
        notificationSettingsService: FakeNotificationSettingsService = FakeNotificationSettingsService(),
        pushService: PushService = FakePushService(),
        fullScreenIntentPermissionsStateLambda: () -> FullScreenIntentPermissionsState = { aFullScreenIntentPermissionsState() },
        appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
        notificationSoundUpdater: NotificationSoundUpdater = FakeNotificationSoundUpdater(),
        // Default copier passes the picked URI string straight through as the FileProvider URI
        // and uses the URI string as the display name. Tests that exercise copy failures override.
        notificationSoundCopier: NotificationSoundCopier = FakeNotificationSoundCopier { source, _ ->
            NotificationSoundCopier.CopyResult.Success(source, source)
        },
        soundDisplayNameResolver: FakeSoundDisplayNameResolver = FakeSoundDisplayNameResolver(),
    ): NotificationSettingsPresenter {
        val matrixClient = FakeMatrixClient(notificationSettingsService = notificationSettingsService)
        return NotificationSettingsPresenter(
            notificationSettingsService = notificationSettingsService,
            userPushStoreFactory = FakeUserPushStoreFactory(),
            matrixClient = matrixClient,
            pushService = pushService,
            systemNotificationsEnabledProvider = FakeSystemNotificationsEnabledProvider(),
            fullScreenIntentPermissionsPresenter = { fullScreenIntentPermissionsStateLambda() },
            appPreferencesStore = appPreferencesStore,
            notificationSoundUpdater = notificationSoundUpdater,
            notificationSoundCopier = notificationSoundCopier,
            soundDisplayNameResolver = soundDisplayNameResolver,
            stringProvider = FakeStringProvider(),
            sessionCoroutineScope = backgroundScope,
        )
    }
}
