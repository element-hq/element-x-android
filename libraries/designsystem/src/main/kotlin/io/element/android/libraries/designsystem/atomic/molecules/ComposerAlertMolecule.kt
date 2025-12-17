/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toAnnotatedString
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun ComposerAlertMolecule(
    avatar: AvatarData?,
    content: AnnotatedString,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
    level: ComposerAlertLevel = ComposerAlertLevel.Default,
    showIcon: Boolean = false,
    submitText: String = stringResource(CommonStrings.action_ok),
) {
    Column(
        modifier.fillMaxWidth()
    ) {
        val lineColor = when (level) {
            ComposerAlertLevel.Default -> ElementTheme.colors.borderInfoSubtle
            ComposerAlertLevel.Info -> ElementTheme.colors.borderInfoSubtle
            ComposerAlertLevel.Critical -> ElementTheme.colors.borderCriticalSubtle
        }

        val startColor = when (level) {
            ComposerAlertLevel.Default -> ElementTheme.colors.bgInfoSubtle
            ComposerAlertLevel.Info -> ElementTheme.colors.bgInfoSubtle
            ComposerAlertLevel.Critical -> ElementTheme.colors.bgCriticalSubtle
        }

        val textColor = when (level) {
            ComposerAlertLevel.Default -> ElementTheme.colors.textPrimary
            ComposerAlertLevel.Info -> ElementTheme.colors.textInfoPrimary
            ComposerAlertLevel.Critical -> ElementTheme.colors.textCriticalPrimary
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(lineColor)
        )
        val brush = Brush.verticalGradient(
            listOf(startColor, ElementTheme.colors.bgCanvasDefault),
        )
        Box(
            modifier = Modifier
                .background(brush)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (avatar != null) {
                        Avatar(
                            avatarData = avatar,
                            avatarType = AvatarType.User,
                        )
                    } else if (showIcon) {
                        val icon = when (level) {
                            ComposerAlertLevel.Default -> CompoundIcons.Info()
                            ComposerAlertLevel.Info -> CompoundIcons.Info()
                            ComposerAlertLevel.Critical -> CompoundIcons.Error()
                        }
                        val iconTint = when (level) {
                            ComposerAlertLevel.Default -> ElementTheme.colors.iconPrimary
                            ComposerAlertLevel.Info -> ElementTheme.colors.iconInfoPrimary
                            ComposerAlertLevel.Critical -> ElementTheme.colors.iconCriticalPrimary
                        }
                        Icon(
                            imageVector = icon,
                            tint = iconTint,
                            contentDescription = null,
                        )
                    }
                    Text(
                        text = content,
                        modifier = Modifier.weight(1f),
                        style = ElementTheme.typography.fontBodyMdRegular,
                        color = textColor,
                        textAlign = TextAlign.Start,
                    )
                }
                Button(
                    text = submitText,
                    size = ButtonSize.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSubmitClick,
                )
            }
        }
    }
}

enum class ComposerAlertLevel {
    Default,
    Info,
    Critical
}

@PreviewsDayNight
@Composable
internal fun ComposerAlertMoleculePreview(
    @PreviewParameter(ComposerAlertMoleculeParamsProvider::class) params: ComposerAlertMoleculeParams,
) = ElementPreview {
    ComposerAlertMolecule(
        avatar = params.avatar,
        content = "Alice’s verified identity has changed. Learn more".toAnnotatedString(),
        level = params.level,
        showIcon = params.showIcon,
        onSubmitClick = {},
    )
}
