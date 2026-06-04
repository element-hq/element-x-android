/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.enterprise.api.CustomRecoveryPassphraseStrength
import io.element.android.features.enterprise.api.CustomRecoveryPassphraseStrengthResult
import io.element.android.features.securebackup.impl.R
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyView
import io.element.android.libraries.androidutils.system.copyToClipboard
import io.element.android.libraries.androidutils.system.startSharePlainTextIntent
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.PasswordVisibilityToggle
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TextFieldValidity
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SecureBackupSetupView(
    state: SecureBackupSetupState,
    onSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Custom flow auto-skips the "save your key" screen: finish once the SDK accepted it.
    LaunchedEffect(state.setupState, state.isCustomEntry()) {
        if (state.isCustomEntry() && state.setupState is SetupState.CreatedAndSaved) {
            onSuccess()
        }
    }
    FlowStepPage(
        modifier = modifier,
        onBackClick = backClickHandler(state, onBackClick),
        title = title(state),
        subTitle = subtitle(state),
        iconStyle = BigIcon.Style.Default(CompoundIcons.KeySolid()),
        buttons = { Buttons(state, onFinish = onSuccess, onCancel = onBackClick) },
    ) {
        Content(state = state)
    }

    if (state.setupState is SetupState.Error) {
        ErrorDialog(
            title = stringResource(id = CommonStrings.common_something_went_wrong),
            content = stringResource(id = CommonStrings.common_something_went_wrong_message),
            onSubmit = {
                state.eventSink.invoke(SecureBackupSetupEvents.DismissDialog)
            },
        )
    }

    if (state.showSaveConfirmationDialog) {
        ConfirmationDialog(
            title = stringResource(id = R.string.screen_recovery_key_setup_confirmation_title),
            content = stringResource(id = R.string.screen_recovery_key_setup_confirmation_description),
            submitText = stringResource(id = CommonStrings.action_continue),
            onSubmitClick = onSuccess,
            onDismiss = {
                state.eventSink.invoke(SecureBackupSetupEvents.DismissDialog)
            }
        )
    }
}

private fun SecureBackupSetupState.canGoBack(): Boolean {
    return recoveryKeyViewState.formattedRecoveryKey == null
}

private fun SecureBackupSetupState.isCustomEntry(): Boolean =
    customRecoveryPassphraseRequirements != null

private fun backClickHandler(
    state: SecureBackupSetupState,
    onBackClick: () -> Unit,
): (() -> Unit)? {
    if (!state.canGoBack()) return null
    if (state.isCustomEntry()) {
        // Back while the SDK call is in flight aborts it and returns to the Confirm step
        // (typed input preserved) rather than silently flipping the step under the spinner.
        if (state.setupState is SetupState.Creating) {
            return { state.eventSink.invoke(SecureBackupSetupEvents.CancelCustomPassphraseSubmit) }
        }
        if (state.customEntryStep == CustomEntryStep.Confirm) {
            // In the custom flow, backing out of Confirm returns to Entry (preserve typed input).
            return { state.eventSink.invoke(SecureBackupSetupEvents.BackToCustomEntry) }
        }
    }
    return onBackClick
}

@Composable
private fun title(state: SecureBackupSetupState): String {
    // Hide the heading until the well-known resolves, so it can't flip copy under the user.
    if (!state.wellknownLoaded) return ""
    // Custom flow has no "save your key" step — use per-step custom titles throughout.
    if (state.isCustomEntry()) {
        return when (state.customEntryStep) {
            CustomEntryStep.Entry -> stringResource(id = R.string.pro_screen_recovery_key_mode_input_title)
            CustomEntryStep.Confirm -> stringResource(id = R.string.pro_screen_recovery_key_mode_confirm_title)
        }
    }
    return when (state.setupState) {
        SetupState.Init,
        SetupState.Creating,
        is SetupState.Error -> when {
            state.isChangeRecoveryKeyUserStory -> stringResource(id = R.string.screen_recovery_key_change_title)
            else -> stringResource(id = R.string.screen_recovery_key_setup_title)
        }
        is SetupState.Created,
        is SetupState.CreatedAndSaved ->
            stringResource(id = R.string.screen_recovery_key_save_title)
    }
}

