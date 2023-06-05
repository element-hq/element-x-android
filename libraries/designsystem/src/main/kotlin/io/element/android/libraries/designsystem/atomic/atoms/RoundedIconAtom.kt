/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.LocalColors
import io.element.android.libraries.designsystem.theme.components.Icon

enum class RoundedIconAtomSize {
    Medium,
    Large
}

@Composable
fun RoundedIconAtom(
    modifier: Modifier = Modifier,
    size: RoundedIconAtomSize = RoundedIconAtomSize.Large,
    resourceId: Int? = null,
    imageVector: ImageVector? = null,
    tint: Color = MaterialTheme.colorScheme.secondary
) {
    Box(
        modifier = modifier
            .size(size.toContainerSize())
            .background(
                color = LocalColors.current.quinary,
                shape = RoundedCornerShape(size.toCornerSize())
            )
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.Center)
                .size(size.toIconSize()),
            tint = tint,
            resourceId = resourceId,
            imageVector = imageVector,
            contentDescription = "",
        )
    }
}

private fun RoundedIconAtomSize.toContainerSize(): Dp {
    return when (this) {
        RoundedIconAtomSize.Medium -> 30.dp
        RoundedIconAtomSize.Large -> 70.dp
    }
}

private fun RoundedIconAtomSize.toCornerSize(): Dp {
    return when (this) {
        RoundedIconAtomSize.Medium -> 8.dp
        RoundedIconAtomSize.Large -> 14.dp
    }
}

private fun RoundedIconAtomSize.toIconSize(): Dp {
    return when (this) {
        RoundedIconAtomSize.Medium -> 16.dp
        RoundedIconAtomSize.Large -> 48.dp
    }
}

@Preview
@Composable
internal fun RoundedIconAtomLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun RoundedIconAtomDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RoundedIconAtom(
            size = RoundedIconAtomSize.Medium,
            imageVector = Icons.Filled.Home,
        )
        RoundedIconAtom(
            size = RoundedIconAtomSize.Large,
            imageVector = Icons.Filled.Home,
        )
    }
}
