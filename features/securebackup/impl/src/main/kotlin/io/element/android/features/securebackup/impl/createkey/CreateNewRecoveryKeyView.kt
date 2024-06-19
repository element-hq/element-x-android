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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.designsystem.atomic.organisms.NumberedListOrganism
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.PageTitle
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.annotatedTextWithBold
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewRecoveryKeyView(
    desktopApplicationName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = {}, navigationIcon = { BackButton(onClick = onBackClick) })
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
            Content(desktopApplicationName = desktopApplicationName)
        }
    }
}

@Composable
private fun Content(desktopApplicationName: String) {
    val listItems = buildList {
        add(AnnotatedString(stringResource(R.string.screen_create_new_recovery_key_list_item_1, desktopApplicationName)))
        add(AnnotatedString(stringResource(R.string.screen_create_new_recovery_key_list_item_2)))
        add(
            annotatedTextWithBold(
                text = stringResource(
                    id = R.string.screen_create_new_recovery_key_list_item_3,
                    stringResource(R.string.screen_create_new_recovery_key_list_item_3_reset_all)
                ),
                boldText = stringResource(R.string.screen_create_new_recovery_key_list_item_3_reset_all)
            )
        )
        add(AnnotatedString(stringResource(R.string.screen_create_new_recovery_key_list_item_4)))
        add(AnnotatedString(stringResource(R.string.screen_create_new_recovery_key_list_item_5)))
    }
    NumberedListOrganism(modifier = Modifier.padding(horizontal = 16.dp), items = listItems.toImmutableList())
}

@PreviewsDayNight
@Composable
internal fun CreateNewRecoveryKeyViewPreview() {
    ElementPreview {
        CreateNewRecoveryKeyView(
            desktopApplicationName = "Element",
            onBackClick = {},
        )
    }
}
