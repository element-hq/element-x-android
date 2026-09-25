/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarHorizontalFabPosition
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.atoms.CounterAtom
import io.element.android.libraries.designsystem.components.tooltip.PlainTooltip
import io.element.android.libraries.designsystem.components.tooltip.TooltipBox
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Ref: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=4457-1136
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HorizontalFloatingToolbar(
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    floatingActionButton: (@Composable () -> Unit)? = null,
    colors: FloatingToolbarColors = FloatingToolbarDefaults.standardFloatingToolbarColors().copy(
        toolbarContainerColor = ElementTheme.colors.bgSubtleSecondary,
    ),
    contentPadding: PaddingValues = PaddingValues(
        vertical = 8.dp,
        horizontal = 12.dp,
    ),
    scrollBehavior: FloatingToolbarScrollBehavior? = null,
    shape: Shape = FloatingToolbarDefaults.ContainerShape,
    leadingContent: @Composable (RowScope.() -> Unit)? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    floatingActionButtonPosition: FloatingToolbarHorizontalFabPosition =
        FloatingToolbarHorizontalFabPosition.End,
    animationSpec: FiniteAnimationSpec<Float> = FloatingToolbarDefaults.animationSpec(),
    expandedShadowElevation: Dp = 8.dp,
    collapsedShadowElevation: Dp = if (floatingActionButton == null) {
        FloatingToolbarDefaults.ContainerCollapsedElevation
    } else {
        FloatingToolbarDefaults.ContainerCollapsedElevationWithFab
    },
    content: @Composable RowScope.() -> Unit,
) {
    if (floatingActionButton == null) {
        androidx.compose.material3.HorizontalFloatingToolbar(
            expanded = expanded,
            modifier = modifier,
            colors = colors,
            contentPadding = contentPadding,
            scrollBehavior = scrollBehavior,
            shape = shape,
            leadingContent = leadingContent,
            trailingContent = trailingContent,
            expandedShadowElevation = expandedShadowElevation,
            collapsedShadowElevation = collapsedShadowElevation,
            content = content,
        )
    } else {
        androidx.compose.material3.HorizontalFloatingToolbar(
            expanded = expanded,
            floatingActionButton = floatingActionButton,
            modifier = modifier,
            colors = colors,
            contentPadding = contentPadding,
            scrollBehavior = scrollBehavior,
            shape = shape,
            floatingActionButtonPosition = floatingActionButtonPosition,
            animationSpec = animationSpec,
            expandedShadowElevation = expandedShadowElevation,
            collapsedShadowElevation = collapsedShadowElevation,
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalFloatingToolbarItem(
    icon: ImageVector,
    tooltipLabel: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    counter: Int? = null,
    forceRenderingTooltip: Boolean = false,
) {
    TooltipBox(
        positionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Above
            ),
        tooltip = { PlainTooltip { Text(tooltipLabel) } },
        state = rememberTooltipState(
            initialIsVisible = forceRenderingTooltip,
        ),
        modifier = modifier,
    ) {
        val colors = if (isSelected) {
            IconButtonDefaults.filledIconButtonColors().copy(
                containerColor = ElementTheme.colors.bgCanvasDefault,
                contentColor = ElementTheme.colors.iconPrimary,
            )
        } else {
            IconButtonDefaults.filledIconButtonColors().copy(
                containerColor = Color.Transparent,
                contentColor = ElementTheme.colors.iconSecondary,
            )
        }
        BadgedBox(
            badge = {
                counter?.let {
                    CounterAtom(
                        count = it,
                        // Tweak the offset so it's centered on the icon
                        modifier = Modifier.offset(x = (-16).dp, y = 10.dp),
                        contentPadding = PaddingValues(vertical = 0.5.dp),
                        textStyle = ElementTheme.typography.fontBodySmMedium,
                    )
                }
            }
        ) {
            IconButton(
                modifier = Modifier.widthIn(min = 56.dp),
                colors = colors,
                onClick = onClick,
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = icon,
                    contentDescription = tooltipLabel,
                )
            }
        }
    }
}

@Composable
fun HorizontalFloatingToolbarSeparator(modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.width(16.dp))
}

@PreviewsDayNight
@Composable
internal fun HorizontalFloatingToolbarPreview() = ElementPreview {
    Column {
        ContentToPreview(
            count = 1,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {},
                ) {
                    Icon(
                        imageVector = CompoundIcons.Plus(),
                        contentDescription = null,
                    )
                }
            }
        )
        ContentToPreview(
            count = 99,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {},
                ) {
                    Icon(
                        imageVector = CompoundIcons.Plus(),
                        contentDescription = null,
                    )
                }
            }
        )
        ContentToPreview(
            count = 999,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {},
                ) {
                    Icon(
                        imageVector = CompoundIcons.Plus(),
                        contentDescription = null,
                    )
                }
            }
        )
    }
}

@PreviewsDayNight
@Composable
internal fun HorizontalFloatingToolbarNoFabPreview() = ElementPreview {
    ContentToPreview(
        floatingActionButton = null,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ContentToPreview(
    count: Int? = 6,
    floatingActionButton: (@Composable () -> Unit)?,
) {
    HorizontalFloatingToolbar(
        modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
        floatingActionButton = floatingActionButton,
    ) {
        listOf(
            CompoundIcons.ChatSolid(),
            CompoundIcons.Space(),
        ).forEachIndexed { index, icon ->
            if (index > 0) {
                HorizontalFloatingToolbarSeparator()
            }
            HorizontalFloatingToolbarItem(
                icon = icon,
                tooltipLabel = "Label",
                isSelected = index == 0,
                counter = if (index == 0) count else null,
                forceRenderingTooltip = true,
                onClick = { },
            )
        }
    }
}
