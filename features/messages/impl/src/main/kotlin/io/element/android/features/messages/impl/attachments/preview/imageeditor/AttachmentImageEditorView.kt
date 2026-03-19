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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.R
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentImageEditorView(
    state: AttachmentImageEditorState,
    onCropRectChange: (NormalizedCropRect) -> Unit,
    onRotateClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotateContentDescription = stringResource(R.string.screen_media_upload_preview_rotate)
    val rotationStateDescription = pluralStringResource(
        R.plurals.a11y_media_upload_preview_rotation_degrees,
        state.edits.rotationDegrees,
        state.edits.rotationDegrees,
    )
    val rotateButtonBackground = ElementTheme.colors.bgSubtlePrimary

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
                title = {},
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .background(ElementTheme.colors.bgCanvasDefault)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        TextButton(
                            text = stringResource(CommonStrings.action_cancel),
                            onClick = onCancelClick,
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(
                            onClick = onRotateClick,
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    color = rotateButtonBackground,
                                    shape = CircleShape,
                                )
                                .clearAndSetSemantics {
                                    contentDescription = rotateContentDescription
                                    stateDescription = rotationStateDescription
                                }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Icon(
                                    modifier = Modifier
                                        .size(22.dp),
                                    imageVector = CompoundIcons.Restart(),
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${state.edits.rotationDegrees}°",
                                    style = ElementTheme.typography.fontBodyXsMedium,
                                    color = ElementTheme.colors.textSecondary,
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        TextButton(
                            text = stringResource(CommonStrings.action_done),
                            onClick = onDoneClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CropEditorCanvas(
    state: AttachmentImageEditorState,
    onCropRectChange: (NormalizedCropRect) -> Unit,
) {
    var imageSize by remember(state.localMedia.uri) { mutableStateOf(IntSize.Zero) }
    val rotationQuarterTurns = state.edits.normalizedRotationQuarterTurns

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
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
                .align(Alignment.Center),
            contentAlignment = Alignment.Center,
        ) {
            if (LocalInspectionMode.current) {
                Image(
                    painter = painterResource(id = CommonDrawables.sample_background),
                    contentDescription = null,
                    modifier = Modifier
                        .requiredSize(imageLayoutWidthDp, imageLayoutHeightDp)
                        .graphicsLayer { rotationZ = rotationQuarterTurns * 90f },
                    contentScale = ContentScale.Fit,
                )
            } else {
                AsyncImage(
                    model = state.localMedia.uri,
                    contentDescription = stringResource(CommonStrings.common_image),
                    modifier = Modifier
                        .requiredSize(imageLayoutWidthDp, imageLayoutHeightDp)
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

            CropOverlay(
                cropRect = state.edits.cropRect,
                onCropRectChange = onCropRectChange,
            )
        }
    }
}

@Composable
private fun CropOverlay(
    cropRect: NormalizedCropRect,
    onCropRectChange: (NormalizedCropRect) -> Unit,
) {
    var dragTarget by remember { mutableStateOf<CropDragTarget?>(null) }
    val latestCropRect by rememberUpdatedState(cropRect)
    val borderColor = ElementTheme.colors.textPrimary
    val guideColor = ElementTheme.colors.textSecondary

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragTarget = detectDragTarget(
                            touchPoint = offset,
                            cropRect = latestCropRect,
                            canvasSize = Size(size.width.toFloat(), size.height.toFloat()),
                            handleTouchRadius = 32.dp.toPx(),
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
                    onCropRectChange(
                        latestCropRect.resize(
                            dragTarget = activeTarget,
                            deltaX = dragAmount.x / size.width.toFloat(),
                            deltaY = dragAmount.y / size.height.toFloat(),
                        )
                    )
                }
            }
    ) {
        val cropLeft = cropRect.left * size.width
        val cropTop = cropRect.top * size.height
        val cropRight = cropRect.right * size.width
        val cropBottom = cropRect.bottom * size.height
        val overlayColor = Color.Black.copy(alpha = 0.48f)

        drawRect(
            color = overlayColor,
            topLeft = Offset.Zero,
            size = Size(width = size.width, height = cropTop),
        )
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, cropTop),
            size = Size(width = cropLeft, height = cropBottom - cropTop),
        )
        drawRect(
            color = overlayColor,
            topLeft = Offset(cropRight, cropTop),
            size = Size(width = size.width - cropRight, height = cropBottom - cropTop),
        )
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, cropBottom),
            size = Size(width = size.width, height = size.height - cropBottom),
        )

        drawRect(
            color = borderColor,
            topLeft = Offset(cropLeft, cropTop),
            size = Size(width = cropRight - cropLeft, height = cropBottom - cropTop),
            style = Stroke(width = 2.dp.toPx()),
        )

        val thirdWidth = (cropRight - cropLeft) / 3f
        val thirdHeight = (cropBottom - cropTop) / 3f
        repeat(2) { index ->
            val offsetX = cropLeft + thirdWidth * (index + 1)
            val offsetY = cropTop + thirdHeight * (index + 1)
            drawLine(
                color = guideColor,
                start = Offset(offsetX, cropTop),
                end = Offset(offsetX, cropBottom),
                strokeWidth = 1.dp.toPx(),
            )
            drawLine(
                color = guideColor,
                start = Offset(cropLeft, offsetY),
                end = Offset(cropRight, offsetY),
                strokeWidth = 1.dp.toPx(),
            )
        }

        val handleLength = 16.dp.toPx()
        val handleColor = borderColor
        drawCornerHandle(cropLeft, cropTop, handleLength, handleColor, true, true)
        drawCornerHandle(cropRight, cropTop, handleLength, handleColor, false, true)
        drawCornerHandle(cropLeft, cropBottom, handleLength, handleColor, true, false)
        drawCornerHandle(cropRight, cropBottom, handleLength, handleColor, false, false)
        drawEdgeHandle(
            center = Offset((cropLeft + cropRight) / 2f, cropTop),
            horizontal = true,
            handleLength = handleLength,
            color = handleColor,
        )
        drawEdgeHandle(
            center = Offset(cropRight, (cropTop + cropBottom) / 2f),
            horizontal = false,
            handleLength = handleLength,
            color = handleColor,
        )
        drawEdgeHandle(
            center = Offset((cropLeft + cropRight) / 2f, cropBottom),
            horizontal = true,
            handleLength = handleLength,
            color = handleColor,
        )
        drawEdgeHandle(
            center = Offset(cropLeft, (cropTop + cropBottom) / 2f),
            horizontal = false,
            handleLength = handleLength,
            color = handleColor,
        )
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
    cropRect: NormalizedCropRect,
    canvasSize: Size,
    handleTouchRadius: Float,
): CropDragTarget? {
    val corners = mapOf(
        CropDragTarget.TopLeft to Offset(cropRect.left * canvasSize.width, cropRect.top * canvasSize.height),
        CropDragTarget.Top to Offset((cropRect.left + cropRect.right) * canvasSize.width / 2f, cropRect.top * canvasSize.height),
        CropDragTarget.TopRight to Offset(cropRect.right * canvasSize.width, cropRect.top * canvasSize.height),
        CropDragTarget.Right to Offset(cropRect.right * canvasSize.width, (cropRect.top + cropRect.bottom) * canvasSize.height / 2f),
        CropDragTarget.BottomRight to Offset(cropRect.right * canvasSize.width, cropRect.bottom * canvasSize.height),
        CropDragTarget.Bottom to Offset((cropRect.left + cropRect.right) * canvasSize.width / 2f, cropRect.bottom * canvasSize.height),
        CropDragTarget.BottomLeft to Offset(cropRect.left * canvasSize.width, cropRect.bottom * canvasSize.height),
        CropDragTarget.Left to Offset(cropRect.left * canvasSize.width, (cropRect.top + cropRect.bottom) * canvasSize.height / 2f),
    )
    corners.forEach { (target, corner) ->
        if ((corner - touchPoint).getDistance() <= handleTouchRadius) {
            return target
        }
    }
    val cropLeft = cropRect.left * canvasSize.width
    val cropTop = cropRect.top * canvasSize.height
    val cropRight = cropRect.right * canvasSize.width
    val cropBottom = cropRect.bottom * canvasSize.height
    return if (touchPoint.x in cropLeft..cropRight && touchPoint.y in cropTop..cropBottom) {
        CropDragTarget.Move
    } else {
        null
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCornerHandle(
    x: Float,
    y: Float,
    handleLength: Float,
    color: Color,
    isLeft: Boolean,
    isTop: Boolean,
) {
    val horizontalEndX = if (isLeft) x + handleLength else x - handleLength
    val verticalEndY = if (isTop) y + handleLength else y - handleLength
    drawLine(
        color = color,
        start = Offset(x, y),
        end = Offset(horizontalEndX, y),
        strokeWidth = 3.dp.toPx(),
    )
    drawLine(
        color = color,
        start = Offset(x, y),
        end = Offset(x, verticalEndY),
        strokeWidth = 3.dp.toPx(),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEdgeHandle(
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
        strokeWidth = 3.dp.toPx(),
    )
}

@PreviewsDayNight
@Composable
internal fun AttachmentImageEditorViewPreview() = ElementPreview {
    AttachmentImageEditorView(
        state = AttachmentImageEditorState(
            localMedia = LocalMedia(
                uri = "file://preview-image".toUri(),
                info = anImageMediaInfo(),
            ),
            edits = AttachmentImageEdits(),
        ),
        onCropRectChange = {},
        onRotateClick = {},
        onCancelClick = {},
        onDoneClick = {},
    )
}
