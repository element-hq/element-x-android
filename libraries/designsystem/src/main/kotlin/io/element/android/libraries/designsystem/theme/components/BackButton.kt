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

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.ui.strings.R.string as StringR

@Composable
fun BackButton(
    action: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.ArrowBack,
    contentDescription: String = stringResource(StringR.action_back),
    enabled: Boolean = true
) {
    IconButton(
        modifier = modifier,
        onClick = action,
        enabled = enabled,
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
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
        BackButton(action = { }, enabled = true, contentDescription = "Back")
        BackButton(action = { }, enabled = false, contentDescription = "Back")
    }
}