@Composable
private fun subtitle(state: SecureBackupSetupState): String? {
    if (!state.wellknownLoaded) return null
    if (state.isCustomEntry()) {
        return when (state.customEntryStep) {
            CustomEntryStep.Entry -> stringResource(id = R.string.pro_screen_recovery_key_mode_input_description)
            CustomEntryStep.Confirm -> stringResource(id = R.string.pro_screen_recovery_key_mode_confirm_description)
        }
    }
    return when (state.setupState) {
        SetupState.Init,
        SetupState.Creating,
        is SetupState.Error -> when {
            state.isChangeRecoveryKeyUserStory -> stringResource(id = R.string.screen_recovery_key_change_description)
            else -> stringResource(id = R.string.screen_recovery_key_setup_description)
        }
        is SetupState.Created,
        is SetupState.CreatedAndSaved ->
            stringResource(id = R.string.screen_recovery_key_save_description)
    }
}

@Composable
private fun Content(
    state: SecureBackupSetupState,
) {
    // Spinner until the well-known resolves, so neither flow renders before we know which applies.
    if (!state.wellknownLoaded && state.setupState == SetupState.Init) {
        LoadingPlaceholder()
        return
    }
    if (state.isCustomEntry()) {
        when (state.setupState) {
            SetupState.Init,
            is SetupState.Error -> when (state.customEntryStep) {
                CustomEntryStep.Entry -> CustomPassphraseEntry(state = state)
                CustomEntryStep.Confirm -> CustomPassphraseConfirm(state = state)
            }
            // Hold the spinner through the auto-skip so the SDK base58 key is never shown/shared.
            SetupState.Creating,
            is SetupState.Created,
            is SetupState.CreatedAndSaved -> LoadingPlaceholder()
        }
        return
    }
    val context = LocalContext.current
    val formattedRecoveryKey = state.recoveryKeyViewState.formattedRecoveryKey
    val toastMessage = stringResource(R.string.screen_recovery_key_copied_to_clipboard)
    val clickLambda = if (formattedRecoveryKey != null) {
        {
            context.copyToClipboard(
                text = formattedRecoveryKey,
                toastMessage = toastMessage,
            )
            state.eventSink.invoke(SecureBackupSetupEvents.RecoveryKeyHasBeenSaved)
        }
    } else {
        if (!state.recoveryKeyViewState.inProgress) {
            {
                state.eventSink.invoke(SecureBackupSetupEvents.CreateRecoveryKey)
            }
        } else {
            null
        }
    }
    RecoveryKeyView(
        modifier = Modifier.padding(top = 52.dp),
        state = state.recoveryKeyViewState,
        onClick = clickLambda,
        onChange = null,
        onSubmit = null,
        toggleRecoveryKeyVisibility = {},
    )
}

@Composable
private fun LoadingPlaceholder() {
    val description = stringResource(id = R.string.a11y_recovery_key_loading_specs)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 52.dp)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        // CircularProgressIndicator applies progressSemantics() internally — no need to add it here.
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun CustomPassphraseEntry(state: SecureBackupSetupState) {
    val specs = state.customRecoveryPassphraseRequirements ?: return
    var passphraseVisible by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.customRecoveryPassphrase)
                .semantics { contentType = ContentType.NewPassword },
            value = state.customPassphrase,
            onValueChange = { state.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(it)) },
            label = stringResource(id = CommonStrings.common_recovery_key),
            singleLine = true,
            visualTransformation = if (passphraseVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                PasswordVisibilityToggle(
                    visible = passphraseVisible,
                    onToggle = { passphraseVisible = !passphraseVisible },
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (state.canContinueFromEntry) {
                        state.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
                    }
                },
            ),
            supportingText = stringResource(
                id = R.string.pro_screen_recovery_key_mode_input_passphrase_field_footer,
                specs.minCharacterCount,
            ),
        )
        // Always render the strength row in the Entry step (matches iOS): an empty field shows the
        // "Strength" base label with an empty bar; typing flips it to the per-level label + bar.
        Spacer(modifier = Modifier.height(8.dp))
        PassphraseStrengthIndicator(
            result = state.customPassphraseStrength,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PassphraseStrengthIndicator(
    result: CustomRecoveryPassphraseStrengthResult?,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(
        id = result?.strength?.labelRes() ?: R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_base,
    )
    val hint = result?.strength?.hintRes()?.let { stringResource(id = it) }
    val score = result?.score ?: 0f
    // Mirror iOS: a zero score (empty field or the Garbage tier) stays neutral; otherwise the
    // label and bar share a single colour interpolated along the red→orange→yellow→green gradient.
    val color = if (score <= 0f) ElementTheme.colors.textSecondary else passphraseStrengthColor(score)
    val announcement = stringResource(id = R.string.a11y_recovery_key_custom_strength_announcement, label)
        .let { if (hint != null) "$it. $hint" else it }
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = announcement
        },
    ) {
        Text(
            text = label,
            color = color,
            style = ElementTheme.typography.fontBodySmMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score },
            modifier = Modifier.fillMaxWidth(),
            color = color,
        )
        if (hint != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = hint,
                color = ElementTheme.colors.textSecondary,
                style = ElementTheme.typography.fontBodySmRegular,
            )
        }
    }
}

