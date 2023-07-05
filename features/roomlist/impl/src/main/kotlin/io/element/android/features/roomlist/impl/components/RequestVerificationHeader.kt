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

package io.element.android.features.roomlist.impl.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.element.android.libraries.designsystem.preview.ElementPreviews
import androidx.compose.ui.unit.dp
import io.element.android.features.roomlist.impl.R
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun RequestVerificationHeader(
    onVerifyClicked: () -> Unit,
    onDismissClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Surface(
            modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row {
                    Text(
                        stringResource(R.string.session_verification_banner_title),
                        modifier = Modifier.weight(1f),
                        style = ElementTextStyles.Bold.body,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                    )
                    Icon(
                        modifier = Modifier.clickable(onClick = onDismissClicked),
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(CommonStrings.action_close)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.session_verification_banner_message),
                    style = ElementTextStyles.Regular.bodyMD
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 7.dp),
                    onClick = onVerifyClicked,
                ) {
                    Text(
                        stringResource(CommonStrings.action_continue),
                        style = ElementTextStyles.Button
                    )
                }
            }
        }
    }
}

@ElementPreviews
@Composable
internal fun PreviewRequestVerificationHeaderLight() {
    ElementPreview {
        RequestVerificationHeader(onVerifyClicked = {}, onDismissClicked = {})
    }
}

@ElementPreviews
@Composable
internal fun PreviewRequestVerificationHeaderDark() {
    ElementPreviewDark {
        RequestVerificationHeader(onVerifyClicked = {}, onDismissClicked = {})
    }
}
