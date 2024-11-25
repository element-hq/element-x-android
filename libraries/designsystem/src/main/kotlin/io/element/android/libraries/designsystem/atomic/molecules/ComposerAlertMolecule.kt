/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.anAvatarData
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toAnnotatedString
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.BooleanProvider
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun ComposerAlertMolecule(
    avatar: AvatarData,
    content: AnnotatedString,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCritical: Boolean = false,
    submitText: String = stringResource(CommonStrings.action_ok),
) {
    Column(
        modifier.fillMaxWidth()
    ) {
        val lineColor = if (isCritical) ElementTheme.colors.borderCriticalSubtle else ElementTheme.colors.borderInfoSubtle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(lineColor)
        )
        val startColor = if (isCritical) ElementTheme.colors.bgCriticalSubtle else ElementTheme.colors.bgInfoSubtle
        val brush = Brush.verticalGradient(
            listOf(startColor, ElementTheme.materialColors.background),
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
                    Avatar(
                        avatarData = avatar,
                    )
                    Text(
                        text = content,
                        modifier = Modifier.weight(1f),
                        style = ElementTheme.typography.fontBodyMdRegular,
                        color = ElementTheme.colors.textPrimary,
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

@PreviewsDayNight
@Composable
internal fun ComposerAlertMoleculePreview(@PreviewParameter(BooleanProvider::class) isCritical: Boolean) = ElementPreview {
    ComposerAlertMolecule(
        avatar = anAvatarData(size = AvatarSize.ComposerAlert),
        content = "Alice’s verified identity has changed. Learn more".toAnnotatedString(),
        isCritical = isCritical,
        onSubmitClick = {},
    )
}
