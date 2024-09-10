/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.atoms.RoundedIconAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoundedIconAtomSize
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * IconTitleSubtitleMolecule is a molecule which displays an icon, a title and a subtitle.
 *
 * @param title the title to display
 * @param subTitle the subtitle to display
 * @param modifier the modifier to apply to this layout
 * @param iconResourceId the resource id of the icon to display, exclusive with [iconImageVector]
 * @param iconImageVector the image vector of the icon to display, exclusive with [iconResourceId]
 * @param iconTint the tint to apply to the icon
 */
@Composable
fun IconTitleSubtitleMolecule(
    title: String,
    subTitle: String?,
    modifier: Modifier = Modifier,
    iconResourceId: Int? = null,
    iconImageVector: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
) {
    Column(modifier) {
        RoundedIconAtom(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            size = RoundedIconAtomSize.Large,
            resourceId = iconResourceId,
            imageVector = iconImageVector,
            tint = iconTint,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = ElementTheme.typography.fontHeadingMdBold,
            color = MaterialTheme.colorScheme.primary,
        )
        if (subTitle != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = subTitle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = ElementTheme.typography.fontBodyMdRegular,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun IconTitleSubtitleMoleculePreview() = ElementPreview {
    IconTitleSubtitleMolecule(
        iconImageVector = CompoundIcons.Chat(),
        title = "Title",
        subTitle = "Subtitle",
    )
}

@PreviewsDayNight
@Composable
internal fun IconTitleSubtitleMoleculeWithResIconPreview() = ElementPreview {
    IconTitleSubtitleMolecule(
        iconResourceId = CompoundDrawables.ic_compound_admin,
        iconTint = Color.Black,
        title = "Title",
        subTitle = "Subtitle",
    )
}
