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

package io.element.android.features.messages.impl.timeline.debug

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.EventId

/**
 * Screen used to display debug info for events.
 * It will only be available in debug builds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDebugInfoView(
    eventId: EventId?,
    model: String,
    originalJson: String?,
    latestEditedJson: String?,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    isTest: Boolean = false,
) {
    val sectionsInitiallyExpanded = isTest || LocalInspectionMode.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Debug event info",
                        style = ElementTheme.typography.aliasScreenTitle,
                    )
                },
                navigationIcon = { BackButton(onClick = onBackPressed) }
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding) // Window insets
                .consumeWindowInsets(padding)
                // Internal padding
                .padding(horizontal = 16.dp)
        ) {
            item {
                Column(Modifier.padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Event ID:")
                    CopyableText(text = eventId?.value ?: "-", modifier = Modifier.fillMaxWidth())
                }
            }
            item {
                CollapsibleSection(title = "Model:", text = model, initiallyExpanded = sectionsInitiallyExpanded)
            }
            if (originalJson != null) {
                item {
                    CollapsibleSection(title = "Original JSON:", text = originalJson, initiallyExpanded = sectionsInitiallyExpanded)
                }
            }
            if (latestEditedJson != null) {
                item {
                    CollapsibleSection(title = "Latest edited JSON:", text = latestEditedJson, initiallyExpanded = sectionsInitiallyExpanded)
                }
            }
        }
    }
}

@Composable
private fun CollapsibleSection(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                imageVector = CompoundIcons.ChevronDown,
                contentDescription = null
            )
        }
        AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
            CopyableText(text = text, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun CopyableText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboardManager = remember { requireNotNull(context.getSystemService<ClipboardManager>()) }
    Box(
        modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(6.dp)
            .clickable { clipboardManager.setPrimaryClip(ClipData.newPlainText("JSON", text)) }
    ) {
        Text(
            text = text,
            style = ElementTheme.typography.fontBodyMdRegular.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.padding(8.dp),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun EventDebugInfoViewPreview() = ElementPreview {
    EventDebugInfoView(
        eventId = EventId("\$some-event-id"),
        model = "Rust(\n\tModel()\n)",
        originalJson = "{\"name\": \"original\"}",
        latestEditedJson = "{\"name\": \"edited\"}",
        onBackPressed = { }
    )
}
