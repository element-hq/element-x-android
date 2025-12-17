/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun RecoveryKeyView(
    state: RecoveryKeyViewState,
    onClick: (() -> Unit)?,
    onChange: ((String) -> Unit)?,
    onSubmit: (() -> Unit)?,
    toggleRecoveryKeyVisibility: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = CommonStrings.common_recovery_key),
            style = ElementTheme.typography.fontBodyMdRegular,
        )
        RecoveryKeyContent(state, onClick, onChange, onSubmit, toggleRecoveryKeyVisibility)
        RecoveryKeyFooter(state)
    }
}

@Composable
private fun RecoveryKeyContent(
    state: RecoveryKeyViewState,
    onClick: (() -> Unit)?,
    onChange: ((String) -> Unit)?,
    onSubmit: (() -> Unit)?,
    toggleRecoveryKeyVisibility: (Boolean) -> Unit,
) {
    when (state.recoveryKeyUserStory) {
        RecoveryKeyUserStory.Setup,
        RecoveryKeyUserStory.Change -> RecoveryKeyStaticContent(state, onClick)
        RecoveryKeyUserStory.Enter -> RecoveryKeyFormContent(
            state = state,
            toggleRecoveryKeyVisibility = toggleRecoveryKeyVisibility,
            onChange = onChange,
            onSubmit = onSubmit,
        )
    }
}

@Composable
private fun RecoveryKeyStaticContent(
    state: RecoveryKeyViewState,
    onClick: (() -> Unit)?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                color = ElementTheme.colors.bgSubtleSecondary,
            )
            .clickableIfNotNull(onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (state.formattedRecoveryKey != null) {
            RecoveryKeyWithCopy(
                recoveryKey = state.formattedRecoveryKey,
                alpha = 1f,
            )
        } else {
            // Use an invisible recovery key to ensure that the Box size is correct.
            val fakeFormattedRecoveryKey = List(12) { "XXXX" }.joinToString(" ")
            RecoveryKeyWithCopy(
                recoveryKey = fakeFormattedRecoveryKey,
                alpha = 0f,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
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
private fun RecoveryKeyWithCopy(
    recoveryKey: String,
    alpha: Float,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = recoveryKey,
            color = ElementTheme.colors.textSecondary,
            style = ElementTheme.typography.fontBodyLgRegular.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = CompoundIcons.Copy(),
            contentDescription = stringResource(id = CommonStrings.action_copy),
            tint = ElementTheme.colors.iconSecondary,
        )
    }
}

@Composable
private fun RecoveryKeyFormContent(
    state: RecoveryKeyViewState,
    toggleRecoveryKeyVisibility: (Boolean) -> Unit,
    onChange: ((String) -> Unit)?,
    onSubmit: (() -> Unit)?,
) {
    onChange ?: error("onChange should not be null")
    onSubmit ?: error("onSubmit should not be null")
    if (state.inProgress) {
        // Ensure recovery key is hidden when user submits the form
        toggleRecoveryKeyVisibility(false)
    }
    val keyHasSpace = state.formattedRecoveryKey.orEmpty().contains(" ")
    val recoveryKeyVisualTransformation = remember(keyHasSpace, state.displayTextFieldContents) {
        if (state.displayTextFieldContents) {
            // Do not apply a visual transformation if the key has spaces, to let user enter passphrase
            if (keyHasSpace) VisualTransformation.None else RecoveryKeyVisualTransformation()
        } else {
            PasswordVisualTransformation()
        }
    }
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTags.recoveryKey)
            .semantics {
                contentType = ContentType.Password
            },
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
        placeholder = stringResource(id = R.string.screen_recovery_key_confirm_key_placeholder),
        trailingIcon = {
            val image =
                if (state.displayTextFieldContents) CompoundIcons.VisibilityOn() else CompoundIcons.VisibilityOff()
            val description =
                if (state.displayTextFieldContents) stringResource(CommonStrings.a11y_hide_password) else stringResource(CommonStrings.a11y_show_password)
            Box(Modifier.clickable { toggleRecoveryKeyVisibility(!state.displayTextFieldContents) }) {
                Icon(
                    imageVector = image,
                    contentDescription = description,
                )
            }
        },
    )
}

@Composable
private fun RecoveryKeyFooter(state: RecoveryKeyViewState) {
    when (state.recoveryKeyUserStory) {
        RecoveryKeyUserStory.Setup,
        RecoveryKeyUserStory.Change -> {
            if (state.formattedRecoveryKey == null) {
                Text(
                    text = stringResource(
                        id = if (state.recoveryKeyUserStory == RecoveryKeyUserStory.Change) {
                            R.string.screen_recovery_key_change_generate_key_description
                        } else {
                            R.string.screen_recovery_key_setup_generate_key_description
                        }
                    ),
                    color = ElementTheme.colors.textSecondary,
                    style = ElementTheme.typography.fontBodySmRegular,
                )
            } else {
                Text(
                    text = stringResource(id = R.string.screen_recovery_key_save_key_description),
                    color = ElementTheme.colors.textSecondary,
                    style = ElementTheme.typography.fontBodySmRegular,
                )
            }
        }
        RecoveryKeyUserStory.Enter -> {
            Text(
                text = stringResource(id = R.string.screen_recovery_key_confirm_key_description),
                color = ElementTheme.colors.textSecondary,
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
        toggleRecoveryKeyVisibility = {},
    )
}
