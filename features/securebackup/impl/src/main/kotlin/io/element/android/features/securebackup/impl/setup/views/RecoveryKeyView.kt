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

package io.element.android.features.securebackup.impl.setup.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.securebackup.impl.R
import io.element.android.features.securebackup.impl.tools.RecoveryKeyVisualTransformation
import io.element.android.libraries.designsystem.modifiers.clickableIfNotNull
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.OutlinedTextField
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun RecoveryKeyView(
    state: RecoveryKeyViewState,
    onClick: (() -> Unit)?,
    onChange: ((String) -> Unit)?,
    onSubmit: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = CommonStrings.common_recovery_key),
            modifier = Modifier.padding(start = 16.dp),
            style = ElementTheme.typography.fontBodyMdRegular,
        )
        RecoveryKeyContent(state, onClick, onChange, onSubmit)
        RecoveryKeyFooter(state)
    }
}

@Composable
private fun RecoveryKeyContent(
    state: RecoveryKeyViewState,
    onClick: (() -> Unit)?,
    onChange: ((String) -> Unit)?,
    onSubmit: (() -> Unit)?,
) {
    when (state.recoveryKeyUserStory) {
        RecoveryKeyUserStory.Setup,
        RecoveryKeyUserStory.Change -> RecoveryKeyStaticContent(state, onClick)
        RecoveryKeyUserStory.Enter -> RecoveryKeyFormContent(state, onChange, onSubmit)
    }
}

@Composable
private fun RecoveryKeyStaticContent(
    state: RecoveryKeyViewState,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                color = ElementTheme.colors.bgSubtleSecondary,
                shape = RoundedCornerShape(14.dp)
            )
            .clickableIfNotNull(onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.formattedRecoveryKey != null) {
            Text(
                text = state.formattedRecoveryKey,
                modifier = Modifier.weight(1f),
            )
            Icon(
                resourceId = CommonDrawables.ic_september_copy,
                contentDescription = stringResource(id = CommonStrings.action_copy),
                tint = ElementTheme.colors.iconSecondary,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 11.dp)
            ) {
                if (state.inProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .progressSemantics()
                            .padding(end = 8.dp)
                            .size(16.dp),
                        color = ElementTheme.colors.textPrimary,
                        strokeWidth = 1.5.dp,
                    )
                }
                Text(
                    text = stringResource(
                        id = when {
                            state.inProgress -> R.string.screen_recovery_key_generating_key
                            state.recoveryKeyUserStory == RecoveryKeyUserStory.Change -> R.string.screen_recovery_key_change_generate_key
                            else -> R.string.screen_recovery_key_setup_generate_key
                        }
                    ),
                    textAlign = TextAlign.Center,
                    style = ElementTheme.typography.fontBodyLgMedium,
                )
            }
        }
    }
}

@Composable
private fun RecoveryKeyFormContent(
    state: RecoveryKeyViewState,
    onChange: ((String) -> Unit)?,
    onSubmit: (() -> Unit)?,
) {
    onChange ?: error("onChange should not be null")
    onSubmit ?: error("onSubmit should not be null")
    val recoveryKeyVisualTransformation = remember {
        RecoveryKeyVisualTransformation()
    }
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        value = state.formattedRecoveryKey.orEmpty(),
        onValueChange = onChange,
        enabled = state.inProgress.not(),
        visualTransformation = recoveryKeyVisualTransformation,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onSubmit() }
        ),
        label = { Text(text = stringResource(id = R.string.screen_recovery_key_confirm_key_placeholder)) }
    )
}

@Composable
private fun RecoveryKeyFooter(state: RecoveryKeyViewState) {
    when (state.recoveryKeyUserStory) {
        RecoveryKeyUserStory.Setup,
        RecoveryKeyUserStory.Change -> {
            if (state.formattedRecoveryKey == null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        resourceId = CommonDrawables.ic_compound_info,
                        contentDescription = null,
                        tint = ElementTheme.colors.iconSecondary,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(20.dp),
                    )
                    Text(
                        text = stringResource(
                            id = if (state.recoveryKeyUserStory == RecoveryKeyUserStory.Change)
                                R.string.screen_recovery_key_change_generate_key_description
                            else
                                R.string.screen_recovery_key_setup_generate_key_description
                        ),
                        color = ElementTheme.colors.textSecondary,
                        modifier = Modifier.padding(start = 8.dp),
                        style = ElementTheme.typography.fontBodySmRegular,
                    )
                }
            } else {
                Text(
                    text = stringResource(id = R.string.screen_recovery_key_save_key_description),
                    color = ElementTheme.colors.textSecondary,
                    modifier = Modifier.padding(start = 16.dp),
                    style = ElementTheme.typography.fontBodySmRegular,
                )
            }
        }
        RecoveryKeyUserStory.Enter -> {
            Text(
                text = stringResource(id = R.string.screen_recovery_key_confirm_key_description),
                color = ElementTheme.colors.textSecondary,
                modifier = Modifier.padding(start = 16.dp),
                style = ElementTheme.typography.fontBodySmRegular,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RecoveryKeyViewPreview(
    @PreviewParameter(RecoveryKeyViewStateProvider::class) state: RecoveryKeyViewState
) = ElementPreview {
    RecoveryKeyView(
        state = state,
        onClick = {},
        onChange = {},
        onSubmit = {},
    )
}
