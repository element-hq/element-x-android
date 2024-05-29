/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.preferences.impl.developer.tracing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.Target
import kotlinx.collections.immutable.ImmutableMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureTracingView(
    state: ConfigureTracingState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onBackClick)
                },
                title = {
                    Text(
                        text = "Configure tracing",
                        style = ElementTheme.typography.aliasScreenTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showMenu = !showMenu }
                    ) {
                        Icon(
                            imageVector = CompoundIcons.OverflowVertical(),
                            tint = ElementTheme.materialColors.secondary,
                            contentDescription = null,
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                showMenu = false
                                state.eventSink.invoke(ConfigureTracingEvents.ResetFilters)
                            },
                            text = { Text("Reset to default") },
                            leadingIcon = {
                                Icon(
                                    imageVector = CompoundIcons.Delete(),
                                    tint = ElementTheme.materialColors.secondary,
                                    contentDescription = null,
                                )
                            }
                        )
                    }
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .padding(it)
                    .consumeWindowInsets(it)
                    .verticalScroll(state = rememberScrollState())
            ) {
                CrateListContent(state)
                ListItem(
                    headlineContent = {
                        Text(
                            text = "Kill and restart the app for the change to take effect.",
                            style = ElementTheme.typography.fontHeadingSmMedium,
                        )
                    },
                )
            }
        }
    )
}

@Composable
private fun CrateListContent(
    state: ConfigureTracingState,
) {
    fun onLogLevelChange(target: Target, logLevel: LogLevel) {
        state.eventSink(ConfigureTracingEvents.UpdateFilter(target, logLevel))
    }

    TargetAndLogLevelListView(
        data = state.targetsToLogLevel,
        onLogLevelChange = ::onLogLevelChange,
    )
}

@Composable
private fun TargetAndLogLevelListView(
    data: ImmutableMap<Target, LogLevel>,
    onLogLevelChange: (Target, LogLevel) -> Unit,
) {
    Column {
        data.forEach { item ->
            fun onLogLevelChange(logLevel: LogLevel) {
                onLogLevelChange(item.key, logLevel)
            }

            TargetAndLogLevelView(
                target = item.key,
                logLevel = item.value,
                onLogLevelChange = ::onLogLevelChange
            )
        }
    }
}

@Composable
private fun TargetAndLogLevelView(
    target: Target,
    logLevel: LogLevel,
    onLogLevelChange: (LogLevel) -> Unit,
) {
    ListItem(
        headlineContent = { Text(text = target.filter.takeIf { it.isNotEmpty() } ?: "(common)") },
        trailingContent = ListItemContent.Custom {
            LogLevelDropdownMenu(
                logLevel = logLevel,
                onLogLevelChange = onLogLevelChange,
            )
        },
    )
}

@Composable
private fun LogLevelDropdownMenu(
    logLevel: LogLevel,
    onLogLevelChange: (LogLevel) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        DropdownMenuItem(
            modifier = Modifier.widthIn(max = 120.dp),
            text = { Text(text = logLevel.filter) },
            onClick = { expanded = !expanded },
            trailingIcon = {
                Icon(
                    modifier = Modifier.rotate(if (expanded) 180f else 0f),
                    imageVector = CompoundIcons.ChevronDown(),
                    contentDescription = null,
                )
            },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            LogLevel.values().forEach { logLevel ->
                DropdownMenuItem(
                    text = {
                        Text(text = logLevel.filter)
                    },
                    onClick = {
                        expanded = false
                        onLogLevelChange(logLevel)
                    }
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ConfigureTracingViewPreview(
    @PreviewParameter(ConfigureTracingStateProvider::class) state: ConfigureTracingState
) = ElementPreview {
    ConfigureTracingView(
        state = state,
        onBackClick = {},
    )
}
