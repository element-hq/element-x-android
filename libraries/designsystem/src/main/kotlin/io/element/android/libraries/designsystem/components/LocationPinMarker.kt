/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.avatar.avatarShape
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import kotlin.math.cos
import kotlin.math.sin

/**
 * Variants of location pin markers.
 */
sealed interface PinVariant {
    data class UserLocation(
        val avatarData: AvatarData,
        val isLive: Boolean,
    ) : PinVariant

    data object PinnedLocation : PinVariant
    data object StaleLocation : PinVariant
}

private val PIN_MARKER_WIDTH = 42.dp
private val PIN_MARKER_HEIGHT = (PIN_MARKER_WIDTH * 1.2f)
private val DOT_RADIUS = 6.dp
private val CONTENT_OFFSET = 5.dp

/**
 * A location pin marker composable that supports multiple variants.
 *
 * Based on Figma design: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=4665-2890&m=dev
 */
@Composable
fun LocationPinMarker(
    variant: PinVariant,
    modifier: Modifier = Modifier,
) {
    val colors = LocationPinColors.fromVariant(variant)
    Box(
        modifier = modifier.size(width = PIN_MARKER_WIDTH, height = PIN_MARKER_HEIGHT),
    ) {
        // Draw the pin shape
        Canvas(
            modifier = Modifier.matchParentSize()
        ) {
            drawPinShape(
                fillColor = colors.fill,
                strokeColor = colors.stroke,
                strokeWidth = 1.dp.toPx(),
            )
        }

        val avatarSize = PIN_MARKER_WIDTH - CONTENT_OFFSET * 2
        val contentModifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = CONTENT_OFFSET)

        when (variant) {
            is PinVariant.UserLocation -> {
                Avatar(
                    avatarData = variant.avatarData,
                    forcedAvatarSize = avatarSize,
                    avatarType = AvatarType.User,
                    modifier = contentModifier
                        .border(width = 1.dp, color = colors.avatarStoke, shape = AvatarType.User.avatarShape()),
                )
            }
            PinVariant.PinnedLocation, PinVariant.StaleLocation -> {
                Canvas(
                    modifier = contentModifier.size(avatarSize)
                ) {
                    drawCircle(
                        color = colors.dotColor,
                        radius = DOT_RADIUS.toPx(),
                        center = center,
                    )
                }
            }
        }
    }
}

private data class LocationPinColors(
    val fill: Color,
    val stroke: Color,
    val dotColor: Color,
    val avatarStoke: Color,
) {
    companion object {
        @Composable
        fun fromVariant(variant: PinVariant): LocationPinColors {
            return when (variant) {
                is PinVariant.UserLocation ->
                    if (variant.isLive) {
                        LocationPinColors(
                            fill = ElementTheme.colors.iconAccentPrimary,
                            stroke = ElementTheme.colors.bgCanvasDefault,
                            dotColor = Color.Transparent,
                            avatarStoke = ElementTheme.colors.bgCanvasDefault,
                        )
                    } else {
                        LocationPinColors(
                            fill = ElementTheme.colors.bgCanvasDefault,
                            stroke = ElementTheme.colors.iconQuaternaryAlpha,
                            dotColor = Color.Transparent,
                            avatarStoke = ElementTheme.colors.iconQuaternaryAlpha,
                        )
                    }
                PinVariant.PinnedLocation -> LocationPinColors(
                    fill = ElementTheme.colors.bgCanvasDefault,
                    stroke = ElementTheme.colors.iconSecondaryAlpha,
                    dotColor = ElementTheme.colors.iconPrimary,
                    avatarStoke = Color.Transparent,
                )
                PinVariant.StaleLocation -> LocationPinColors(
                    fill = ElementTheme.colors.bgSubtlePrimary,
                    stroke = ElementTheme.colors.borderInteractiveSecondary,
                    dotColor = ElementTheme.colors.iconDisabled,
                    avatarStoke = Color.Transparent,
                )
            }
        }
    }
}

/**
 * Draws a teardrop-shaped pin with smooth curves.
 *
 * Based on SVG reference with dimensions 40x48 (ratio 1:1.2).
 * Uses quadratic Bezier curves for smooth transitions from circle to tip.
 */
