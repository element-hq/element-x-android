/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.api.pollcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun PollTitleView(
    title: String,
    isPollEnded: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isPollEnded) {
            Icon(
                imageVector = CompoundIcons.PollsEnd(),
                contentDescription = stringResource(id = CommonStrings.a11y_poll_end),
                modifier = Modifier.size(22.dp)
            )
        } else {
            Icon(
                imageVector = CompoundIcons.Polls(),
                contentDescription = stringResource(id = CommonStrings.a11y_poll),
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = title,
            style = ElementTheme.typography.fontBodyLgMedium
        )
    }
}

@PreviewsDayNight
@Composable
internal fun PollTitleViewPreview() = ElementPreview {
    PollTitleView(
        title = "What is your favorite color?",
        isPollEnded = false
    )
}