private fun CustomRecoveryPassphraseStrength.labelRes(): Int = when (this) {
    CustomRecoveryPassphraseStrength.Garbage -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_garbage
    CustomRecoveryPassphraseStrength.Weak -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_weak
    CustomRecoveryPassphraseStrength.Moderate -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_moderate
    CustomRecoveryPassphraseStrength.Okay -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_okay
    CustomRecoveryPassphraseStrength.Strong -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_strong
    CustomRecoveryPassphraseStrength.VeryStrong -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_very_strong
    CustomRecoveryPassphraseStrength.UltraStrong -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_ultra_strong
    CustomRecoveryPassphraseStrength.Mega -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_mega
}

private fun CustomRecoveryPassphraseStrength.hintRes(): Int = when (this) {
    CustomRecoveryPassphraseStrength.Garbage -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_hint_garbage
    CustomRecoveryPassphraseStrength.Weak -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_hint_weak
    CustomRecoveryPassphraseStrength.Moderate -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_hint_moderate
    CustomRecoveryPassphraseStrength.Okay -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_hint_okay
    CustomRecoveryPassphraseStrength.Strong -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_hint_strong
    CustomRecoveryPassphraseStrength.VeryStrong -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_hint_very_strong
    CustomRecoveryPassphraseStrength.UltraStrong -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_hint_ultra_strong
    CustomRecoveryPassphraseStrength.Mega -> R.string.pro_screen_recovery_key_mode_input_passphrase_strength_label_hint_mega
}

// Stops mirror iOS PasswordStrengthBar.gradient: Compound red900 / lime700 with the same vivid
// orange & yellow midpoints. Intentionally fixed (not light/dark semantic) so the gradient reads
// identically to iOS in both themes.
private val passphraseStrengthGradientStops = listOf(
    0.25f to Color(0xFFD51928), // Compound red900
    0.5f to Color(0xFFFF9500), // orange
    0.75f to Color(0xFFFFCC00), // yellow
    1.0f to Color(0xFF54C424), // Compound lime700
)

/** Interpolates the strength bar colour along [passphraseStrengthGradientStops] for a 0f..1f score. */
private fun passphraseStrengthColor(score: Float): Color {
    val clamped = score.coerceIn(0f, 1f)
    var previousLocation = 0f
    var previousColor = passphraseStrengthGradientStops.first().second
    for ((location, color) in passphraseStrengthGradientStops) {
        if (clamped <= location) {
            val span = location - previousLocation
            if (span <= 0f) return color
            return lerp(previousColor, color, ((clamped - previousLocation) / span).coerceIn(0f, 1f))
        }
        previousLocation = location
        previousColor = color
    }
    return previousColor
}

