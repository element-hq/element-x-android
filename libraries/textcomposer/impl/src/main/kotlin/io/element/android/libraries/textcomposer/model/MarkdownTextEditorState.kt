/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.textcomposer.components.markdown.StableCharSequence
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.libraries.textcomposer.mentions.MentionType
import io.element.android.libraries.textcomposer.mentions.ResolvedSuggestion
import io.element.android.libraries.textcomposer.mentions.getMentionSpans
import kotlinx.parcelize.Parcelize

@Stable
class MarkdownTextEditorState(
    initialText: String?,
    initialFocus: Boolean,
) {
    var text by mutableStateOf(StableCharSequence(initialText ?: ""))
    var selection by mutableStateOf(0..0)
    var hasFocus by mutableStateOf(initialFocus)
    var requestFocusAction by mutableStateOf({})
    var lineCount by mutableIntStateOf(1)
    var currentSuggestion by mutableStateOf<Suggestion?>(null)

    fun insertSuggestion(
        resolvedSuggestion: ResolvedSuggestion,
        mentionSpanProvider: MentionSpanProvider,
    ) {
        val suggestion = currentSuggestion ?: return
        when (resolvedSuggestion) {
            is ResolvedSuggestion.AtRoom -> {
                val currentText = SpannableStringBuilder(text.value())
                val mentionSpan = mentionSpanProvider.createEveryoneMentionSpan()
                currentText.replace(suggestion.start, suggestion.end, "@ ")
                val end = suggestion.start + 1
                currentText.setSpan(mentionSpan, suggestion.start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.update(currentText, true)
                selection = IntRange(end + 1, end + 1)
            }
            is ResolvedSuggestion.Member -> {
                val currentText = SpannableStringBuilder(text.value())
                val mentionSpan = mentionSpanProvider.createUserMentionSpan(resolvedSuggestion.roomMember.userId)
                currentText.replace(suggestion.start, suggestion.end, "@ ")
                val end = suggestion.start + 1
                currentText.setSpan(mentionSpan, suggestion.start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                this.text.update(currentText, true)
                this.selection = IntRange(end + 1, end + 1)
            }
            is ResolvedSuggestion.Alias -> {
                val currentText = SpannableStringBuilder(text.value())
                val mentionSpan = mentionSpanProvider.createRoomMentionSpan(resolvedSuggestion.roomAlias.toRoomIdOrAlias())
                currentText.replace(suggestion.start, suggestion.end, "# ")
                val end = suggestion.start + 1
                currentText.setSpan(mentionSpan, suggestion.start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                this.text.update(currentText, true)
                this.selection = IntRange(end + 1, end + 1)
            }
        }
    }

    fun getMessageMarkdown(permalinkBuilder: PermalinkBuilder): String {
        val charSequence = text.value()
        return if (charSequence is Spanned) {
            val mentions = charSequence.getMentionSpans()
            buildString {
                append(charSequence.toString())
                if (mentions.isNotEmpty()) {
                    for (mention in mentions.sortedByDescending { charSequence.getSpanEnd(it) }) {
                        val start = charSequence.getSpanStart(mention)
                        val end = charSequence.getSpanEnd(mention)
                        when (mention.type) {
                            is MentionType.User -> {
                                permalinkBuilder.permalinkForUser(mention.type.userId).getOrNull()?.let { link ->
                                    replace(start, end, "[${mention.type.userId}]($link)")
                                }
                            }
                            is MentionType.Everyone -> {
                                replace(start, end, "@room")
                            }
                            is MentionType.Room -> {
                                val roomIdOrAlias = mention.type.roomIdOrAlias
                                if (roomIdOrAlias is RoomIdOrAlias.Alias) {
                                    permalinkBuilder.permalinkForRoomAlias(roomIdOrAlias.roomAlias).getOrNull()?.let { link ->
                                        replace(start, end, "[${roomIdOrAlias.roomAlias}]($link)")
                                    }
                                }
                            }
                            else -> Unit
                        }
                    }
                }
            }
        } else {
            charSequence.toString()
        }
    }

    fun getMentions(): List<IntentionalMention> {
        val mentionSpans = text.value().getMentionSpans()
        return mentionSpans.mapNotNull { mentionSpan ->
            when (mentionSpan.type) {
                is MentionType.User -> IntentionalMention.User(mentionSpan.type.userId)
                is MentionType.Everyone -> IntentionalMention.Room
                else -> null
            }
        }
    }

    @Parcelize
    data class SavedValue(
        val text: CharSequence,
        val selectionStart: Int,
        val selectionEnd: Int,
    ) : Parcelable
}

object MarkdownTextEditorStateSaver : Saver<MarkdownTextEditorState, MarkdownTextEditorState.SavedValue> {
    override fun restore(value: MarkdownTextEditorState.SavedValue): MarkdownTextEditorState {
        return MarkdownTextEditorState(
            initialText = "",
            initialFocus = false,
        ).apply {
            text.update(value.text, true)
            selection = value.selectionStart..value.selectionEnd
        }
    }

    override fun SaverScope.save(value: MarkdownTextEditorState): MarkdownTextEditorState.SavedValue {
        return MarkdownTextEditorState.SavedValue(
            text = value.text.value(),
            selectionStart = value.selection.first,
            selectionEnd = value.selection.last,
        )
    }
}

@Composable
fun rememberMarkdownTextEditorState(
    initialText: String? = null,
    initialFocus: Boolean = false,
): MarkdownTextEditorState {
    return rememberSaveable(saver = MarkdownTextEditorStateSaver) { MarkdownTextEditorState(initialText, initialFocus) }
}