private fun DrawScope.drawPinShape(
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float,
) {
    val width = size.width
    val height = size.height

    val circleRadius = width / 2 - strokeWidth
    val circleCenterX = width / 2
    val circleCenterY = width / 2

    // The tip at the bottom
    val tipX = width / 2
    val tipY = height - strokeWidth

    // Angle from the bottom of circle where it transitions to curves (in degrees)
    val transitionAngleDeg = 65f

    val rightTransitionAngle = 90f - transitionAngleDeg
    val leftTransitionAngle = 90f + transitionAngleDeg

    // Calculate transition points on the circle
    val rightTransitionX = circleCenterX + circleRadius * cos(Math.toRadians(rightTransitionAngle.toDouble())).toFloat()
    val rightTransitionY = circleCenterY + circleRadius * sin(Math.toRadians(rightTransitionAngle.toDouble())).toFloat()
    val leftTransitionX = circleCenterX + circleRadius * cos(Math.toRadians(leftTransitionAngle.toDouble())).toFloat()
    val leftTransitionY = circleCenterY + circleRadius * sin(Math.toRadians(leftTransitionAngle.toDouble())).toFloat()

    // Arc sweep: counter-clockwise over the top
    val arcSweepAngle = -(360f - 2 * transitionAngleDeg)

    // For cubic Bezier: tangent direction at transition points
    // Shorter tangent for smoother transition from circle
    val tangentLength = (tipY - leftTransitionY) * 0.45f

    // Left side control points (from left transition to tip)
    val leftTangentAngle = leftTransitionAngle - 90.0
    val leftC1X = leftTransitionX + tangentLength * cos(Math.toRadians(leftTangentAngle)).toFloat()
    val leftC1Y = leftTransitionY + tangentLength * sin(Math.toRadians(leftTangentAngle)).toFloat()
    // C2 control points - horizontal approach creates rounded tip
    val tipOffset = 20f
    val leftC2X = tipX - tipOffset
    val leftC2Y = tipY - strokeWidth
    // Right side control points (from tip to right transition)
    val rightTangentAngle = rightTransitionAngle + 90.0
    val rightC1X = tipX + tipOffset
    val rightC1Y = tipY - strokeWidth
    val rightC2X = rightTransitionX + tangentLength * cos(Math.toRadians(rightTangentAngle)).toFloat()
    val rightC2Y = rightTransitionY + tangentLength * sin(Math.toRadians(rightTangentAngle)).toFloat()

    val path = Path().apply {
        moveTo(rightTransitionX, rightTransitionY)
        arcTo(
            rect = Rect(
                center = Offset(circleCenterX, circleCenterY),
                radius = circleRadius,
            ),
            startAngleDegrees = rightTransitionAngle,
            sweepAngleDegrees = arcSweepAngle,
            forceMoveTo = false,
        )

        // Cubic Bezier from left transition point to tip
        cubicTo(leftC1X, leftC1Y, leftC2X, leftC2Y, tipX, tipY)
        // Cubic Bezier from tip back to right transition point
        cubicTo(rightC1X, rightC1Y, rightC2X, rightC2Y, rightTransitionX, rightTransitionY)

        close()
    }

    drawPath(path = path, color = fillColor, style = Fill)
    drawPath(path = path, color = strokeColor, style = Stroke(width = strokeWidth))
}

@PreviewsDayNight
@Composable
internal fun LocationPinMarkerPreview() = ElementPreview {
    val sampleAvatarData = AvatarData(
        id = "@alice:matrix.org",
        name = "Alice",
        url = null,
        size = AvatarSize.SelectedUser
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LocationPinMarker(
                variant = PinVariant.UserLocation(avatarData = sampleAvatarData, isLive = false),
            )
            LocationPinMarker(
                variant = PinVariant.UserLocation(avatarData = sampleAvatarData, isLive = true),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LocationPinMarker(
                variant = PinVariant.PinnedLocation,
            )
            LocationPinMarker(
                variant = PinVariant.StaleLocation,
            )
        }
    }
}
