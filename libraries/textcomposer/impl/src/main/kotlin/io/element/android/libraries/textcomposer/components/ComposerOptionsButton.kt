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

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.textcomposer.R
import io.element.android.compound.theme.ElementTheme

@Composable
internal fun ComposerOptionsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier
            .size(48.dp),
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.size(30.dp),
            resourceId = CommonDrawables.ic_plus_composer,
            contentDescription = stringResource(R.string.rich_text_editor_a11y_add_attachment),
            tint = ElementTheme.colors.iconPrimary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun ComposerOptionsButtonPreview() = ElementPreview {
    ComposerOptionsButton(onClick = {})
}

