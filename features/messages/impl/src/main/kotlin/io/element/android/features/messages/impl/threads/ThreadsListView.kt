/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.threads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.theme.components.ElementLoadingIndicator
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.threads.ThreadListItemData
import io.element.android.libraries.ui.strings.CommonStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadsListView(
    threads: List<ThreadListItemData>,
    isLoading: Boolean,
    onThreadClick: (EventId) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                titleStr = stringResource(CommonStrings.common_thread) + "s",
                navigationIcon = { BackButton(onClick = onBackClick) },
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .fillMaxSize(),
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            ElementLoadingIndicator()
                        }
                    }
                    threads.isEmpty() -> {
                        ThreadsListEmpty()
                    }
                    else -> {
                        ThreadsListLoaded(
                            threads = threads,
                            onThreadClick = onThreadClick,
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ThreadsListEmpty(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = 32.dp,
                vertical = 48.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        IconTitleSubtitleMolecule(
            title = "No threads yet",
            subTitle = null,
            iconStyle = BigIcon.Style.Default(CompoundIcons.Threads()),
        )
    }
}

@Composable
private fun ThreadsListLoaded(
    threads: List<ThreadListItemData>,
    onThreadClick: (EventId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            items = threads,
            key = { it.rootEventId.value },
        ) { thread ->
            ListItem(
                headlineContent = {
                    Text(
                        text = thread.senderDisplayName ?: thread.senderId.value,
                        style = ElementTheme.typography.fontBodyLgMedium,
                    )
                },
                supportingContent = thread.lastMessagePreview?.let { preview ->
                    {
                        Text(
                            text = preview,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = ElementTheme.typography.fontBodySmRegular,
                            color = ElementTheme.colors.textSecondary,
                        )
                    }
                },
                trailingContent = ListItemContent.Text(formatTimestamp(thread.timestamp)),
                onClick = { onThreadClick(thread.rootEventId) },
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
