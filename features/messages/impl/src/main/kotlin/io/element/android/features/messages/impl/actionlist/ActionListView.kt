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

package io.element.android.features.messages.impl.actionlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.VideoCameraBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.timeline.components.blurhash.BlurHashAsyncImage
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemProfileChangeContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Divider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("MutableParams") // False positive
@Composable
fun ActionListView(
    state: ActionListState,
    isVisible: MutableState<Boolean>,
    onActionSelected: (action: TimelineItemAction, TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(isVisible.value) {
        if (!isVisible.value) {
            state.eventSink(ActionListEvents.Clear)
        }
    }

    fun onItemActionClicked(
        itemAction: TimelineItemAction,
        targetItem: TimelineItem.Event
    ) {
        onActionSelected(itemAction, targetItem)
        isVisible.value = false
    }

    if (isVisible.value) {
        ModalBottomSheet(
            onDismissRequest = {
                isVisible.value = false
            }
        ) {
            SheetContent(
                state = state,
                onActionClicked = ::onItemActionClicked,
                modifier = modifier
                    .padding(bottom = 32.dp)
//                    .navigationBarsPadding() - FIXME after https://issuetracker.google.com/issues/275849044
//                    .imePadding()
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SheetContent(
    state: ActionListState,
    modifier: Modifier = Modifier,
    onActionClicked: (TimelineItemAction, TimelineItem.Event) -> Unit = { _, _ -> },
) {
    when (val target = state.target) {
        is ActionListState.Target.Loading,
        ActionListState.Target.None -> {
            // Crashes if sheetContent size is zero
            Box(modifier = modifier.size(1.dp))
        }

        is ActionListState.Target.Success -> {
            val actions = target.actions
            LazyColumn(
                modifier = modifier.fillMaxWidth()
            ) {
                item {
                    Column {
                        MessageSummary(event = target.event, modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider()
                    }
                }
                item {
                    EmojiReactionsRow(Modifier.fillMaxWidth())
                    Divider()
                }
                items(
                    items = actions,
                ) { action ->
                    ListItem(
                        modifier = Modifier.clickable {
                            onActionClicked(action, target.event)
                        },
                        text = {
                            Text(
                                text = action.title,
                                color = if (action.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                        },
                        icon = {
                            Icon(
                                resourceId = action.icon,
                                contentDescription = "",
                                tint = if (action.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageSummary(event: TimelineItem.Event, modifier: Modifier = Modifier) {
    val content: @Composable () -> Unit
    var icon: @Composable () -> Unit = { Avatar(avatarData = event.senderAvatar.copy(size = AvatarSize.SMALL)) }
    val contentStyle = ElementTextStyles.Regular.bodyMD.copy(color = MaterialTheme.colorScheme.secondary)
    val imageModifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(9.dp))

    @Composable
    fun ContentForBody(body: String) {
        Text(body, style = contentStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }

    when (event.content) {
        is TimelineItemTextBasedContent -> content = { ContentForBody(event.content.body) }
        is TimelineItemStateContent -> content = { ContentForBody(event.content.body) }
        is TimelineItemProfileChangeContent -> content = { ContentForBody(event.content.body) }
        is TimelineItemEncryptedContent -> content = { ContentForBody(stringResource(StringR.string.common_unable_to_decrypt)) }
        is TimelineItemRedactedContent -> content = { ContentForBody(stringResource(StringR.string.common_message_removed)) }
        is TimelineItemUnknownContent -> content = { ContentForBody(stringResource(StringR.string.common_unsupported_event)) }
        is TimelineItemImageContent -> {
            icon = {
                val mediaRequestData = MediaRequestData(
                    source = event.content.mediaSource,
                    kind = MediaRequestData.Kind.Content
                )
                BlurHashAsyncImage(
                    model = mediaRequestData,
                    blurHash = event.content.blurhash,
                    contentDescription = stringResource(StringR.string.common_image),
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier,
                )
            }
            content = { ContentForBody(event.content.body) }
        }
        is TimelineItemVideoContent -> {
            icon = {
                val thumbnailSource = event.content.thumbnailSource
                if (thumbnailSource != null) {
                    val mediaRequestData = MediaRequestData(
                        source = event.content.thumbnailSource,
                        kind = MediaRequestData.Kind.Content
                    )
                    BlurHashAsyncImage(
                        model = mediaRequestData,
                        blurHash = event.content.blurHash,
                        contentDescription = stringResource(StringR.string.common_video),
                        contentScale = ContentScale.Crop,
                        modifier = imageModifier,
                    )
                } else {
                    Box(
                        modifier = imageModifier.background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VideoCameraBack,
                            contentDescription = stringResource(StringR.string.common_video),
                        )
                    }
                }
            }
            content = { ContentForBody(event.content.body) }
        }
        is TimelineItemFileContent -> {
            icon = {
                Box(
                    modifier = imageModifier.background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Attachment,
                        contentDescription = stringResource(StringR.string.common_file),
                        modifier = Modifier.rotate(-45f)
                    )
                }
            }
            content = { ContentForBody(event.content.body) }
        }
    }
    Row(modifier = modifier) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row {
                if (event.senderDisplayName != null) {
                    Text(event.senderDisplayName, style = ElementTextStyles.Bold.caption1)
                }
                Text(
                    event.sentTime,
                    style = ElementTextStyles.Regular.caption2,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
            content()
        }
    }
}

@Composable
internal fun EmojiReactionsRow(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.padding(horizontal = 28.dp, vertical = 16.dp)
    ) {
        // TODO use real emojis, have real interaction
        Text("\uD83D\uDC4D", fontSize = 28.dpToSp())
        Text("\uD83D\uDC4E", fontSize = 28.dpToSp())
        Text("\uD83D\uDD25", fontSize = 28.dpToSp())
        Text("❤\uFE0F", fontSize = 28.dpToSp())
        Text("\uD83D\uDC4F", fontSize = 28.dpToSp())
        Icon(
            imageVector = Icons.Outlined.AddReaction,
            contentDescription = "Emojis",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically)
        )
    }
}

@Composable
private fun Int.dpToSp(): TextUnit = with(LocalDensity.current) {
    return dp.toSp()
}

@Preview
@Composable
fun SheetContentLightPreview(@PreviewParameter(ActionListStateProvider::class) state: ActionListState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun SheetContentDarkPreview(@PreviewParameter(ActionListStateProvider::class) state: ActionListState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: ActionListState) {
    SheetContent(state = state)
}
