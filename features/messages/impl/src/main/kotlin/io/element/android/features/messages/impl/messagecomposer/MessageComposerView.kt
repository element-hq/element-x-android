/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerEvent
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerState
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerStatePreviewParam
import io.element.android.features.messages.api.timeline.voicemessages.composer.aVoiceMessageComposerState
import io.element.android.libraries.androidutils.ui.hideKeyboard
import io.element.android.libraries.androidutils.ui.showKeyboard
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.textcomposer.TextComposer
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/**
 * Maximum interval to wait for the keyboard to reappear before the emoji picker is dismissed without it.
 */
private val KeyboardShowTimeout = 1.seconds

@Composable
internal fun MessageComposerView(
    state: MessageComposerState,
    voiceMessageState: VoiceMessageComposerState,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    fun sendMessage() {
        state.eventSink(MessageComposerEvent.SendMessage)
    }

    fun sendUri(uri: Uri) {
        state.eventSink(MessageComposerEvent.SendUri(uri))
    }

    fun onAddAttachment() {
        state.eventSink(MessageComposerEvent.AddAttachment)
    }

    fun onCloseSpecialMode() {
        state.eventSink(MessageComposerEvent.CloseSpecialMode)
    }

    fun onDismissTextFormatting() {
        view.clearFocus()
        state.eventSink(MessageComposerEvent.ToggleTextFormatting(enabled = false))
    }

    fun onSuggestionReceived(suggestion: Suggestion?) {
        state.eventSink(MessageComposerEvent.SuggestionReceived(suggestion))
    }

    fun onError(error: Throwable) {
        state.eventSink(MessageComposerEvent.Error(error))
    }

    fun onTyping(typing: Boolean) {
        state.eventSink(MessageComposerEvent.TypingNotice(typing))
    }

    val coroutineScope = rememberCoroutineScope()
    fun onRequestFocus() {
        coroutineScope.launch {
            state.textEditorState.requestFocus()
        }
    }

    val onVoiceRecorderEvent = { press: VoiceMessageRecorderEvent ->
        voiceMessageState.eventSink(VoiceMessageComposerEvent.RecorderEvent(press))
    }

    val onSendVoiceMessage = {
        voiceMessageState.eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
        state.eventSink(MessageComposerEvent.CloseSpecialMode)
    }

    val onToggleEmojiPicker = rememberEmojiPickerKeyboardToggle(state)

    val onDeleteVoiceMessage = {
        voiceMessageState.eventSink(VoiceMessageComposerEvent.DeleteVoiceMessage)
    }

    val onVoicePlayerEvent = { event: VoiceMessagePlayerEvent ->
        voiceMessageState.eventSink(VoiceMessageComposerEvent.PlayerEvent(event))
    }

    TextComposer(
        modifier = modifier,
        state = state.textEditorState,
        voiceMessageState = voiceMessageState.voiceMessageState,
        onRequestFocus = ::onRequestFocus,
        onSendMessage = ::sendMessage,
        composerMode = state.mode,
        isInThreadTimeline = state.isInThreadTimeline,
        showTextFormatting = state.showTextFormatting,
        onResetComposerMode = ::onCloseSpecialMode,
        onAddAttachment = ::onAddAttachment,
        onDismissTextFormatting = ::onDismissTextFormatting,
        onVoiceRecorderEvent = onVoiceRecorderEvent,
        onVoicePlayerEvent = onVoicePlayerEvent,
        onSendVoiceMessage = onSendVoiceMessage,
        onDeleteVoiceMessage = onDeleteVoiceMessage,
        onReceiveSuggestion = ::onSuggestionReceived,
        resolveMentionDisplay = state.resolveMentionDisplay,
        resolveAtRoomMentionDisplay = state.resolveAtRoomMentionDisplay,
        onError = ::onError,
        onTyping = ::onTyping,
        onSelectRichContent = ::sendUri,
        onAddEmoji = onToggleEmojiPicker,
    )

    AsyncActionView(
        async = state.slashCommandAction,
        onSuccess = {},
        onErrorDismiss = { state.eventSink(MessageComposerEvent.ClearSlashError) },
    )
}

/**
 * Coordinates the emoji picker with the soft keyboard and returns the emoji button's action, which switches between them.
 *
 * The picker substitutes for the keyboard, in the keyboard's position: the keyboard slides out in front of it on open and back
 * over it on close, and the picker is removed only once the keyboard has settled, so the composer never moves.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun rememberEmojiPickerKeyboardToggle(state: MessageComposerState): () -> Unit {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val isEmojiPickerVisible = state.emojiPickerState != ComposerEmojiPickerState.Hidden
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val imeTargetInsets = WindowInsets.imeAnimationTarget
    // The soft keyboard has fully slid in and is serving the composer. Derived so the IME animation does not recompose every frame.
    val isKeyboardServingComposer by remember(imeInsets, imeTargetInsets, density, state.textEditorState) {
        derivedStateOf {
            val imeBottom = imeInsets.getBottom(density)
            imeBottom > 0 && imeBottom == imeTargetInsets.getBottom(density) && state.textEditorState.hasFocus()
        }
    }
    // Set by the emoji button when it hands the composer back to the keyboard.
    var isEmojiPickerClosing by remember { mutableStateOf(false) }
    LaunchedEffect(isEmojiPickerVisible, isEmojiPickerClosing) {
        if (!isEmojiPickerVisible) {
            isEmojiPickerClosing = false
            return@LaunchedEffect
        }
        val keyboardServingComposer = snapshotFlow { isKeyboardServingComposer }
        if (isEmojiPickerClosing) {
            // Without a soft keyboard to wait for, e.g. when a hardware keyboard is attached, give up after a timeout.
            withTimeoutOrNull(KeyboardShowTimeout) { keyboardServingComposer.first { it } }
        } else {
            // The keyboard lingers for a moment after the picker opens: wait for it to leave, then for it to come back,
            // e.g. because the user tapped the composer.
            keyboardServingComposer.first { !it }
            keyboardServingComposer.first { it }
        }
        state.eventSink(MessageComposerEvent.DismissEmojiPicker)
    }
    return {
        if (isEmojiPickerVisible) {
            isEmojiPickerClosing = true
            coroutineScope.launch {
                state.textEditorState.requestFocus()
                // Before API 30 the keyboard is shown only for the focused view, i.e. the editor.
                (view.findFocus() ?: view).showKeyboard()
            }
        } else {
            state.eventSink(MessageComposerEvent.OpenEmojiPicker)
            view.hideKeyboard()
        }
    }
}

@PreviewsDayNight
@Composable
internal fun MessageComposerViewPreview(
    @PreviewParameter(MessageComposerStatePreviewParam::class) state: MessageComposerState,
) = ElementPreview {
    Column {
        MessageComposerView(
            modifier = Modifier.height(IntrinsicSize.Min),
            state = state,
            voiceMessageState = aVoiceMessageComposerState(),
        )
        MessageComposerView(
            modifier = Modifier.height(200.dp),
            state = state,
            voiceMessageState = aVoiceMessageComposerState(),
        )
        DisabledComposerView()
    }
}

@PreviewsDayNight
@Composable
internal fun MessageComposerViewVoicePreview(
    @PreviewParameter(VoiceMessageComposerStatePreviewParam::class) state: VoiceMessageComposerState,
) = ElementPreview {
    Column {
        MessageComposerView(
            modifier = Modifier.height(IntrinsicSize.Min),
            state = aMessageComposerState(),
            voiceMessageState = state,
        )
    }
}
