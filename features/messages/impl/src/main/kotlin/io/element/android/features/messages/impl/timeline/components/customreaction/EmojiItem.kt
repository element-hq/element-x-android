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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
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
import io.element.android.features.messages.impl.timeline.components.customreaction.picker.SkinTonePicker
import io.element.android.features.messages.impl.timeline.components.customreaction.picker.SkinToneSlotSize
import io.element.android.features.messages.impl.timeline.components.customreaction.picker.SkinToneSlotSpacing
import io.element.android.features.messages.impl.timeline.components.customreaction.picker.aSkinList
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Composable
fun EmojiItem(
    item: Emoji,
    isSelected: Boolean,
    onSelectEmoji: (Emoji) -> Unit,
    onLongPress: (Emoji) -> Unit,
    onDismissSkinPicker: () -> Unit,
    skinPickerEmoji: Emoji?,
    selectedSkinUnicodes: ImmutableSet<String>,
    hasSelectedSkin: Boolean,
    modifier: Modifier = Modifier,
    emojiSize: TextUnit = 20.sp,
) {
    val backgroundColor = when {
        isSelected -> ElementTheme.colors.bgActionPrimaryRest
        hasSelectedSkin -> ElementTheme.colors.bgActionTertiarySelected
        else -> Color.Transparent
    }
    val density = LocalDensity.current
    var itemSize by remember { mutableStateOf(IntSize.Zero) }
    var itemOffsetPercent by remember { mutableFloatStateOf(0f) }
    var hoveredIndex by remember { mutableIntStateOf(-1) }
    var dismissed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val description = a11yReactionAction(
        emoji = item.unicode,
        userAlreadyReacted = isSelected,
    )
    val hasSkinTones = !item.skins.isNullOrEmpty()
    Box(
        modifier = modifier
            .onPlaced { coordinates ->
                val position = coordinates.positionInParent()
                val parentBounds = coordinates.parentLayoutCoordinates?.size ?: return@onPlaced
                // Calculate the offset of the item inside its parent as a percentage, to be able to set the initial offset
                // for the selection inside the skin tone picker relative to its size.
                itemOffsetPercent = if (parentBounds.width > 0) {
                    position.x / parentBounds.width
                } else {
                    0f
                }
            }
            .sizeIn(minWidth = 40.dp, minHeight = 40.dp)
            .onSizeChanged { itemSize = it }
            .background(backgroundColor, CircleShape)
            .indication(interactionSource, ripple())
            .pointerInput(item) {
                // Always detect long press and tap gestures
                detectTapGestures(
                    onPress = { pressOffset ->
                        val press = PressInteraction.Press(pressOffset)
                        interactionSource.emit(press)
                        if (tryAwaitRelease()) {
                            interactionSource.emit(PressInteraction.Release(press))
                            if (hasSkinTones) {
                                onSelectEmoji(item)
                            }
                        } else {
                            interactionSource.emit(PressInteraction.Cancel(press))
                        }
                    },
                    onTap = { onSelectEmoji(item) },
                )
            }
            .then(
                // Only detect drag after long press for those items which have a skin tone picker
                if (hasSkinTones) {
                    Modifier.pointerInput(item) {
                        val slotWidthPx = with(density) { SkinToneSlotSize.toPx() }
                        val spacingPx = with(density) { SkinToneSlotSpacing.toPx() }
                        val paddingPx = with(density) { SkinTonePadding.toPx() }
                        var startOffset = Offset.Zero
                        detectDragGesturesAfterLongPress(
                            onDragStart = { position ->
                                startOffset = position
                                dismissed = false
                                onLongPress(item)
                                hoveredIndex = -1
                            },
                            onDrag = { change, _ ->
                                if (!dismissed) {
                                    change.consume()
                                    // It's a valid drag event if it's within ~2 items height above, so, on top of the current one or the skin tone selector,
                                    // or 1 item below, which should be far enough to not be triggered by mistake
                                    val isValidDrag = if (change.position.y < startOffset.y) {
                                        startOffset.y - change.position.y <= itemSize.height * 2f
                                    } else {
                                        change.position.y - startOffset.y <= itemSize.height
                                    }
                                    if (!isValidDrag) {
                                        dismissed = true
                                        hoveredIndex = -1
                                        onDismissSkinPicker()
                                    } else {
                                        val skinItemsCount = item.skins!!.size
                                        // Original + variants
                                        val totalSlots = 1 + skinItemsCount
                                        // Calculate the whole size of the skin tone picker
                                        val pickerWidthPx = 2 * paddingPx + totalSlots * slotWidthPx + (totalSlots - 1) * spacingPx
                                        // Calculate the initial offset inside the picker, given the relative position of the item inside its parent
                                        val initialOffset = pickerWidthPx * itemOffsetPercent
                                        val xInPicker = initialOffset + change.position.x

                                        // If it's a valid offset, calculate the hovered index, otherwise, set it to -1 to indicate that no item is hovered
                                        val index = if (xInPicker in 0f..pickerWidthPx) {
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
                                onDismissSkinPicker()
                            },
                            onDragCancel = {
                                dismissed = false
                                hoveredIndex = -1
                            },
                        )
                    }
                } else {
                    Modifier
                }
            )
            .clearAndSetSemantics {
                contentDescription = description
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.unicode,
            style = LocalTextStyle.current.copy(fontSize = emojiSize),
        )
        if (hasSkinTones) {
            val pickerIndicatorColor = ElementTheme.colors.borderInteractivePrimary
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
                drawPath(path, color = pickerIndicatorColor, style = Fill)
            }
        }

        if (skinPickerEmoji == item) {
            val pickerHeight = SkinTonePadding * 2 + SkinToneSlotSize
            val popupOffsetPx = with(density) { -pickerHeight.roundToPx() }
            Popup(
                onDismissRequest = { onDismissSkinPicker() },
                alignment = Alignment.BottomCenter,
                offset = IntOffset(0, popupOffsetPx),
            ) {
                SkinTonePicker(
                    emoji = item,
                    onSelect = { selectedEmoji ->
                        onSelectEmoji(selectedEmoji)
                        onDismissSkinPicker()
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
        modifier = Modifier.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (isSelected in listOf(false, true)) {
            for (skinList in listOf(persistentListOf(), aSkinList())) {
                val hasSelectedSkinValues = if (skinList.isNotEmpty()) listOf(false, true) else listOf(false)
                for (hasSelectedSkin in hasSelectedSkinValues) {
                    EmojiItem(
                        item = Emoji(
                            hexcode = "",
                            label = "",
                            tags = null,
                            shortcodes = persistentListOf(),
                            unicode = "👍",
                            skins = skinList,
                        ),
                        isSelected = isSelected,
                        onSelectEmoji = {},
                        hasSelectedSkin = hasSelectedSkin,
                        onLongPress = {},
                        onDismissSkinPicker = {},
                        skinPickerEmoji = null,
                        selectedSkinUnicodes = persistentSetOf(),
                    )
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun EmojiItemWithPopupPreview() = ElementPreview {
    Row(
        modifier = Modifier
            .padding(top = 100.dp, bottom = 4.dp, start = 200.dp, end = 200.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val item = Emoji(
            hexcode = "",
            label = "",
            tags = null,
            shortcodes = persistentListOf(),
            unicode = "👍",
            skins = aSkinList(),
        )
        EmojiItem(
            item = item,
            isSelected = false,
            onSelectEmoji = {},
            hasSelectedSkin = false,
            onLongPress = {},
            onDismissSkinPicker = {},
            skinPickerEmoji = item,
            selectedSkinUnicodes = persistentSetOf("👍🏾"),
        )
    }
}
