/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import io.element.android.compound.theme.ElementTheme
import io.element.android.emojibasebindings.Emoji
import io.element.android.features.messages.impl.timeline.a11y.a11yReactionAction
import io.element.android.features.messages.impl.timeline.components.customreaction.picker.SkinTonePadding
import io.element.android.features.messages.impl.timeline.components.customreaction.picker.SkinTonePickerContent
import io.element.android.features.messages.impl.timeline.components.customreaction.picker.SkinToneSlotSize
import io.element.android.features.messages.impl.timeline.components.customreaction.picker.SkinToneSlotSpacing
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.persistentListOf

@Composable
fun EmojiItem(
    item: Emoji,
    isSelected: Boolean,
    onSelectEmoji: (Emoji) -> Unit,
    modifier: Modifier = Modifier,
    emojiSize: TextUnit = 20.sp,
    onLongPress: ((Emoji) -> Unit)? = null,
    skinPickerEmoji: Emoji? = null,
    onDismissSkinPicker: (() -> Unit)? = null,
    selectedSkinUnicodes: Set<String> = emptySet(),
    hasSelectedSkin: Boolean = false,
) {
    val backgroundColor = when {
        isSelected -> ElementTheme.colors.bgActionPrimaryRest
        hasSelectedSkin -> ElementTheme.colors.bgActionTertiarySelected
        else -> Color.Transparent
    }
    val density = LocalDensity.current
    var itemSize by remember { mutableStateOf(IntSize.Zero) }
    var hoveredIndex by remember { mutableStateOf(-1) }
    var dismissed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val description = a11yReactionAction(
        emoji = item.unicode,
        userAlreadyReacted = isSelected,
    )
    Box(
        modifier = modifier
            .sizeIn(minWidth = 40.dp, minHeight = 40.dp)
            .onSizeChanged { itemSize = it }
            .background(backgroundColor, CircleShape)
            .indication(interactionSource, ripple())
            .pointerInput(item) {
                detectTapGestures(
                    onPress = { pressOffset ->
                        val press = PressInteraction.Press(pressOffset)
                        interactionSource.emit(press)
                        if (tryAwaitRelease()) {
                            interactionSource.emit(PressInteraction.Release(press))
                        } else {
                            interactionSource.emit(PressInteraction.Cancel(press))
                        }
                    },
                    onTap = { onSelectEmoji(item) },
                )
            }
            .pointerInput(item) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { _ ->
                        if (item.skins != null && onLongPress != null) {
                            dismissed = false
                            onLongPress(item)
                            hoveredIndex = -1
                        }
                    },
                    onDrag = { change, _ ->
                        if (item.skins != null && !dismissed) {
                            change.consume()
                            val yThresholdPx = with(density) { 100.dp.toPx() }
                            if (change.position.y > yThresholdPx) {
                                dismissed = true
                                hoveredIndex = -1
                                onDismissSkinPicker?.invoke()
                            } else {
                                val skinCount = item.skins!!.size
                                val slotWidthPx = with(density) { SkinToneSlotSize.toPx() }
                                val spacingPx = with(density) { SkinToneSlotSpacing.toPx() }
                                val paddingPx = with(density) { SkinTonePadding.toPx() }
                                val totalSlots = 1 + skinCount
                                val pickerWidthPx = 2 * paddingPx + totalSlots * slotWidthPx + (totalSlots - 1) * spacingPx
                                val pickerHalfWidthPx = pickerWidthPx / 2f
                                val centerXPx = itemSize.width / 2f
                                val pickerLeftPx = centerXPx - pickerHalfWidthPx
                                val xInPicker = change.position.x - pickerLeftPx
                                val index = if (xInPicker >= 0f && xInPicker <= pickerWidthPx) {
                                    (xInPicker / slotWidthPx).toInt().coerceIn(0, totalSlots - 1)
                                } else {
                                    -1
                                }
                                hoveredIndex = index
                            }
                        }
                    },
                    onDragEnd = {
                        if (!dismissed) {
                            val idx = hoveredIndex
                            val skinCount = item.skins?.size ?: 0
                            if (idx in 0..skinCount) {
                                if (idx == 0) {
                                    onSelectEmoji(item)
                                } else {
                                    val skin = item.skins?.getOrNull(idx - 1)
                                    if (skin != null) {
                                        onSelectEmoji(item.copy(unicode = skin.unicode))
                                    }
                                }
                            } else if (idx < 0) {
                                onSelectEmoji(item)
                            }
                        }
                        dismissed = false
                        hoveredIndex = -1
                        onDismissSkinPicker?.invoke()
                    },
                    onDragCancel = {
                        dismissed = false
                        hoveredIndex = -1
                        onDismissSkinPicker?.invoke()
                    },
                )
            }
            .clearAndSetSemantics {
                contentDescription = description
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.unicode,
            style = LocalTextStyle.current.copy(fontSize = emojiSize),
        )
        if (item.skins != null) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(8.dp),
            ) {
                val path = Path().apply {
                    moveTo(size.width, size.height)
                    lineTo(size.width, 0f)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = Color.Gray.copy(alpha = 0.5f), style = Fill)
            }
        }

        if (skinPickerEmoji == item) {
            val pickerHeight = SkinTonePadding * 2 + SkinToneSlotSize
            val popupOffsetPx = with(density) { -pickerHeight.roundToPx() }
            Popup(
                onDismissRequest = { onDismissSkinPicker?.invoke() },
                alignment = Alignment.BottomCenter,
                offset = IntOffset(0, popupOffsetPx),
            ) {
                SkinTonePickerContent(
                    emoji = item,
                    onSelect = { selectedEmoji ->
                        onSelectEmoji(selectedEmoji)
                        onDismissSkinPicker?.invoke()
                    },
                    hoveredIndex = hoveredIndex,
                    selectedUnicodes = selectedSkinUnicodes,
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun EmojiItemPreview() = ElementPreview {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (isSelected in listOf(true, false)) {
            EmojiItem(
                item = Emoji(
                    hexcode = "",
                    label = "",
                    tags = null,
                    shortcodes = persistentListOf(),
                    unicode = "👍",
                    skins = null
                ),
                isSelected = isSelected,
                onSelectEmoji = {},
            )
        }
    }
}
