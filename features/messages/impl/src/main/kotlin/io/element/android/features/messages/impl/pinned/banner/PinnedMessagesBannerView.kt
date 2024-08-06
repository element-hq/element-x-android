/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.pinned.banner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.pinnedMessageBannerBorder
import io.element.android.libraries.designsystem.theme.pinnedMessageBannerIndicator
import io.element.android.libraries.designsystem.utils.annotatedTextWithBold
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun PinnedMessagesBannerView(
    state: PinnedMessagesBannerState,
    onClick: (EventId) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (state) {
            PinnedMessagesBannerState.Hidden -> Unit
            is PinnedMessagesBannerState.Loading -> {
                PinnedMessagesBannerRow(
                    state = state,
                    onViewAllClick = onViewAllClick,
                    modifier = Modifier.clickable(onClick = { }),
                )
            }
            is PinnedMessagesBannerState.Loaded -> {
                PinnedMessagesBannerRow(
                    state = state,
                    onViewAllClick = onViewAllClick,
                    modifier = Modifier.clickable(
                        onClick = {
                            onClick(state.currentPinnedMessage.eventId)
                            state.eventSink(PinnedMessagesBannerEvents.MoveToNextPinned)
                        }),
                )
            }
        }
    }
}

@Composable
fun PinnedMessagesBannerRow(
    state: PinnedMessagesBannerState,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = ElementTheme.colors.pinnedMessageBannerBorder
    Row(
        modifier = modifier
            .background(color = ElementTheme.colors.bgCanvasDefault)
            .fillMaxWidth()
            .drawBorder(borderColor)
            .heightIn(min = 64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        PinIndicators(
            pinIndex = state.currentPinnedMessageIndex(),
            pinsCount = state.pinnedMessagesCount(),
            modifier = Modifier.heightIn(max = 40.dp)
        )
        Icon(
            imageVector = CompoundIcons.PinSolid(),
            contentDescription = null,
            tint = ElementTheme.materialColors.secondary,
            modifier = Modifier.size(20.dp)
        )
        PinnedMessageItem(
            index = state.currentPinnedMessageIndex(),
            totalCount = state.pinnedMessagesCount(),
            message = state.formattedMessage(),
            modifier = Modifier.weight(1f)
        )
        ViewAllButton(state, onViewAllClick)
    }
}

@Composable
private fun ViewAllButton(
    state: PinnedMessagesBannerState,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        val text = if (state is PinnedMessagesBannerState.Loaded) {
            stringResource(id = CommonStrings.screen_room_pinned_banner_view_all_button_title)
        } else {
            ""
        }
        TextButton(
            text = text,
            showProgress = state is PinnedMessagesBannerState.Loading,
            onClick = onViewAllClick
        )
    }
}

private fun Modifier.drawBorder(borderColor: Color): Modifier {
    return this
        .drawBehind {
            val strokeWidth = 0.5.dp.toPx()
            val y = size.height - strokeWidth / 2
            drawLine(
                borderColor,
                Offset(0f, y),
                Offset(size.width, y),
                strokeWidth
            )
            drawLine(
                borderColor,
                Offset(0f, 0f),
                Offset(size.width, 0f),
                strokeWidth
            )
        }
        .shadow(elevation = 5.dp, spotColor = Color.Transparent)
}

@Composable
private fun PinIndicators(
    pinIndex: Int,
    pinsCount: Int,
    modifier: Modifier = Modifier,
) {
    val indicatorHeight = remember(pinsCount) {
        when (pinsCount) {
            0 -> 0
            1 -> 32
            2 -> 18
            else -> 11
        }
    }
    val lazyListState = rememberLazyListState()
    LaunchedEffect(pinIndex) {
        val viewportSize = lazyListState.layoutInfo.viewportSize
        lazyListState.animateScrollToItem(
            pinIndex,
            indicatorHeight / 2 - viewportSize.height / 2
        )
    }
    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        verticalArrangement = spacedBy(2.dp),
        userScrollEnabled = false,
        reverseLayout = true
    ) {
        items(pinsCount) { index ->
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(indicatorHeight.dp)
                    .background(
                        color = if (index == pinIndex) {
                            ElementTheme.colors.iconAccentPrimary
                        } else {
                            ElementTheme.colors.pinnedMessageBannerIndicator
                        }
                    )
            )
        }
    }
}

@Composable
private fun PinnedMessageItem(
    index: Int,
    totalCount: Int,
    message: AnnotatedString?,
    modifier: Modifier = Modifier,
) {
    val countMessage = stringResource(id = CommonStrings.screen_room_pinned_banner_indicator, index + 1, totalCount)
    val fullCountMessage = stringResource(id = CommonStrings.screen_room_pinned_banner_indicator_description, countMessage)
    Column(modifier = modifier) {
        AnimatedVisibility(totalCount > 1) {
            Text(
                text = annotatedTextWithBold(
                    text = fullCountMessage,
                    boldText = countMessage,
                ),
                style = ElementTheme.typography.fontBodySmMedium,
                color = ElementTheme.colors.textActionAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (message != null) {
            Text(
                text = message,
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textPrimary,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }
}

@Stable
internal interface PinnedMessagesBannerViewScrollBehavior {
    val isVisible: Boolean
    val nestedScrollConnection: NestedScrollConnection
}

internal object PinnedMessagesBannerViewDefaults {
    @Composable
    fun rememberExitOnScrollBehavior(): PinnedMessagesBannerViewScrollBehavior = remember {
        ExitOnScrollBehavior()
    }
}

private class ExitOnScrollBehavior : PinnedMessagesBannerViewScrollBehavior {
    override var isVisible by mutableStateOf(true)
    override val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (available.y < -1) {
                isVisible = true
            }
            if (available.y > 1) {
                isVisible = false
            }
            return Offset.Zero
        }
    }
}

@PreviewsDayNight
@Composable
internal fun PinnedMessagesBannerViewPreview(@PreviewParameter(PinnedMessagesBannerStateProvider::class) state: PinnedMessagesBannerState) = ElementPreview {
    PinnedMessagesBannerView(
        state = state,
        onClick = {},
        onViewAllClick = {},
    )
}
