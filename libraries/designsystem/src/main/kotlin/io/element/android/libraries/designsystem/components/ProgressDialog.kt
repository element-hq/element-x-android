/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.DialogPreview
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.ui.strings.CommonStrings
import timber.log.Timber

@Composable
fun ProgressDialog(
    modifier: Modifier = Modifier,
    text: String? = null,
    type: ProgressDialogType = ProgressDialogType.Indeterminate,
    properties: DialogProperties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    showCancelButton: Boolean = false,
    onDismissRequest: () -> Unit = {},
) {
    DisposableEffect(Unit) {
        onDispose {
            Timber.v("OnDispose progressDialog")
        }
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        ProgressDialogContent(
            modifier = modifier,
            text = text,
            showCancelButton = showCancelButton,
            onCancelClick = onDismissRequest,
            progressIndicator = {
                when (type) {
                    is ProgressDialogType.Indeterminate -> {
                        CircularProgressIndicator(
                            color = ElementTheme.colors.iconPrimary
                        )
                    }
                    is ProgressDialogType.Determinate -> {
                        CircularProgressIndicator(
                            progress = { type.progress },
                            color = ElementTheme.colors.iconPrimary
                        )
                    }
                }
            }
        )
    }
}

@Immutable
sealed interface ProgressDialogType {
    data class Determinate(val progress: Float) : ProgressDialogType
    data object Indeterminate : ProgressDialogType
}

@Composable
private fun ProgressDialogContent(
    modifier: Modifier = Modifier,
    text: String? = null,
    showCancelButton: Boolean = false,
    onCancelClick: () -> Unit = {},
    progressIndicator: @Composable () -> Unit = {
        CircularProgressIndicator(
            color = ElementTheme.colors.iconPrimary
        )
    }
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 38.dp, bottom = 32.dp, start = 40.dp, end = 40.dp)
        ) {
            progressIndicator()
            if (!text.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = text,
                    color = ElementTheme.colors.textPrimary,
                )
            }
            if (showCancelButton) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    TextButton(
                        text = stringResource(id = CommonStrings.action_cancel),
                        onClick = onCancelClick,
                    )
                }
            }
        }
    }
}

@Preview(group = PreviewGroup.Dialogs)
@Composable
internal fun ProgressDialogContentPreview() = ElementThemedPreview {
    DialogPreview {
        ProgressDialogContent(text = "test dialog content", showCancelButton = true)
    }
}

@PreviewsDayNight
@Composable
internal fun ProgressDialogPreview() = ElementPreview {
    ProgressDialog(text = "test dialog content", showCancelButton = true)
}
