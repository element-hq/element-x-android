/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.features.securebackup.impl.tools.RecoveryKeyVisualTransformation
import io.element.android.libraries.designsystem.modifiers.clickableIfNotNull
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.OutlinedTextField
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.autofill
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
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
                imageVector = CompoundIcons.Copy(),
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun RecoveryKeyFormContent(
    state: RecoveryKeyViewState,
    onChange: ((String) -> Unit)?,
    onSubmit: (() -> Unit)?,
) {
    onChange ?: error("onChange should not be null")
    onSubmit ?: error("onSubmit should not be null")
    val keyHasSpace = state.formattedRecoveryKey.orEmpty().contains(" ")
    val recoveryKeyVisualTransformation = remember(keyHasSpace) {
        // Do not apply a visual transformation if the key has spaces, to let user enter passphrase
        if (keyHasSpace) VisualTransformation.None else RecoveryKeyVisualTransformation()
    }
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTags.recoveryKey)
            .autofill(
                autofillTypes = listOf(AutofillType.Password),
                onFill = { onChange(it) },
            ),
        minLines = 2,
        value = state.formattedRecoveryKey.orEmpty(),
        onValueChange = onChange,
        enabled = state.inProgress.not(),
        visualTransformation = recoveryKeyVisualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
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
                        imageVector = CompoundIcons.InfoSolid(),
                        contentDescription = null,
                        tint = ElementTheme.colors.iconSecondary,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(20.dp),
                    )
                    Text(
                        text = stringResource(
                            id = if (state.recoveryKeyUserStory == RecoveryKeyUserStory.Change) {
                                R.string.screen_recovery_key_change_generate_key_description
                            } else {
                                R.string.screen_recovery_key_setup_generate_key_description
                            }
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
