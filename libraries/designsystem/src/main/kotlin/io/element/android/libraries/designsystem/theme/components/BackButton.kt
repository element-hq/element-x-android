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

package io.element.android.libraries.designsystem.theme.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.R as DrawableR
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = DrawableR.drawable.ic_arrow_back,
    contentDescription: String = stringResource(StringR.string.a11y_back),
    enabled: Boolean = true
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(resourceId = icon, contentDescription = contentDescription)
    }
}

@Preview
@Composable
internal fun BackButtonPreviewLight() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun BackButtonPreviewDark() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Column {
        BackButton(onClick = { }, enabled = true, contentDescription = "Back")
        BackButton(onClick = { }, enabled = false, contentDescription = "Back")
    }
}
