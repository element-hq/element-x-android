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

package io.element.android.libraries.designsystem.atomic.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.LocalColors
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun IconTitleSubtitleMolecule(
    iconResourceId: Int,
    title: String,
    subTitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Box(
            modifier = Modifier
                .size(width = 70.dp, height = 70.dp)
                .align(Alignment.CenterHorizontally)
                .background(
                    color = LocalColors.current.quinary,
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 48.dp, height = 48.dp),
                tint = MaterialTheme.colorScheme.secondary,
                resourceId = iconResourceId,
                contentDescription = "",
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = ElementTextStyles.Bold.title2,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subTitle,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = ElementTextStyles.Regular.subheadline,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Preview
@Composable
internal fun IconTitleSubtitleMoleculeLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun IconTitleSubtitleMoleculeDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    IconTitleSubtitleMolecule(
        iconResourceId = R.drawable.ic_edit,
        title = "Title",
        subTitle = "Sub iitle",
    )
}
