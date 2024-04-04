/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.securebackup.impl.createkey

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.PageTitle
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.modifiers.squareSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewRecoveryKeyView(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = {}, navigationIcon = { BackButton(onClick = onBackClicked) })
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            PageTitle(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 40.dp),
                title = stringResource(R.string.screen_create_new_recovery_key_title),
                iconStyle = BigIcon.Style.Default(CompoundIcons.Computer())
            )
            Content()
        }
    }
}

@Composable
private fun Content() {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Item(index = 1, text = AnnotatedString(stringResource(R.string.screen_create_new_recovery_key_list_item_1)))
        Item(index = 2, text = AnnotatedString(stringResource(R.string.screen_create_new_recovery_key_list_item_2)))
        Item(
            index = 3,
            text = buildAnnotatedString {
                val resetAllAction = stringResource(R.string.screen_create_new_recovery_key_list_item_3_reset_all)
                val text = stringResource(R.string.screen_create_new_recovery_key_list_item_3, resetAllAction)
                append(text)
                val start = text.indexOf(resetAllAction)
                val end = start + resetAllAction.length
                if (start in text.indices && end in text.indices) {
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                }
            }
        )
        Item(index = 4, text = AnnotatedString(stringResource(R.string.screen_create_new_recovery_key_list_item_4)))
        Item(index = 5, text = AnnotatedString(stringResource(R.string.screen_create_new_recovery_key_list_item_5)))
    }
}

@Composable
private fun Item(index: Int, text: AnnotatedString) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ItemNumber(index = index)
        Text(text = text, style = ElementTheme.typography.fontBodyMdRegular, color = ElementTheme.colors.textPrimary)
    }
}

@Composable
private fun ItemNumber(
    index: Int,
) {
    val color = ElementTheme.colors.textPlaceholder
    Box(
        modifier = Modifier
            .border(1.dp, color, CircleShape)
            .squareSize()
    ) {
        Text(
            modifier = Modifier.padding(1.5.dp),
            text = index.toString(),
            style = ElementTheme.typography.fontBodySmRegular,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun ItemNumberPreview() {
    ElementPreview {
        CreateNewRecoveryKeyView(
            onBackClicked = {},
        )
    }
}
