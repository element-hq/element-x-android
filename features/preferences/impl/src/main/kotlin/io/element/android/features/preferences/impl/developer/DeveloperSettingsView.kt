/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.impl.R
import io.element.android.features.preferences.impl.developer.appsettings.AppDeveloperSettingsView
import io.element.android.libraries.androidutils.system.copyToClipboard
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.ui.strings.CommonStrings
import io.mhssn.colorpicker.ColorPickerDialog
import io.mhssn.colorpicker.ColorPickerType

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DeveloperSettingsView(
    state: DeveloperSettingsState,
    onOpenShowkase: () -> Unit,
    onPushHistoryClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    extraOptions: @Composable () -> Unit = {},
) {
    if (state.showLoader) {
        ProgressDialog()
    }
    if (state.markAllRoomsAsReadAction.isConfirming()) {
        ConfirmationDialog(
            title = "Are you sure you want to mark all the rooms as read?",
            content = "",
            submitText = stringResource(CommonStrings.action_yes),
            onSubmitClick = { state.eventSink(DeveloperSettingsEvents.MarkAllRoomsAsRead(needsConfirmation = false)) },
            onDismiss = { state.eventSink(DeveloperSettingsEvents.DismissMarkAllRoomsAsReadConfirmation) },
        )
    }
    BackHandler(
        enabled = !state.showLoader,
        onBack = onBackClick,
    )
    PreferencePage(
        modifier = modifier,
        onBackClick = {
            if (!state.showLoader) {
                onBackClick()
            }
        },
        title = stringResource(id = CommonStrings.common_developer_options)
    ) {
        // Note: this is OK to hardcode strings in this debug screen.
        AppDeveloperSettingsView(
            state = state.appDeveloperSettingsState,
            onOpenShowkase = onOpenShowkase,
            afterFeatureFlags = { MessageSearchIndexCategory(state) },
        )
        SessionCategory(deviceId = state.deviceId)
        NotificationCategory(onPushHistoryClick)
        MarkAllRoomsAsReadCategory(state)

        if (state.isEnterpriseBuild) {
            PreferenceCategory(title = "Theme") {
                ListItem(
                    content = {
                        Text("Change brand color")
                    },
                    onClick = {
                        state.eventSink(DeveloperSettingsEvents.SetShowColorPicker(true))
                    }
                )
                ListItem(
                    content = {
                        Text("Reset brand color")
                    },
                    onClick = {
                        state.eventSink(DeveloperSettingsEvents.ChangeBrandColor(null))
                    }
                )
            }
        }

        extraOptions()

        val cache = state.cacheSize
        PreferenceCategory(title = "Cache") {
            ListItem(
                content = { Text("Database sizes") },
                supportingContent = {
                    if (state.databaseSizes.isLoading()) {
                        Text("Computing...")
                    } else {
                        val dbSizes = state.databaseSizes.dataOrNull()
                        if (dbSizes != null && dbSizes.isNotEmpty()) {
                            Column {
                                for ((dbName, size) in dbSizes) {
                                    Text("$dbName: $size")
                                }
                            }
                        } else {
                            Text("Unknown")
                        }
                    }
                }
            )
            ListItem(
                content = {
                    Text("Vacuum stores")
                },
                onClick = {
                    state.eventSink(DeveloperSettingsEvents.VacuumStores)
                }
            )
            ListItem(
                content = {
                    Text("Clear cache")
                },
                trailingContent = if (state.cacheSize.isLoading() || state.clearCacheAction.isLoading()) {
                    ListItemContent.Custom {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .progressSemantics()
                                .size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    ListItemContent.Text(cache.dataOrNull().orEmpty())
                },
                onClick = {
                    if (state.clearCacheAction.isLoading().not()) {
                        state.eventSink(DeveloperSettingsEvents.ClearCache)
                    }
                }
            )
        }
    }
    ColorPickerDialog(
        show = state.showColorPicker,
        type = ColorPickerType.Classic(
            showAlphaBar = false,
        ),
        onDismissRequest = {
            state.eventSink(DeveloperSettingsEvents.SetShowColorPicker(false))
        },
        onPickedColor = {
            state.eventSink(DeveloperSettingsEvents.ChangeBrandColor(it))
        },
    )
}

/**
 * Sits directly under the feature-flag list so it reads as an extension of the "Message search"
 * toggle that gates it. Wording stays "swept/fetched" on purpose — see [MessageSearchIndexStatus].
 */
@Composable
private fun MessageSearchIndexCategory(state: DeveloperSettingsState) {
    val status = state.messageSearchIndexStatus
    if (status is MessageSearchIndexStatus.Hidden) return
    PreferenceCategory(title = "Message search index") {
        when (status) {
            MessageSearchIndexStatus.Hidden -> Unit
            MessageSearchIndexStatus.RestartNeeded -> ListItem(
                content = { Text("Start indexing") },
                supportingContent = {
                    Text("Restart the app first: the search index is created at startup while the Message search flag is on.")
                },
                enabled = false,
            )
            MessageSearchIndexStatus.Idle -> ListItem(
                content = { Text("Start indexing") },
                supportingContent = {
                    Text("Fetches older history room by room so it becomes searchable. Uses network data and shows progress in a notification.")
                },
                onClick = { state.eventSink(DeveloperSettingsEvents.StartSearchIndexing) },
            )
            is MessageSearchIndexStatus.Paused -> ListItem(
                content = { Text("Resume indexing") },
                supportingContent = {
                    Text("Paused at room ${status.roomsDone} of ${status.roomsTotal}.")
                },
                onClick = { state.eventSink(DeveloperSettingsEvents.StartSearchIndexing) },
            )
            MessageSearchIndexStatus.WaitingForRun -> {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
                // Covers both unmet constraints (no connection) and retry backoff — WorkManager
                // does not say which, so the wording must not claim one.
                SearchIndexSupportingText("Waiting to continue…")
                CancelSearchIndexingItem(state)
            }
            is MessageSearchIndexStatus.Running -> {
                if (status.roomsTotal == 0) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    SearchIndexSupportingText("Preparing…")
                } else {
                    LinearProgressIndicator(
                        progress = { status.roomsDone.toFloat() / status.roomsTotal },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    SearchIndexSupportingText("Indexing room ${status.roomsDone} of ${status.roomsTotal}")
                }
                CancelSearchIndexingItem(state)
            }
            is MessageSearchIndexStatus.Finished -> {
                ListItem(
                    content = { Text("Indexing finished") },
                    supportingContent = {
                        Text(
                            "${status.roomsSwept} rooms swept, ${status.pagesFetched} pages of history fetched. " +
                                "New messages are indexed automatically as they arrive."
                        )
                    },
                )
                ListItem(
                    content = { Text("Start indexing again") },
                    onClick = { state.eventSink(DeveloperSettingsEvents.StartSearchIndexing) },
                )
            }
        }
    }
}

@Composable
private fun SearchIndexSupportingText(text: String) {
    Text(
        text = text,
        style = ElementTheme.typography.fontBodyMdRegular,
        color = ElementTheme.colors.textSecondary,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun CancelSearchIndexingItem(state: DeveloperSettingsState) {
    ListItem(
        content = { Text("Cancel indexing") },
        onClick = { state.eventSink(DeveloperSettingsEvents.CancelSearchIndexing) },
    )
}

@Composable
private fun SessionCategory(deviceId: DeviceId) {
    PreferenceCategory(title = "Session") {
        val toastMessage = stringResource(CommonStrings.common_copied_to_clipboard)
        val context = LocalContext.current
        ListItem(
            content = { Text("DeviceId") },
            supportingContent = { Text(text = deviceId.value) },
            onClick = {
                context.copyToClipboard(
                    text = deviceId.value,
                    toastMessage = toastMessage,
                )
            }
        )
    }
}

@Composable
private fun MarkAllRoomsAsReadCategory(state: DeveloperSettingsState) {
    PreferenceCategory(title = "Room list") {
        ListItem(
            content = {
                Text("Mark all rooms as read")
            },
            supportingContent = {
                Text(
                    text = """
                        This will send a private read receipt and a read marker in every room you are part of. 
                        It's a long running operation that might get rate limited.
                        It will run in the background but the app must be alive for it to finish.
                        """.trimIndent(),
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            },
            enabled = !state.showLoader,
            onClick = {
                state.eventSink(DeveloperSettingsEvents.MarkAllRoomsAsRead(needsConfirmation = true))
            },
        )
    }
}

@Composable
private fun NotificationCategory(onPushHistoryClick: () -> Unit) {
    PreferenceCategory(title = stringResource(id = R.string.screen_notification_settings_title)) {
        ListItem(
            content = {
                Text(stringResource(R.string.troubleshoot_notifications_entry_point_push_history_title))
            },
            onClick = onPushHistoryClick,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun DeveloperSettingsViewPreview(
    @PreviewParameter(DeveloperSettingsStateProvider::class) state: DeveloperSettingsState
) = ElementPreview {
    DeveloperSettingsView(
        state = state,
        onOpenShowkase = {},
        onPushHistoryClick = {},
        onBackClick = {},
    )
}
