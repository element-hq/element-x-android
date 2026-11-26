/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.data.bytes
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.paths.SessionPaths
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustUserProfile
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiHomeserverCapabilities
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiSyncService
import io.element.android.libraries.matrix.impl.room.FakeTimelineEventFilterFactory
import io.element.android.libraries.matrix.impl.search.RustSearchBackfillService
import io.element.android.libraries.matrix.impl.search.workmanager.SearchBackfillRequestBuilder
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_DEVICE_ID
import io.element.android.libraries.matrix.test.A_PASSWORD
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.scanner.FakeContentScanner
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.libraries.workmanager.api.WorkManagerRequestWrapper
import io.element.android.libraries.workmanager.test.FakeWorkManagerScheduler
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.lambda.LambdaOneParamRecorder
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientDelegate
import org.matrix.rustcomponents.sdk.CreateRoomParameters
import org.matrix.rustcomponents.sdk.IgnoredUsersListener
import org.matrix.rustcomponents.sdk.MediaSource
import org.matrix.rustcomponents.sdk.RoomHistoryVisibility
import org.matrix.rustcomponents.sdk.StoreSizes
import org.matrix.rustcomponents.sdk.SyncService
import java.io.File

private const val AN_ACCOUNT_DATA_EVENT_TYPE = "org.example.custom"
private const val AN_ACCOUNT_DATA_CONTENT = """{"key":"value"}"""

class RustMatrixClientTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `ensure that sessionId and deviceId can be retrieved from the client`() = runTest {
        createRustMatrixClient().run {
            assertThat(sessionId).isEqualTo(A_USER_ID)
            assertThat(deviceId).isEqualTo(A_DEVICE_ID)
            destroy()
        }
    }

    @Test
    fun `clear cache invokes the method clearCaches from the client and close it`() = runTest {
        val clearCachesResult = lambdaRecorder<Unit> { }
        val closeResult = lambdaRecorder<Unit> { }
        val client = createRustMatrixClient(
            client = FakeFfiClient(
                clearCachesResult = clearCachesResult,
                closeResult = closeResult,
            )
        )
        client.clearCache()
        clearCachesResult.assertions().isCalledOnce()
        closeResult.assertions().isCalledOnce()
        client.destroy()
    }

    @Test
    fun `retrieving the UserProfile updates the database`() = runTest {
        val profilePersisted = CompletableDeferred<Unit>()
        val updateUserProfileResult = lambdaRecorder<String, String?, String?, String?, Unit> { _, _, _, _ -> profilePersisted.complete(Unit) }
        val getMediaThumbnailResult = lambdaRecorder<MediaSource, ULong, ULong, Boolean, ByteArray> { _, _, _, _ -> byteArrayOf(1, 2, 3) }
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(
                    sessionId = A_USER_ID.value,
                    userDisplayName = null,
                    userAvatarUrl = null,
                )
            ),
            updateUserProfileResult = updateUserProfileResult,
        )
        val client = createRustMatrixClient(
            client = FakeFfiClient(
                getProfileResult = { userId -> aRustUserProfile(userId = userId, displayName = A_USER_NAME, avatarUrl = AN_AVATAR_URL) },
                getMediaThumbnailResult = getMediaThumbnailResult,
            ),
            sessionStore = sessionStore,
        )
        profilePersisted.await()
        getMediaThumbnailResult.assertions().isCalledOnce()
            .with(any(), value(240uL), value(240uL), value(false))
        updateUserProfileResult.assertions().isCalledOnce()
            .with(
                value(A_USER_ID.value),
                value(A_USER_NAME),
                value(AN_AVATAR_URL),
                value("AQID"),
            )
        client.destroy()
    }

    @Test
    fun `retrieving the UserProfile does not download the avatar again if it has not changed`() = runTest {
        val profilePersisted = CompletableDeferred<Unit>()
        val updateUserProfileResult = lambdaRecorder<String, String?, String?, String?, Unit> { _, _, _, _ -> profilePersisted.complete(Unit) }
        val getMediaThumbnailResult = lambdaRecorder<MediaSource, ULong, ULong, Boolean, ByteArray> { _, _, _, _ -> byteArrayOf(1, 2, 3) }
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(
                    sessionId = A_USER_ID.value,
                    userDisplayName = A_USER_NAME,
                    userAvatarUrl = AN_AVATAR_URL,
                    userAvatarData = "storedData",
                )
            ),
            updateUserProfileResult = updateUserProfileResult,
        )
        val client = createRustMatrixClient(
            client = FakeFfiClient(
                getProfileResult = { userId -> aRustUserProfile(userId = userId, displayName = A_USER_NAME, avatarUrl = AN_AVATAR_URL) },
                getMediaThumbnailResult = getMediaThumbnailResult,
            ),
            sessionStore = sessionStore,
        )
        profilePersisted.await()
        getMediaThumbnailResult.assertions().isNeverCalled()
        updateUserProfileResult.assertions().isCalledOnce()
            .with(
                value(A_USER_ID.value),
                value(A_USER_NAME),
                value(AN_AVATAR_URL),
                value("storedData"),
            )
        client.destroy()
    }

    @Test
    fun `retrieving the UserProfile downloads the avatar again if the previous download failed`() = runTest {
        val profilePersisted = CompletableDeferred<Unit>()
        val updateUserProfileResult = lambdaRecorder<String, String?, String?, String?, Unit> { _, _, _, _ -> profilePersisted.complete(Unit) }
        val getMediaThumbnailResult = lambdaRecorder<MediaSource, ULong, ULong, Boolean, ByteArray> { _, _, _, _ -> byteArrayOf(1, 2, 3) }
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(
                    sessionId = A_USER_ID.value,
                    userDisplayName = A_USER_NAME,
                    userAvatarUrl = AN_AVATAR_URL,
                    userAvatarData = null,
                )
            ),
            updateUserProfileResult = updateUserProfileResult,
        )
        val client = createRustMatrixClient(
            client = FakeFfiClient(
                getProfileResult = { userId -> aRustUserProfile(userId = userId, displayName = A_USER_NAME, avatarUrl = AN_AVATAR_URL) },
                getMediaThumbnailResult = getMediaThumbnailResult,
            ),
            sessionStore = sessionStore,
        )
        profilePersisted.await()
        getMediaThumbnailResult.assertions().isCalledOnce()
        updateUserProfileResult.assertions().isCalledOnce()
            .with(
                value(A_USER_ID.value),
                value(A_USER_NAME),
                value(AN_AVATAR_URL),
                value("AQID"),
            )
        client.destroy()
    }

    @Test
    fun `retrieving the UserProfile clears the avatar data if the avatar download fails`() = runTest {
        val profilePersisted = CompletableDeferred<Unit>()
        val updateUserProfileResult = lambdaRecorder<String, String?, String?, String?, Unit> { _, _, _, _ -> profilePersisted.complete(Unit) }
        val getMediaThumbnailResult = lambdaRecorder<MediaSource, ULong, ULong, Boolean, ByteArray> { _, _, _, _ -> error("Download failed") }
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(
                    sessionId = A_USER_ID.value,
                    userDisplayName = A_USER_NAME,
                    userAvatarUrl = "mxc://server.org/previousAvatar",
                    userAvatarData = "previousData",
                )
            ),
            updateUserProfileResult = updateUserProfileResult,
        )
        val client = createRustMatrixClient(
            client = FakeFfiClient(
                getProfileResult = { userId -> aRustUserProfile(userId = userId, displayName = A_USER_NAME, avatarUrl = AN_AVATAR_URL) },
                getMediaThumbnailResult = getMediaThumbnailResult,
            ),
            sessionStore = sessionStore,
        )
        profilePersisted.await()
        getMediaThumbnailResult.assertions().isCalledOnce()
        updateUserProfileResult.assertions().isCalledOnce()
            .with(
                value(A_USER_ID.value),
                value(A_USER_NAME),
                value(AN_AVATAR_URL),
                value(null),
            )
        client.destroy()
    }

    @Test
    fun `retrieving the UserProfile without avatar clears the avatar data`() = runTest {
        val profilePersisted = CompletableDeferred<Unit>()
        val updateUserProfileResult = lambdaRecorder<String, String?, String?, String?, Unit> { _, _, _, _ -> profilePersisted.complete(Unit) }
        val getMediaThumbnailResult = lambdaRecorder<MediaSource, ULong, ULong, Boolean, ByteArray> { _, _, _, _ -> byteArrayOf(1, 2, 3) }
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(
                    sessionId = A_USER_ID.value,
                    userDisplayName = A_USER_NAME,
                    userAvatarUrl = AN_AVATAR_URL,
                    userAvatarData = "storedData",
                )
            ),
            updateUserProfileResult = updateUserProfileResult,
        )
        val client = createRustMatrixClient(
            client = FakeFfiClient(
                getProfileResult = { userId -> aRustUserProfile(userId = userId, displayName = A_USER_NAME, avatarUrl = null) },
                getMediaThumbnailResult = getMediaThumbnailResult,
            ),
            sessionStore = sessionStore,
        )
        profilePersisted.await()
        getMediaThumbnailResult.assertions().isNeverCalled()
        updateUserProfileResult.assertions().isCalledOnce()
            .with(
                value(A_USER_ID.value),
                value(A_USER_NAME),
                value(null),
                value(null),
            )
        client.destroy()
    }

    @Test
    fun `getDatabaseSizes returns the database sizes`() = runTest {
        val client = createRustMatrixClient(
            client = FakeFfiClient(getStoreSizesResult = {
                StoreSizes(
                    cryptoStore = null,
                    stateStore = 10uL,
                    eventCacheStore = 11uL,
                    mediaStore = 12uL,
                    searchIndex = 13uL,
                )
            })
        )

        client.getDatabaseSizes().getOrThrow().run {
            assertThat(cryptoStore).isNull()
            assertThat(stateStore).isEqualTo(10.bytes)
            assertThat(eventCacheStore).isEqualTo(11.bytes)
            assertThat(mediaStore).isEqualTo(12.bytes)
            assertThat(searchIndex).isEqualTo(13.bytes)
        }
    }

    @Test
    fun `createDM overrides room history visibility to invited`() = runTest {
        var createParameters: CreateRoomParameters? = null
        val createRoomLambda = lambdaRecorder<CreateRoomParameters, String> {
            createParameters = it
            A_ROOM_ID.value
        }
        val client = createRustMatrixClient(
            client = FakeFfiClient(createRoomResult = createRoomLambda)
        )

        client.createDM(userId = A_USER_ID, isEncrypted = true)

        createRoomLambda.assertions().isCalledOnce()
        assertThat(createParameters?.historyVisibilityOverride).isEqualTo(RoomHistoryVisibility.Invited)
    }

    @Test
    fun `getAccountData returns the raw content provided by the client`() = runTest {
        val accountDataResult = lambdaRecorder<String, String?> { AN_ACCOUNT_DATA_CONTENT }
        val client = createRustMatrixClient(
            client = FakeFfiClient(accountDataResult = accountDataResult)
        )

        assertThat(client.getAccountData(AN_ACCOUNT_DATA_EVENT_TYPE).getOrThrow()).isEqualTo(AN_ACCOUNT_DATA_CONTENT)
        accountDataResult.assertions().isCalledOnce().with(value(AN_ACCOUNT_DATA_EVENT_TYPE))
        client.destroy()
    }

    @Test
    fun `setAccountData forwards the event type and the raw content to the client`() = runTest {
        val setAccountDataResult = lambdaRecorder<String, String, Unit> { _, _ -> }
        val client = createRustMatrixClient(
            client = FakeFfiClient(setAccountDataResult = setAccountDataResult)
        )

        assertThat(client.setAccountData(AN_ACCOUNT_DATA_EVENT_TYPE, AN_ACCOUNT_DATA_CONTENT).isSuccess).isTrue()
        setAccountDataResult.assertions().isCalledOnce().with(value(AN_ACCOUNT_DATA_EVENT_TYPE), value(AN_ACCOUNT_DATA_CONTENT))
        client.destroy()
    }

    @Test
    fun `ignoredUsersFlow uses the value from the server even if the SDK first emits an empty list`() = runTest {
        val client = createRustMatrixClient(
            client = FakeFfiClient(
                ignoredUsersResult = { listOf(A_USER_ID.value, A_USER_ID_2.value) },
                // The SDK emits its in-memory value immediately, which is empty until the account data is received by the sync
                subscribeToIgnoredUsersResult = { listener -> listener.call(emptyList()) },
            )
        )
        runInitialIgnoredUsersFetch()
        assertThat(client.ignoredUsersFlow.value).containsExactly(A_USER_ID, A_USER_ID_2).inOrder()
        client.destroy()
    }

    @Test
    fun `ignoredUsersFlow emits the values received from the SDK listener`() = runTest {
        var ignoredUsersListener: IgnoredUsersListener? = null
        val client = createRustMatrixClient(
            client = FakeFfiClient(
                subscribeToIgnoredUsersResult = { listener -> ignoredUsersListener = listener },
            )
        )
        runInitialIgnoredUsersFetch()
        assertThat(client.ignoredUsersFlow.value).isEmpty()
        ignoredUsersListener!!.call(listOf(A_USER_ID.value))
        assertThat(client.ignoredUsersFlow.value).containsExactly(A_USER_ID)
        client.destroy()
    }

    @Test
    fun `ignoredUsersFlow still emits the values received from the SDK listener if the initial fetch fails`() = runTest {
        var ignoredUsersListener: IgnoredUsersListener? = null
        val client = createRustMatrixClient(
            client = FakeFfiClient(
                ignoredUsersResult = { error("Network error") },
                subscribeToIgnoredUsersResult = { listener -> ignoredUsersListener = listener },
            )
        )
        runInitialIgnoredUsersFetch()
        assertThat(client.ignoredUsersFlow.value).isEmpty()
        ignoredUsersListener!!.call(listOf(A_USER_ID.value))
        assertThat(client.ignoredUsersFlow.value).containsExactly(A_USER_ID)
        client.destroy()
    }

    @Test
    fun `homeserverCapabilities returns a single instance which is destroyed with the client`() = runTest {
        val destroyResult = lambdaRecorder<Unit> { }
        val client = createRustMatrixClient(
            client = FakeFfiClient(homeserverCapabilities = FakeFfiHomeserverCapabilities(destroy = destroyResult)),
        )
        assertThat(client.homeserverCapabilities).isSameInstanceAs(client.homeserverCapabilities)
        destroyResult.assertions().isNeverCalled()
        client.destroy()
        destroyResult.assertions().isCalledOnce()
    }

    @Test
    fun `when logout succeeds, the sync is stopped and the session is removed`() = runTest {
        val events = mutableListOf<String>()
        val sessionStore = aSessionStoreWithCurrentSession()
        val sessionPaths = createSessionPaths()
        val client = createRustMatrixClient(
            client = FakeFfiClient(logoutResult = { events.add("logout") }),
            innerSyncService = aRecordingSyncService(events),
            sessionStore = sessionStore,
            sessionPaths = sessionPaths,
        )
        client.logout(userInitiated = true, ignoreSdkError = false)
        assertThat(events.take(2)).containsExactly("stop", "logout").inOrder()
        assertThat(events).doesNotContain("start")
        assertThat(client.isShuttingDown).isTrue()
        assertThat(sessionStore.getSession(A_USER_ID.value)).isNull()
        assertThat(sessionPaths.fileDirectory.exists()).isFalse()
        assertThat(sessionPaths.cacheDirectory.exists()).isFalse()
    }

    @Test
    fun `when logout fails and the SDK error is ignored, the session is still removed`() = runTest {
        val events = mutableListOf<String>()
        val sessionStore = aSessionStoreWithCurrentSession()
        val client = createRustMatrixClient(
            client = FakeFfiClient(logoutResult = { error("Logout failed") }),
            innerSyncService = aRecordingSyncService(events),
            sessionStore = sessionStore,
            sessionPaths = createSessionPaths(),
        )
        client.logout(userInitiated = true, ignoreSdkError = true)
        assertThat(events).doesNotContain("start")
        assertThat(client.isShuttingDown).isTrue()
        assertThat(sessionStore.getSession(A_USER_ID.value)).isNull()
    }

    @Test
    fun `when logout fails, the session is restored and still usable`() = runTest {
        val setDelegateResult = lambdaRecorder<ClientDelegate?, Unit> { }
        val events = mutableListOf<String>()
        val sessionStore = aSessionStoreWithCurrentSession()
        val client = createRustMatrixClient(
            client = FakeFfiClient(
                setDelegateResult = setDelegateResult,
                logoutResult = {
                    events.add("logout")
                    error("Logout failed")
                },
            ),
            innerSyncService = aRecordingSyncService(events),
            sessionStore = sessionStore,
        )
        val result = runCatchingExceptions { client.logout(userInitiated = true, ignoreSdkError = false) }
        assertThat(result.isFailure).isTrue()
        client.assertSessionRestored(setDelegateResult, events, "logout")
        assertThat(sessionStore.getSession(A_USER_ID.value)).isNotNull()
        client.destroy()
    }

    @Test
    fun `when logout is cancelled, the session is restored and still usable`() = runTest {
        val setDelegateResult = lambdaRecorder<ClientDelegate?, Unit> { }
        val events = mutableListOf<String>()
        val client = createRustMatrixClient(
            client = FakeFfiClient(
                setDelegateResult = setDelegateResult,
                logoutResult = {
                    events.add("logout")
                    throw CancellationException("Cancelled")
                },
            ),
            innerSyncService = aRecordingSyncService(events),
        )
        val error = catchCancellation { client.logout(userInitiated = true, ignoreSdkError = false) }
        assertThat(error).isNotNull()
        client.assertSessionRestored(setDelegateResult, events, "logout")
        client.destroy()
    }

    @Test
    fun `when account deactivation succeeds, the sync is stopped and the session is removed`() = runTest {
        val events = mutableListOf<String>()
        val sessionStore = aSessionStoreWithCurrentSession()
        val sessionPaths = createSessionPaths()
        val deactivateAccountResult = lambdaRecorder<String, Boolean, Unit> { _, _ -> events.add("deactivate") }
        val client = createRustMatrixClient(
            client = FakeFfiClient(deactivateAccountResult = deactivateAccountResult),
            innerSyncService = aRecordingSyncService(events),
            sessionStore = sessionStore,
            sessionPaths = sessionPaths,
        )
        val result = client.deactivateAccount(password = A_PASSWORD, eraseData = true)
        assertThat(result.isSuccess).isTrue()
        deactivateAccountResult.assertions().isCalledOnce().with(value(A_PASSWORD), value(true))
        assertThat(events.take(2)).containsExactly("stop", "deactivate").inOrder()
        assertThat(events).doesNotContain("start")
        assertThat(client.isShuttingDown).isTrue()
        assertThat(sessionStore.getSession(A_USER_ID.value)).isNull()
        assertThat(sessionPaths.fileDirectory.exists()).isFalse()
        assertThat(sessionPaths.cacheDirectory.exists()).isFalse()
    }

    @Test
    fun `when account deactivation fails, the session is restored and still usable`() = runTest {
        val setDelegateResult = lambdaRecorder<ClientDelegate?, Unit> { }
        val events = mutableListOf<String>()
        val sessionStore = aSessionStoreWithCurrentSession()
        val client = createRustMatrixClient(
            client = FakeFfiClient(
                setDelegateResult = setDelegateResult,
                deactivateAccountResult = { _, _ ->
                    events.add("deactivate")
                    error("Wrong password")
                },
            ),
            innerSyncService = aRecordingSyncService(events),
            sessionStore = sessionStore,
        )
        val result = client.deactivateAccount(password = A_PASSWORD, eraseData = false)
        assertThat(result.exceptionOrNull()).hasMessageThat().isEqualTo("Wrong password")
        client.assertSessionRestored(setDelegateResult, events, "deactivate")
        assertThat(sessionStore.getSession(A_USER_ID.value)).isNotNull()
        client.destroy()
    }

    @Test
    fun `when account deactivation is cancelled, the session is restored and still usable`() = runTest {
        val setDelegateResult = lambdaRecorder<ClientDelegate?, Unit> { }
        val events = mutableListOf<String>()
        val client = createRustMatrixClient(
            client = FakeFfiClient(
                setDelegateResult = setDelegateResult,
                deactivateAccountResult = { _, _ ->
                    events.add("deactivate")
                    throw CancellationException("Cancelled")
                },
            ),
            innerSyncService = aRecordingSyncService(events),
        )
        val error = catchCancellation { client.deactivateAccount(password = A_PASSWORD, eraseData = false) }
        assertThat(error).isNotNull()
        client.assertSessionRestored(setDelegateResult, events, "deactivate")
        client.destroy()
    }

    private fun RustMatrixClient.assertSessionRestored(
        setDelegateResult: LambdaOneParamRecorder<ClientDelegate?, Unit>,
        events: List<String>,
        request: String,
    ) {
        assertThat(isShuttingDown).isFalse()
        // The delegate is set when creating the client, then restored after the failure
        setDelegateResult.assertions().isCalledExactly(2).withSequence(
            listOf(any()),
            listOf(any()),
        )
        // The sync is stopped during the request, then restarted
        assertThat(events).containsExactly("stop", request, "start").inOrder()
    }

    private fun aRecordingSyncService(events: MutableList<String>) = FakeFfiSyncService(
        start = { events.add("start") },
        stop = { events.add("stop") },
    )

    private fun aSessionStoreWithCurrentSession() = InMemorySessionStore(
        initialList = listOf(aSessionData(sessionId = A_USER_ID.value)),
        updateUserProfileResult = { _, _, _, _ -> },
    )

    private fun createSessionPaths() = SessionPaths(
        fileDirectory = temporaryFolder.newFolder("files"),
        cacheDirectory = temporaryFolder.newFolder("cache"),
    )

    private suspend fun catchCancellation(block: suspend () -> Unit): CancellationException? {
        return try {
            block()
            null
        } catch (e: CancellationException) {
            e
        }
    }

    // The initial fetch of the ignored users runs in the background scope, with a simulated delay
    private fun TestScope.runInitialIgnoredUsersFetch() {
        advanceTimeBy(1)
        runCurrent()
    }

    private fun TestScope.createRustMatrixClient(
        client: Client = FakeFfiClient(),
        innerSyncService: SyncService = FakeFfiSyncService(),
        sessionStore: SessionStore = InMemorySessionStore(
            updateUserProfileResult = { _, _, _, _ -> },
        ),
        sessionPaths: SessionPaths = SessionPaths(fileDirectory = File("files"), cacheDirectory = File("cache")),
    ) = RustMatrixClient(
        innerClient = client,
        sessionPaths = sessionPaths,
        sessionStore = sessionStore,
        appCoroutineScope = backgroundScope,
        sessionDelegate = aRustClientSessionDelegate(
            sessionStore = sessionStore,
        ),
        innerSyncService = innerSyncService,
        dispatchers = testCoroutineDispatchers(),
        baseCacheDirectory = File(""),
        clock = FakeSystemClock(),
        timelineEventFilterFactory = FakeTimelineEventFilterFactory(),
        featureFlagService = FakeFeatureFlagService(),
        analyticsService = FakeAnalyticsService(),
        workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}, cancelLambda = { _, _ -> }),
        contentScanner = FakeContentScanner(),
        isMessageSearchAvailable = false,
        searchBackfillServiceFactory = object : RustSearchBackfillService.Factory {
            override fun create(innerClient: Client) = RustSearchBackfillService(
                innerClient = innerClient,
                searchBackfillRequestBuilderFactory = {
                    object : SearchBackfillRequestBuilder {
                        override suspend fun build(): Result<List<WorkManagerRequestWrapper>> = Result.success(emptyList())
                    }
                },
                workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}, cancelLambda = { _, _ -> }),
            )
        },
    )
}
