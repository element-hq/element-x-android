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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
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
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.analytics.GetDatabaseSizesUseCase
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@AssistedInject
class DeveloperSettingsPresenter(
    @Assisted private val navigator: DeveloperSettingsNavigator,
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
    private val buildMeta: BuildMeta,
    private val notificationSettingsService: NotificationSettingsService,
) : Presenter<DeveloperSettingsState> {
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: DeveloperSettingsNavigator): DeveloperSettingsPresenter
    }

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
        val pushRulesAction = remember {
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

        fun handleEvent(event: DeveloperSettingsEvent) {
            when (event) {
                DeveloperSettingsEvent.ClearCache -> coroutineScope.clearCache(clearCacheAction)
                is DeveloperSettingsEvent.ChangeBrandColor -> coroutineScope.launch {
                    showColorPicker = false
                    val color = event.color
                        ?.toArgb()
                        ?.toHexString(HexFormat.UpperCase)
                        ?.substring(2, 8)
                        ?.padStart(7, '#')
                    enterpriseService.overrideBrandColor(sessionId, color)
                }
                is DeveloperSettingsEvent.SetShowColorPicker -> {
                    showColorPicker = event.show
                }
                DeveloperSettingsEvent.VacuumStores -> coroutineScope.launch {
                    vacuumStoresUseCase()
                }
                is DeveloperSettingsEvent.MarkAllRoomsAsRead -> {
                    if (event.needsConfirmation) {
                        markAllRoomsAsReadAction.value = AsyncAction.ConfirmingNoParams
                    } else {
                        coroutineScope.markAllRoomsAsRead(
                            markAllRoomsAsReadAction = markAllRoomsAsReadAction,
                        )
                    }
                }
                DeveloperSettingsEvent.DismissMarkAllRoomsAsReadConfirmation -> {
                    markAllRoomsAsReadAction.value = AsyncAction.Uninitialized
                }
                DeveloperSettingsEvent.OpenPushRules -> coroutineScope.openPushRules(pushRulesAction)
                DeveloperSettingsEvent.DismissPushRulesError -> {
                    pushRulesAction.value = AsyncAction.Uninitialized
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
            pushRulesAction = pushRulesAction.value,
            isEnterpriseBuild = buildMeta.isEnterpriseBuild,
            showColorPicker = showColorPicker,
            deviceId = deviceId,
            eventSink = ::handleEvent,
        )
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

    private fun CoroutineScope.openPushRules(pushRulesAction: MutableState<AsyncAction<Unit>>) = launch {
        pushRulesAction.value = AsyncAction.Loading
        notificationSettingsService.getRawPushRules()
            .onSuccess { content ->
                pushRulesAction.value = AsyncAction.Uninitialized
                navigator.openPushRules(
                    filename = pushRulesFilename(),
                    content = content.orEmpty().prettyPrintJson(),
                )
            }
            .onFailure {
                pushRulesAction.value = AsyncAction.Failure(it)
            }
    }

    /**
     * The user id contains a colon, which is not a valid character for a file name on all the file systems,
     * so replace it by an underscore.
     */
    private fun pushRulesFilename() = "push_rules${sessionId.value.replace(':', '_')}.json"
}

@OptIn(ExperimentalSerializationApi::class)
private val prettyPrintJson = Json {
    prettyPrint = true
    // Keep the indentation small, for a better rendering on mobile.
    prettyPrintIndent = "  "
}

/**
 * Pretty print this json content, so that it is readable when rendered.
 * Return the content as is if it is not valid json.
 */
private fun String.prettyPrintJson(): String = runCatchingExceptions {
    prettyPrintJson.encodeToString(JsonElement.serializer(), prettyPrintJson.parseToJsonElement(this))
}.getOrDefault(this)
