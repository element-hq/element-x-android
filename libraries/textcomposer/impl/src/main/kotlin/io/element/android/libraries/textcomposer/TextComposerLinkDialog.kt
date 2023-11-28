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

package io.element.android.libraries.textcomposer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.components.dialogs.ListDialog
import io.element.android.libraries.designsystem.components.list.TextFieldListItem
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.wysiwyg.view.models.LinkAction

@Composable
fun TextComposerLinkDialog(
    onDismissRequest: () -> Unit,
    linkAction: LinkAction,
    onSaveLinkRequest: (url: String) -> Unit,
    onCreateLinkRequest: (url: String, text: String) -> Unit,
    onRemoveLinkRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val urlToEdit by remember(linkAction) {
        derivedStateOf {
            (linkAction as? LinkAction.SetLink)?.currentUrl
        }
    }

    urlToEdit.let { url ->
        when {
            url != null -> {
                EditLinkDialog(
                    currentUrl = url,
                    onDismissRequest = onDismissRequest,
                    onSaveLinkRequest = onSaveLinkRequest,
                    onRemoveLinkRequest = onRemoveLinkRequest,
                    modifier = modifier,
                )
            }
            linkAction is LinkAction.InsertLink -> {
                CreateLinkWithTextDialog(
                    onDismissRequest = onDismissRequest,
                    onCreateLinkRequest = onCreateLinkRequest,
                    modifier = modifier,
                )
            }
            linkAction is LinkAction.SetLink -> {
                CreateLinkWithoutTextDialog(
                    onDismissRequest = onDismissRequest,
                    onSaveLinkRequest = onSaveLinkRequest,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun CreateLinkWithTextDialog(
    onDismissRequest: () -> Unit,
    onCreateLinkRequest: (url: String, text: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var linkText by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }

    val titleText = stringResource(R.string.rich_text_editor_create_link)

    fun onSubmit() {
        onCreateLinkRequest(linkUrl, linkText)
        onDismissRequest()
    }

    ListDialog(
        onDismissRequest = onDismissRequest,
        onSubmit = ::onSubmit,
        title = titleText,
        modifier = modifier
    ) {
        item {
            TextFieldListItem(
                placeholder = stringResource(id = CommonStrings.common_text),
                text = linkText,
                onTextChanged = { linkText = it },
            )
        }
        item {
            TextFieldListItem(
                placeholder = stringResource(id = R.string.rich_text_editor_url_placeholder),
                text = linkUrl,
                onTextChanged = { linkUrl = it },
            )
        }
    }
}

@Composable
private fun CreateLinkWithoutTextDialog(
    onDismissRequest: () -> Unit,
    onSaveLinkRequest: (url: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var linkUrl by remember { mutableStateOf("") }

    val titleText = stringResource(R.string.rich_text_editor_create_link)

    fun onSubmit() {
        onSaveLinkRequest(linkUrl)
        onDismissRequest()
    }

    ListDialog(
        onDismissRequest = onDismissRequest,
        onSubmit = ::onSubmit,
        title = titleText,
        modifier = modifier
    ) {
        item {
            TextFieldListItem(
                placeholder = stringResource(id = R.string.rich_text_editor_url_placeholder),
                text = linkUrl,
                onTextChanged = { linkUrl = it },
            )
        }
    }
}

// The edit link dialog does not yet support displaying or editing the text of a link
// https://github.com/matrix-org/matrix-rich-text-editor/issues/617
@Composable
private fun EditLinkDialog(
    currentUrl: String,
    onDismissRequest: () -> Unit,
    onSaveLinkRequest: (url: String) -> Unit,
    onRemoveLinkRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var linkUrl by remember { mutableStateOf(currentUrl) }

    val titleText = stringResource(R.string.rich_text_editor_edit_link)

    fun onSubmit() {
        onSaveLinkRequest(linkUrl)
        onDismissRequest()
    }

    fun onRemoveClicked() {
        onRemoveLinkRequest()
        onDismissRequest()
    }

    ListDialog(
        onDismissRequest = onDismissRequest,
        onSubmit = ::onSubmit,
        title = titleText,
        modifier = modifier
    ) {
        item {
            TextFieldListItem(
                placeholder = stringResource(id = R.string.rich_text_editor_url_placeholder),
                text = linkUrl,
                onTextChanged = { linkUrl = it },
            )
        }
        item {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.rich_text_editor_remove_link),
                        color = ElementTheme.colors.textCriticalPrimary
                    )
                },
                onClick = ::onRemoveClicked,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerLinkDialogCreateLinkPreview() = ElementPreview {
    TextComposerLinkDialog(
        onDismissRequest = {},
        linkAction = LinkAction.InsertLink,
        onSaveLinkRequest = {},
        onCreateLinkRequest = { _, _ -> },
        onRemoveLinkRequest = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TextComposerLinkDialogCreateLinkWithoutTextPreview() = ElementPreview {
    TextComposerLinkDialog(
        onDismissRequest = {},
        linkAction = LinkAction.SetLink(null),
        onSaveLinkRequest = {},
        onCreateLinkRequest = { _, _ -> },
        onRemoveLinkRequest = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TextComposerLinkDialogEditLinkPreview() = ElementPreview {
    TextComposerLinkDialog(
        onDismissRequest = {},
        linkAction = LinkAction.SetLink("https://element.io"),
        onSaveLinkRequest = {},
        onCreateLinkRequest = { _, _ -> },
        onRemoveLinkRequest = {},
    )
}

