/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.preferences.impl.developer.appsettings.AppDeveloperSettingsState
import io.element.android.features.preferences.impl.developer.appsettings.anAppDeveloperSettingsState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.DeviceId
import kotlinx.collections.immutable.persistentMapOf

open class DeveloperSettingsStateProvider : PreviewParameterProvider<DeveloperSettingsState> {
    override val values: Sequence<DeveloperSettingsState>
        get() = sequenceOf(
            aDeveloperSettingsState(),
            aDeveloperSettingsState(
                clearCacheAction = AsyncAction.Loading
            ),
            aDeveloperSettingsState(
                isEnterpriseBuild = true,
                // Disable the color picker for now, Paparazzi is failing with:
                // java.lang.IllegalArgumentException: Cannot round NaN value.
                //  at kotlin.math.MathKt__MathJVMKt.roundToInt(MathJVM.kt:1210)
                //  at io.mhssn.colorpicker.ext.ColorExtKt.lighten(ColorExt.kt:86)
                //  at io.mhssn.colorpicker.pickers.ClassicColorPickerKt$ClassicColorPicker$1$1.invokeSuspend(ClassicColorPicker.kt:53)
                showColorPicker = false,
            ),
            aDeveloperSettingsState(
                messageSearchIndexStatus = MessageSearchIndexStatus.Idle,
            ),
            aDeveloperSettingsState(
                messageSearchIndexStatus = MessageSearchIndexStatus.RestartNeeded,
            ),
            aDeveloperSettingsState(
                messageSearchIndexStatus = MessageSearchIndexStatus.Paused(roomsDone = 4, roomsTotal = 9),
            ),
            aDeveloperSettingsState(
                messageSearchIndexStatus = MessageSearchIndexStatus.WaitingForRun,
            ),
            aDeveloperSettingsState(
                messageSearchIndexStatus = MessageSearchIndexStatus.Running(roomsDone = 3, roomsTotal = 9),
            ),
            aDeveloperSettingsState(
                messageSearchIndexStatus = MessageSearchIndexStatus.Running(roomsDone = 0, roomsTotal = 0),
            ),
            aDeveloperSettingsState(
                messageSearchIndexStatus = MessageSearchIndexStatus.Finished(roomsSwept = 9, pagesFetched = 420),
            ),
        )
}

fun aDeveloperSettingsState(
    appDeveloperSettingsState: AppDeveloperSettingsState = anAppDeveloperSettingsState(),
    clearCacheAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    markAllRoomsAsReadAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    isEnterpriseBuild: Boolean = false,
    showColorPicker: Boolean = false,
    messageSearchIndexStatus: MessageSearchIndexStatus = MessageSearchIndexStatus.Hidden,
    deviceId: DeviceId = DeviceId("ILAKNDNASDLK"),
    eventSink: (DeveloperSettingsEvents) -> Unit = {},
) = DeveloperSettingsState(
    appDeveloperSettingsState = appDeveloperSettingsState,
    cacheSize = AsyncData.Success("1.2 MB"),
    databaseSizes = AsyncData.Success(persistentMapOf("state_store" to "1.2MB")),
    clearCacheAction = clearCacheAction,
    markAllRoomsAsReadAction = markAllRoomsAsReadAction,
    isEnterpriseBuild = isEnterpriseBuild,
    showColorPicker = showColorPicker,
    messageSearchIndexStatus = messageSearchIndexStatus,
    deviceId = deviceId,
    eventSink = eventSink,
)
