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

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.ui.model.MatrixUser
import io.element.android.libraries.matrix.ui.model.getBestName

// FIXME Row are not the same height if there is a name or not.
@Composable
fun MatrixUserRow(
    matrixUser: MatrixUser,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            matrixUser.avatarData,
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)
                .alignByBaseline()
                .weight(1f)
        ) {
            // Name
            Text(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                text = matrixUser.getBestName(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
            )
            // Id
            if (matrixUser.username.isNullOrEmpty().not()) {
                Text(
                    text = matrixUser.id.value,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
fun MatrixUserRowLightPreview(@PreviewParameter(MatrixUserPreviewParameterProvider::class) matrixUser: MatrixUser) =
    ElementPreviewLight { ContentToPreview(matrixUser) }

@Preview
@Composable
fun MatrixUserRowDarkPreview(@PreviewParameter(MatrixUserPreviewParameterProvider::class) matrixUser: MatrixUser) =
    ElementPreviewDark { ContentToPreview(matrixUser) }

@Composable
private fun ContentToPreview(matrixUser: MatrixUser) {
    MatrixUserRow(matrixUser)
}
