/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview.imageeditor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.R
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.ui.strings.CommonStrings
import kotlin.math.min

private val minHandleTouchRadius = 16.dp
private val maxHandleTouchRadius = 56.dp

/**
 * Ref: https://www.figma.com/design/zftpgS6LjiczobJZ1GUNpt/Updates-to-Media---File-Upload?node-id=51-3539
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentImageEditorView(
    state: AttachmentImageEditorState,
    onCropRectChange: (NormalizedCropRect) -> Unit,
    onRotateClick: () -> Unit,
    onFlipHorizontallyClick: () -> Unit,
    onFlipVerticallyClick: () -> Unit,
    onResetClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotateContentDescription = stringResource(R.string.screen_image_edition_a11y_rotate_to_the_left)
    val rotationStateDescription = pluralStringResource(
        R.plurals.screen_image_edition_a11y_rotation_state,
        state.edits.rotationDegrees,
        state.edits.rotationDegrees,
    )
    val flipHorizontalLabel = stringResource(R.string.screen_image_edition_a11y_flip_image_horizontally)
    val flipHorizontalState = if (state.edits.isFlippedHorizontally) {
        stringResource(R.string.screen_image_edition_a11y_flip_image_horizontally_state_flipped)
    } else {
        stringResource(R.string.screen_image_edition_a11y_flip_image_horizontally_state_original)
    }
    val flipVerticalLabel = stringResource(R.string.screen_image_edition_a11y_flip_image_vertically)
    val flipVerticalState = if (state.edits.isFlippedVertically) {
        stringResource(R.string.screen_image_edition_a11y_flip_image_vertically_state_flipped)
    } else {
        stringResource(R.string.screen_image_edition_a11y_flip_image_vertically_state_original)
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(
                        imageVector = CompoundIcons.Close(),
                        onClick = onCancelClick,
                    )
                },
                title = {
                    Text(
                        modifier = Modifier.semantics {
                            heading()
                        },
                        text = stringResource(R.string.screen_image_edition_title),
                    )
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ElementTheme.colors.bgCanvasDefault)
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CropEditorCanvas(
                    state = state,
                    onCropRectChange = onCropRectChange,
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 360.dp)
                    .navigationBarsPadding()
                    .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    TextButton(
                        text = stringResource(CommonStrings.action_reset),
                        destructive = true,
                        onClick = onResetClick,
                    )
                }
                Row(
                    modifier = Modifier.weight(2f),
                    // Center the content horizontally
                    horizontalArrangement = Arrangement.Center,
                ) {
                    IconButton(
                        onClick = onFlipHorizontallyClick,
                        modifier = Modifier
                            .clearAndSetSemantics {
                                contentDescription = flipHorizontalLabel
                                stateDescription = flipHorizontalState
                            }
                    ) {
                        Icon(
                            imageVector = CompoundIcons.FlipHorizontal(),
                            contentDescription = null,
                        )
                    }
                    IconButton(
                        onClick = onRotateClick,
                        modifier = Modifier
                            .clearAndSetSemantics {
                                contentDescription = rotateContentDescription
                                stateDescription = rotationStateDescription
                            }
                    ) {
                        Icon(
                            imageVector = CompoundIcons.RotateLeft(),
                            contentDescription = null,
                        )
                    }
                    IconButton(
                        onClick = onFlipVerticallyClick,
                        modifier = Modifier
                            .clearAndSetSemantics {
                                contentDescription = flipVerticalLabel
                                stateDescription = flipVerticalState
                            }
                    ) {
                        Icon(
                            imageVector = CompoundIcons.FlipVertical(),
                            contentDescription = null,
                        )
                    }
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    TextButton(
                        text = stringResource(CommonStrings.action_save),
                        onClick = onDoneClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.CropEditorCanvas(
    state: AttachmentImageEditorState,
    onCropRectChange: (NormalizedCropRect) -> Unit,
) {
    var imageSize by remember(state.localMedia.uri) { mutableStateOf(IntSize.Zero) }
    val rotationQuarterTurns = state.edits.normalizedRotationQuarterTurns
    val flipScaleX = if (state.edits.isFlippedHorizontally) -1f else 1f
    val flipScaleY = if (state.edits.isFlippedVertically) -1f else 1f

    var imageRect by remember { mutableStateOf(Rect.Zero) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        val displayedSize = remember(maxWidth, maxHeight, imageSize, rotationQuarterTurns) {
            val sourceWidth = imageSize.width.takeIf { it > 0 } ?: 1
            val sourceHeight = imageSize.height.takeIf { it > 0 } ?: 1
            val aspectRatio = if (rotationQuarterTurns % 2 == 0) {
                sourceWidth.toFloat() / sourceHeight.toFloat()
            } else {
                sourceHeight.toFloat() / sourceWidth.toFloat()
            }
            fitSize(
                containerWidth = constraints.maxWidth.toFloat(),
                containerHeight = constraints.maxHeight.toFloat(),
                aspectRatio = aspectRatio,
            )
        }
        val density = LocalDensity.current
        val displayedWidthDp = with(density) { displayedSize.width.toDp() }
        val displayedHeightDp = with(density) { displayedSize.height.toDp() }
        val imageLayoutSize = remember(displayedSize, rotationQuarterTurns) {
            if (rotationQuarterTurns % 2 == 0) {
                displayedSize
            } else {
                Size(
                    width = displayedSize.height,
                    height = displayedSize.width,
                )
            }
        }
        val imageLayoutWidthDp = with(density) { imageLayoutSize.width.toDp() }
        val imageLayoutHeightDp = with(density) { imageLayoutSize.height.toDp() }

        Box(
            modifier = Modifier
                .size(displayedWidthDp, displayedHeightDp)
                .align(Alignment.Center)
                .onPlaced {
                    imageRect = it.boundsInParent()
                },
            contentAlignment = Alignment.Center,
        ) {
            if (LocalInspectionMode.current) {
                Image(
                    painter = painterResource(id = CommonDrawables.sample_background),
                    contentDescription = null,
                    modifier = Modifier
                        .requiredSize(imageLayoutWidthDp, imageLayoutHeightDp)
                        .graphicsLayer {
                            scaleX = flipScaleX
                            scaleY = flipScaleY
                        }
                        .graphicsLayer { rotationZ = rotationQuarterTurns * 90f },
                    contentScale = ContentScale.Fit,
                )
            } else {
                AsyncImage(
                    model = state.localMedia.uri,
                    contentDescription = stringResource(CommonStrings.common_image),
                    modifier = Modifier
                        .requiredSize(imageLayoutWidthDp, imageLayoutHeightDp)
                        .graphicsLayer {
                            scaleX = flipScaleX
                            scaleY = flipScaleY
                        }
                        .graphicsLayer { rotationZ = rotationQuarterTurns * 90f },
                    contentScale = ContentScale.Fit,
                    onState = { painterState ->
                        if (painterState is AsyncImagePainter.State.Success) {
                            imageSize = IntSize(
                                width = painterState.result.image.width,
                                height = painterState.result.image.height,
                            )
                        }
                    }
                )
            }
        }
        val minHandleTouchRadiusPx = minHandleTouchRadius.toPx()
        val maxHandleTouchRadiusPx = maxHandleTouchRadius.toPx()
        val touchRadiusPx by rememberUpdatedState(
            (min(
                state.edits.cropRect.width * imageRect.width,
                state.edits.cropRect.height * imageRect.height,
            ) / 4f).coerceIn(
                minHandleTouchRadiusPx,
                maxHandleTouchRadiusPx,
            )
        )
        var dragTarget by remember { mutableStateOf<CropDragTarget?>(null) }
        val latestCropRect by rememberUpdatedState(state.edits.cropRect)
        val drawGuidelines = dragTarget == CropDragTarget.Move || state.previewDebug
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragTarget = detectDragTarget(
                                touchPoint = offset,
                                imageOffset = imageRect.topLeft,
                                cropRect = latestCropRect,
                                canvasSize = Size(imageRect.width, imageRect.height),
                                handleTouchRadius = touchRadiusPx,
                            )
                        },
                        onDragCancel = {
                            dragTarget = null
                        },
                        onDragEnd = {
                            dragTarget = null
                        },
                    ) { change, dragAmount ->
                        val activeTarget = dragTarget ?: return@detectDragGestures
                        change.consume()
                        val gestureAreaWidth = imageRect.width.takeIf { it > 0f } ?: size.width.toFloat()
                        val gestureAreaHeight = imageRect.height.takeIf { it > 0f } ?: size.height.toFloat()
                        onCropRectChange(
                            latestCropRect.applyChange(
                                dragTarget = activeTarget,
                                deltaX = dragAmount.x / gestureAreaWidth,
                                deltaY = dragAmount.y / gestureAreaHeight,
                            )
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            CropOverlay(
                imageSize = DpSize(displayedWidthDp, displayedHeightDp),
                cropRect = state.edits.cropRect,
                drawGuidelines = drawGuidelines,
                previewDebug = state.previewDebug,
                touchRadiusPx = touchRadiusPx,
                dragTarget = dragTarget,
            )
        }
    }
}

@Composable
private fun CropOverlay(
    imageSize: DpSize,
    cropRect: NormalizedCropRect,
    drawGuidelines: Boolean,
    previewDebug: Boolean,
    touchRadiusPx: Float,
    dragTarget: CropDragTarget?,
) {
    val borderColor = ElementTheme.colors.iconPrimary
    val guideColor = ElementTheme.colors.iconPrimary

    Canvas(
        modifier = Modifier.size(imageSize.width, imageSize.height)
    ) {
        val cropLeft = cropRect.left * size.width
        val cropTop = cropRect.top * size.height
        val cropRight = cropRect.right * size.width
        val cropBottom = cropRect.bottom * size.height
        // Hardcoded black: the crop overlay must always darken the image regardless of theme.
        // No semantic token exists for this use case in the Compound design system.
        val overlayColor = Color.Black.copy(alpha = 0.48f)
        // Overlay above the crop area
        drawRect(
            color = overlayColor,
            topLeft = Offset.Zero,
            size = Size(width = size.width, height = cropTop),
        )
        // Overlay on the left of the crop area
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, cropTop),
            size = Size(width = cropLeft, height = cropBottom - cropTop),
        )
        // Overlay on the right of the crop area
        drawRect(
            color = overlayColor,
            topLeft = Offset(cropRight, cropTop),
            size = Size(width = size.width - cropRight, height = cropBottom - cropTop),
        )
        // Overlay below the crop area
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, cropBottom),
            size = Size(width = size.width, height = size.height - cropBottom),
        )
        // Main frame of the crop area
        drawRect(
            color = borderColor,
            topLeft = Offset(cropLeft, cropTop),
            size = Size(width = cropRight - cropLeft, height = cropBottom - cropTop),
            style = Stroke(width = 1.dp.toPx()),
        )
        // Guidelines dividing the crop area into 9 equal parts
        if (drawGuidelines) {
            val thirdWidth = (cropRight - cropLeft) / 3f
            val thirdHeight = (cropBottom - cropTop) / 3f
            for (index in 1..2) {
                val offsetX = cropLeft + thirdWidth * index
                val offsetY = cropTop + thirdHeight * index
                // Vertical guide line
                drawLine(
                    color = guideColor,
                    start = Offset(offsetX, cropTop),
                    end = Offset(offsetX, cropBottom),
                    strokeWidth = 1.dp.toPx(),
                )
                // Horizontal guide line
                drawLine(
                    color = guideColor,
                    start = Offset(cropLeft, offsetY),
                    end = Offset(cropRight, offsetY),
                    strokeWidth = 1.dp.toPx(),
                )
            }
        }
        // Corner handles
        val handleLength = 18.dp.toPx()
        val handleOffset = 2.dp.toPx()
        // Top left corner
        drawCornerHandle(
            x = cropLeft - handleOffset,
            y = cropTop - handleOffset,
            handleLength = handleLength,
            color = borderColor,
            position = CropDragTarget.Corner.TopLeft,
        )
        // Top right corner
        drawCornerHandle(
            x = cropRight + handleOffset,
            y = cropTop - handleOffset,
            handleLength = handleLength,
            color = borderColor,
            position = CropDragTarget.Corner.TopRight,
        )
        // Bottom left corner
        drawCornerHandle(
            x = cropLeft - handleOffset,
            y = cropBottom + handleOffset,
            handleLength = handleLength,
            color = borderColor,
            position = CropDragTarget.Corner.BottomLeft,
        )
        // Bottom right corner
        drawCornerHandle(
            x = cropRight + handleOffset,
            y = cropBottom + handleOffset,
            handleLength = handleLength,
            color = borderColor,
            position = CropDragTarget.Corner.BottomRight,
        )
        val handleColor = borderColor
        // Top handle
        drawEdgeHandle(
            center = Offset((cropLeft + cropRight) / 2f, cropTop - handleOffset),
            horizontal = true,
            handleLength = handleLength,
            color = handleColor,
        )
        // Right handle
        drawEdgeHandle(
            center = Offset(cropRight + handleOffset, (cropTop + cropBottom) / 2f),
            horizontal = false,
            handleLength = handleLength,
            color = handleColor,
        )
        // Bottom handle
        drawEdgeHandle(
            center = Offset((cropLeft + cropRight) / 2f, cropBottom + handleOffset),
            horizontal = true,
            handleLength = handleLength,
            color = handleColor,
        )
        // Left handle
        drawEdgeHandle(
            center = Offset(cropLeft - handleOffset, (cropTop + cropBottom) / 2f),
            horizontal = false,
            handleLength = handleLength,
            color = handleColor,
        )

        if (previewDebug) {
            // Draw disk around touchable area
            listOf(
                CropDragTarget.Edge.Top,
                CropDragTarget.Edge.Right,
                CropDragTarget.Edge.Bottom,
                CropDragTarget.Edge.Left,
                CropDragTarget.Corner.TopLeft,
                CropDragTarget.Corner.TopRight,
                CropDragTarget.Corner.BottomRight,
                CropDragTarget.Corner.BottomLeft,
                CropDragTarget.Move,
            ).forEach { target ->
                val color = when (target) {
                    is CropDragTarget.Move -> Color.Red
                    is CropDragTarget.Corner -> Color.Blue
                    is CropDragTarget.Edge -> Color.Green
                }.copy(alpha = if (dragTarget == target) 9f else 0.5f)
                drawCircle(
                    color = color,
                    radius = touchRadiusPx,
                    center = computeOffset(target, cropRect, Size(size.width, size.height)),
                )
            }
        }
    }
}

private fun fitSize(
    containerWidth: Float,
    containerHeight: Float,
    aspectRatio: Float,
): Size {
    val widthBasedHeight = containerWidth / aspectRatio
    return if (widthBasedHeight <= containerHeight) {
        Size(width = containerWidth, height = widthBasedHeight)
    } else {
        Size(width = containerHeight * aspectRatio, height = containerHeight)
    }
}

private fun detectDragTarget(
    touchPoint: Offset,
    imageOffset: Offset,
    cropRect: NormalizedCropRect,
    canvasSize: Size,
    handleTouchRadius: Float,
): CropDragTarget? {
    // Give priority on Move (extra detection of the center of crop area)
    // to ensure that user can move a small crop, then to corners and at last to edges.
    val handlesArea = mapOf(
        CropDragTarget.Move to computeOffset(CropDragTarget.Move, cropRect, canvasSize),
        CropDragTarget.Corner.TopLeft to computeOffset(CropDragTarget.Corner.TopLeft, cropRect, canvasSize),
        CropDragTarget.Corner.TopRight to computeOffset(CropDragTarget.Corner.TopRight, cropRect, canvasSize),
        CropDragTarget.Corner.BottomRight to computeOffset(CropDragTarget.Corner.BottomRight, cropRect, canvasSize),
        CropDragTarget.Corner.BottomLeft to computeOffset(CropDragTarget.Corner.BottomLeft, cropRect, canvasSize),
        CropDragTarget.Edge.Top to computeOffset(CropDragTarget.Edge.Top, cropRect, canvasSize),
        CropDragTarget.Edge.Right to computeOffset(CropDragTarget.Edge.Right, cropRect, canvasSize),
        CropDragTarget.Edge.Bottom to computeOffset(CropDragTarget.Edge.Bottom, cropRect, canvasSize),
        CropDragTarget.Edge.Left to computeOffset(CropDragTarget.Edge.Left, cropRect, canvasSize),
    )
    handlesArea.forEach { (target, corner) ->
        if ((corner - touchPoint + imageOffset).getDistance() <= handleTouchRadius) {
            return target
        }
    }
    val cropLeft = imageOffset.x + cropRect.left * canvasSize.width
    val cropTop = imageOffset.y + cropRect.top * canvasSize.height
    val cropRight = imageOffset.x + cropRect.right * canvasSize.width
    val cropBottom = imageOffset.y + cropRect.bottom * canvasSize.height
    return if (touchPoint.x in cropLeft..cropRight && touchPoint.y in cropTop..cropBottom) {
        CropDragTarget.Move
    } else {
        null
    }
}

private fun computeOffset(
    target: CropDragTarget,
    cropRect: NormalizedCropRect,
    canvasSize: Size,
) = when (target) {
    CropDragTarget.Move -> Offset((cropRect.left + cropRect.right) * canvasSize.width / 2f, (cropRect.top + cropRect.bottom) * canvasSize.height / 2f)
    CropDragTarget.Corner.TopLeft -> Offset(cropRect.left * canvasSize.width, cropRect.top * canvasSize.height)
    CropDragTarget.Edge.Top -> Offset((cropRect.left + cropRect.right) * canvasSize.width / 2f, cropRect.top * canvasSize.height)
    CropDragTarget.Corner.TopRight -> Offset(cropRect.right * canvasSize.width, cropRect.top * canvasSize.height)
    CropDragTarget.Edge.Right -> Offset(cropRect.right * canvasSize.width, (cropRect.top + cropRect.bottom) * canvasSize.height / 2f)
    CropDragTarget.Corner.BottomRight -> Offset(cropRect.right * canvasSize.width, cropRect.bottom * canvasSize.height)
    CropDragTarget.Edge.Bottom -> Offset((cropRect.left + cropRect.right) * canvasSize.width / 2f, cropRect.bottom * canvasSize.height)
    CropDragTarget.Corner.BottomLeft -> Offset(cropRect.left * canvasSize.width, cropRect.bottom * canvasSize.height)
    CropDragTarget.Edge.Left -> Offset(cropRect.left * canvasSize.width, (cropRect.top + cropRect.bottom) * canvasSize.height / 2f)
}

// x and y are the coordinates of the corner
private fun DrawScope.drawCornerHandle(
    x: Float,
    y: Float,
    handleLength: Float,
    color: Color,
    position: CropDragTarget.Corner,
) {
    val strokeWidth = 4.dp.toPx()
    val correction = strokeWidth / 2
    val horizontalCorrection = if (position.isLeft()) -correction else correction
    val horizontalEndX = if (position.isLeft()) x + handleLength else x - handleLength
    val verticalEndY = if (position.isTop()) y + handleLength else y - handleLength
    val verticalCorrection = if (position.isTop()) -correction else correction
    // Horizontal line
    drawLine(
        color = color,
        start = Offset(x + horizontalCorrection, y),
        end = Offset(horizontalEndX + horizontalCorrection, y),
        strokeWidth = strokeWidth,
    )
    // Vertical line
    drawLine(
        color = color,
        start = Offset(x, y + verticalCorrection),
        end = Offset(x, verticalEndY + verticalCorrection),
        strokeWidth = strokeWidth,
    )
}

private fun CropDragTarget.Corner.isLeft() = this == CropDragTarget.Corner.TopLeft || this == CropDragTarget.Corner.BottomLeft
private fun CropDragTarget.Corner.isTop() = this == CropDragTarget.Corner.TopLeft || this == CropDragTarget.Corner.TopRight

private fun DrawScope.drawEdgeHandle(
    center: Offset,
    horizontal: Boolean,
    handleLength: Float,
    color: Color,
) {
    val start = if (horizontal) {
        Offset(center.x - handleLength / 2f, center.y)
    } else {
        Offset(center.x, center.y - handleLength / 2f)
    }
    val end = if (horizontal) {
        Offset(center.x + handleLength / 2f, center.y)
    } else {
        Offset(center.x, center.y + handleLength / 2f)
    }
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 4.dp.toPx(),
    )
}

// Only preview in dark, dark theme is forced on the Node.
@Preview
@Composable
internal fun AttachmentImageEditorViewPreview(
    @PreviewParameter(AttachmentImageEditorStateProvider::class) state: AttachmentImageEditorState,
) = ElementPreviewDark {
    AttachmentImageEditorView(
        state = state,
        onCropRectChange = {},
        onRotateClick = {},
        onFlipHorizontallyClick = {},
        onFlipVerticallyClick = {},
        onResetClick = {},
        onCancelClick = {},
        onDoneClick = {},
    )
}
