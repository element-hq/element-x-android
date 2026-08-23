/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import dev.zacsweers.metro.Inject
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.preferences.impl.developer.appsettings.AppDeveloperSettingsState
import io.element.android.features.preferences.impl.tasks.ClearCacheUseCase
import io.element.android.features.preferences.impl.tasks.ComputeCacheSizeUseCase
import io.element.android.features.preferences.impl.tasks.MarkAllRoomsAsRead
import io.element.android.features.preferences.impl.tasks.VacuumStoresUseCase
import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.data.ByteUnit
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.analytics.GetDatabaseSizesUseCase
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.search.MessageSearchIndexer
import io.element.android.libraries.matrix.api.search.MessageSearchSweepActivity
import io.element.android.libraries.matrix.api.search.SearchBackfillCursor
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class DeveloperSettingsPresenter(
    private val appDeveloperSettingsPresenter: Presenter<AppDeveloperSettingsState>,
    private val sessionId: SessionId,
    private val deviceId: DeviceId,
    private val computeCacheSizeUseCase: ComputeCacheSizeUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
    private val enterpriseService: EnterpriseService,
    private val vacuumStoresUseCase: VacuumStoresUseCase,
    private val databaseSizesUseCase: GetDatabaseSizesUseCase,
    private val fileSizeFormatter: FileSizeFormatter,
    private val markAllRoomsAsRead: MarkAllRoomsAsRead,
    private val featureFlagService: FeatureFlagService,
    private val matrixClient: MatrixClient,
    private val messageSearchIndexer: MessageSearchIndexer,
    private val buildMeta: BuildMeta,
) : Presenter<DeveloperSettingsState> {
    @Composable
    override fun present(): DeveloperSettingsState {
        val cacheSize = remember {
            mutableStateOf<AsyncData<String>>(AsyncData.Uninitialized)
        }
        val databaseSizes = remember {
            mutableStateOf<AsyncData<ImmutableMap<String, String>>>(AsyncData.Uninitialized)
        }
        val clearCacheAction = remember {
            mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
        }
        val markAllRoomsAsReadAction = remember {
            mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
        }
        var showColorPicker by remember {
            mutableStateOf(false)
        }
        LaunchedEffect(Unit) {
            computeDatabaseSizes(databaseSizes)
        }
        val coroutineScope = rememberCoroutineScope()
        // Compute cache size each time the clear cache action value is changed
        LaunchedEffect(clearCacheAction.value.isSuccess()) {
            computeCacheSize(cacheSize)
        }

        val isMessageSearchFlagEnabled by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.MessageSearch)
        }.collectAsState(initial = false)
        val sweepActivity by remember {
            messageSearchIndexer.userSweepActivityFlow(sessionId)
        }.collectAsState(initial = MessageSearchSweepActivity.NONE)
        val sweepCursor by remember {
            messageSearchIndexer.cursorFlow(sessionId)
        }.collectAsState(initial = null)
        val messageSearchIndexStatus = messageSearchIndexStatus(
            flagEnabled = isMessageSearchFlagEnabled,
            indexAvailable = matrixClient.isMessageSearchAvailable,
            sweepActivity = sweepActivity,
            cursor = sweepCursor,
        )

        fun handleEvent(event: DeveloperSettingsEvents) {
            when (event) {
                DeveloperSettingsEvents.ClearCache -> coroutineScope.clearCache(clearCacheAction)
                is DeveloperSettingsEvents.ChangeBrandColor -> coroutineScope.launch {
                    showColorPicker = false
                    val color = event.color
                        ?.toArgb()
                        ?.toHexString(HexFormat.UpperCase)
                        ?.substring(2, 8)
                        ?.padStart(7, '#')
                    enterpriseService.overrideBrandColor(sessionId, color)
                }
                is DeveloperSettingsEvents.SetShowColorPicker -> {
                    showColorPicker = event.show
                }
                DeveloperSettingsEvents.VacuumStores -> coroutineScope.launch {
                    vacuumStoresUseCase()
                }
                is DeveloperSettingsEvents.MarkAllRoomsAsRead -> {
                    if (event.needsConfirmation) {
                        markAllRoomsAsReadAction.value = AsyncAction.ConfirmingNoParams
                    } else {
                        coroutineScope.markAllRoomsAsRead(
                            markAllRoomsAsReadAction = markAllRoomsAsReadAction,
                        )
                    }
                }
                DeveloperSettingsEvents.DismissMarkAllRoomsAsReadConfirmation -> {
                    markAllRoomsAsReadAction.value = AsyncAction.Uninitialized
                }
                DeveloperSettingsEvents.StartSearchIndexing -> coroutineScope.launch {
                    messageSearchIndexer.startUserInitiatedSweep(sessionId)
                }
                DeveloperSettingsEvents.CancelSearchIndexing -> {
                    messageSearchIndexer.cancelSweep(sessionId)
                }
            }
        }

        val appDeveloperSettingsState = appDeveloperSettingsPresenter.present()
        return DeveloperSettingsState(
            appDeveloperSettingsState = appDeveloperSettingsState,
            cacheSize = cacheSize.value,
            databaseSizes = databaseSizes.value,
            clearCacheAction = clearCacheAction.value,
            markAllRoomsAsReadAction = markAllRoomsAsReadAction.value,
            isEnterpriseBuild = buildMeta.isEnterpriseBuild,
            showColorPicker = showColorPicker,
            messageSearchIndexStatus = messageSearchIndexStatus,
            deviceId = deviceId,
            eventSink = ::handleEvent,
        )
    }

    /**
     * The order of the checks is the trust order of the signals: the flag gates everything, an
     * unattached index makes any action pointless, live WorkManager activity beats whatever the
     * stored cursor says (it may be a stale mid-flight snapshot of the sweep that is about to
     * resume), and only then does the cursor get to describe the past.
     */
    private fun messageSearchIndexStatus(
        flagEnabled: Boolean,
        indexAvailable: Boolean,
        sweepActivity: MessageSearchSweepActivity,
        cursor: SearchBackfillCursor?,
    ): MessageSearchIndexStatus {
        return when {
            !flagEnabled -> MessageSearchIndexStatus.Hidden
            !indexAvailable -> MessageSearchIndexStatus.RestartNeeded
            sweepActivity == MessageSearchSweepActivity.RUNNING -> MessageSearchIndexStatus.Running(
                roomsDone = cursor?.index ?: 0,
                roomsTotal = cursor?.queue?.size ?: 0,
            )
            sweepActivity == MessageSearchSweepActivity.WAITING -> MessageSearchIndexStatus.WaitingForRun
            cursor == null -> MessageSearchIndexStatus.Idle
            !cursor.isDrained -> MessageSearchIndexStatus.Paused(
                roomsDone = cursor.index,
                roomsTotal = cursor.queue.size,
            )
            cursor.queue.isNotEmpty() -> MessageSearchIndexStatus.Finished(
                roomsSwept = cursor.queue.size,
                pagesFetched = cursor.pagesIssued,
            )
            else -> MessageSearchIndexStatus.Idle
        }
    }

    private fun CoroutineScope.computeCacheSize(cacheSize: MutableState<AsyncData<String>>) = launch {
        suspend {
            computeCacheSizeUseCase()
        }.runCatchingUpdatingState(cacheSize)
    }

    private fun CoroutineScope.computeDatabaseSizes(databaseSizes: MutableState<AsyncData<ImmutableMap<String, String>>>) = launch {
        suspend {
            databaseSizesUseCase(sessionId).getOrThrow().let { sizes ->
                buildMap {
                    sizes.stateStore?.let { stateStoreSize ->
                        put("State store", fileSizeFormatter.format(stateStoreSize.into(ByteUnit.BYTES), useShortFormat = true))
                    }
                    sizes.eventCacheStore?.let { eventCacheStoreSize ->
                        put("Event cache store", fileSizeFormatter.format(eventCacheStoreSize.into(ByteUnit.BYTES), useShortFormat = true))
                    }
                    sizes.mediaStore?.let { mediaStoreSize ->
                        put("Media store", fileSizeFormatter.format(mediaStoreSize.into(ByteUnit.BYTES), useShortFormat = true))
                    }
                    sizes.cryptoStore?.let { cryptoStoreSize ->
                        put("Crypto store", fileSizeFormatter.format(cryptoStoreSize.into(ByteUnit.BYTES), useShortFormat = true))
                    }
                }
            }.toImmutableMap()
        }.runCatchingUpdatingState(databaseSizes)
    }

    private fun CoroutineScope.clearCache(clearCacheAction: MutableState<AsyncAction<Unit>>) = launch {
        suspend { clearCacheUseCase() }.runCatchingUpdatingState(state = clearCacheAction)
    }

    private fun CoroutineScope.markAllRoomsAsRead(
        markAllRoomsAsReadAction: MutableState<AsyncAction<Unit>>,
    ) = launch {
        suspend {
            markAllRoomsAsRead().getOrThrow()
        }.runCatchingUpdatingState(state = markAllRoomsAsReadAction)
    }
}