@Composable
private fun CustomPassphraseConfirm(state: SecureBackupSetupState) {
    var passphraseVisible by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.customRecoveryPassphraseConfirm)
                .semantics { contentType = ContentType.NewPassword },
            value = state.customPassphraseConfirm,
            onValueChange = { state.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphraseConfirm(it)) },
            label = stringResource(id = CommonStrings.common_recovery_key),
            singleLine = true,
            visualTransformation = if (passphraseVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                PasswordVisibilityToggle(
                    visible = passphraseVisible,
                    onToggle = { passphraseVisible = !passphraseVisible },
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (state.canSubmitCustomPassphrase) {
                        state.eventSink.invoke(SecureBackupSetupEvents.SubmitCustomPassphrase)
                    }
                },
            ),
            validity = if (state.customPassphraseMismatch) TextFieldValidity.Invalid else TextFieldValidity.None,
            supportingText = if (state.customPassphraseMismatch) {
                stringResource(id = R.string.pro_screen_recovery_key_mode_custom_mismatch)
            } else {
                null
            },
        )
    }
}

@Composable
private fun ColumnScope.Buttons(
    state: SecureBackupSetupState,
    onFinish: () -> Unit,
    onCancel: () -> Unit,
) {
    // No buttons until the well-known resolves; Content shows a spinner meanwhile.
    if (!state.wellknownLoaded && state.setupState == SetupState.Init) return
    if (state.isCustomEntry()) {
        CustomButtons(state = state, onCancel = onCancel)
    } else {
        AutoGenButtons(state = state, onFinish = onFinish)
    }
}

@Composable
private fun ColumnScope.CustomButtons(
    state: SecureBackupSetupState,
    onCancel: () -> Unit,
) {
    // No save/share controls in the custom flow — they'd leak the base58 key during Created.
    when (state.setupState) {
        SetupState.Creating,
        is SetupState.Created,
        is SetupState.CreatedAndSaved -> return
        SetupState.Init,
        is SetupState.Error -> Unit
    }
    when (state.customEntryStep) {
        CustomEntryStep.Entry -> {
            Button(
                text = stringResource(id = CommonStrings.action_continue),
                enabled = state.canContinueFromEntry,
                modifier = Modifier.fillMaxWidth(),
                onClick = { state.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase) },
            )
            TextButton(
                text = stringResource(id = CommonStrings.action_cancel),
                modifier = Modifier.fillMaxWidth(),
                onClick = onCancel,
            )
        }
        CustomEntryStep.Confirm -> {
            Button(
                text = stringResource(id = R.string.pro_screen_recovery_key_mode_confirm_finish_button),
                enabled = state.canSubmitCustomPassphrase,
                modifier = Modifier.fillMaxWidth(),
                onClick = { state.eventSink.invoke(SecureBackupSetupEvents.SubmitCustomPassphrase) },
            )
        }
    }
}

@Composable
private fun ColumnScope.AutoGenButtons(
    state: SecureBackupSetupState,
    onFinish: () -> Unit,
) {
    val context = LocalContext.current
    val chooserTitle = stringResource(id = R.string.screen_recovery_key_save_action)
    when (state.setupState) {
        SetupState.Init,
        SetupState.Creating,
        is SetupState.Error -> {
            Button(
                text = stringResource(id = CommonStrings.action_done),
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                onClick = onFinish
            )
        }
        is SetupState.Created,
        is SetupState.CreatedAndSaved -> {
            OutlinedButton(
                text = stringResource(id = R.string.screen_recovery_key_save_action),
                leadingIcon = IconSource.Vector(CompoundIcons.Download()),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    context.startSharePlainTextIntent(
                        activityResultLauncher = null,
                        chooserTitle = chooserTitle,
                        text = state.setupState.recoveryKey()!!,
                    )
                    state.eventSink.invoke(SecureBackupSetupEvents.RecoveryKeyHasBeenSaved)
                },
            )
            Button(
                text = stringResource(id = CommonStrings.action_done),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (state.setupState is SetupState.CreatedAndSaved) {
                        onFinish()
                    } else {
                        state.eventSink.invoke(SecureBackupSetupEvents.Done)
                    }
                },
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SecureBackupSetupViewPreview(
    @PreviewParameter(SecureBackupSetupStateProvider::class) state: SecureBackupSetupState
) = ElementPreview {
    SecureBackupSetupView(
        state = state,
        onSuccess = {},
        onBackClick = {},
    )
}
