/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
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
import androidx.core.text.getSpans
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.textcomposer.components.markdown.StableCharSequence
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
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
        permalinkBuilder: PermalinkBuilder,
    ) {
        val suggestion = currentSuggestion ?: return
        when (resolvedSuggestion) {
            is ResolvedSuggestion.AtRoom -> {
                val currentText = SpannableStringBuilder(text.value())
                val replaceText = "@room"
                val roomPill = mentionSpanProvider.getMentionSpanFor(replaceText, "")
                currentText.replace(suggestion.start, suggestion.end, "@ ")
                val end = suggestion.start + 1
                currentText.setSpan(roomPill, suggestion.start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.update(currentText, true)
                selection = IntRange(end + 1, end + 1)
            }
            is ResolvedSuggestion.Member -> {
                val currentText = SpannableStringBuilder(text.value())
                val text = resolvedSuggestion.roomMember.displayName?.prependIndent("@")
                    ?: resolvedSuggestion.roomMember.userId.value // TCHAP TODO check needed about mxid displaying
                val link = permalinkBuilder.permalinkForUser(resolvedSuggestion.roomMember.userId).getOrNull() ?: return
                val mentionPill = mentionSpanProvider.getMentionSpanFor(text, link)
                currentText.replace(suggestion.start, suggestion.end, "@ ")
                val end = suggestion.start + 1
                currentText.setSpan(mentionPill, suggestion.start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                this.text.update(currentText, true)
                this.selection = IntRange(end + 1, end + 1)
            }
            is ResolvedSuggestion.Alias -> {
                val currentText = SpannableStringBuilder(text.value())
                val text = resolvedSuggestion.roomAlias.value
                val link = permalinkBuilder.permalinkForRoomAlias(resolvedSuggestion.roomAlias).getOrNull() ?: return
                val mentionPill = mentionSpanProvider.getMentionSpanFor(text, link)
                currentText.replace(suggestion.start, suggestion.end, "# ")
                val end = suggestion.start + 1
                currentText.setSpan(mentionPill, suggestion.start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
                            MentionSpan.Type.USER -> {
                                permalinkBuilder.permalinkForUser(UserId(mention.rawValue)).getOrNull()?.let { link ->
                                    replace(start, end, "[${mention.rawValue}]($link)")
                                }
                            }
                            MentionSpan.Type.EVERYONE -> {
                                replace(start, end, "@room")
                            }
                            MentionSpan.Type.ROOM -> {
                                permalinkBuilder.permalinkForRoomAlias(RoomAlias(mention.rawValue)).getOrNull()?.let { link ->
                                    replace(start, end, "[${mention.text}]($link)")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            charSequence.toString()
        }
    }

    fun getMentions(): List<IntentionalMention> {
        val text = SpannableString(text.value())
        val mentionSpans = text.getSpans<MentionSpan>(0, text.length)
        return mentionSpans.mapNotNull { mentionSpan ->
            when (mentionSpan.type) {
                MentionSpan.Type.USER -> IntentionalMention.User(UserId(mentionSpan.rawValue))
                MentionSpan.Type.EVERYONE -> IntentionalMention.Room
                MentionSpan.Type.ROOM -> null
            }
        }
    }

    @Parcelize
    data class SavedState(
        val text: CharSequence,
        val selectionStart: Int,
        val selectionEnd: Int,
    ) : Parcelable
}

object MarkdownTextEditorStateSaver : Saver<MarkdownTextEditorState, MarkdownTextEditorState.SavedState> {
    override fun restore(value: MarkdownTextEditorState.SavedState): MarkdownTextEditorState {
        return MarkdownTextEditorState(
            initialText = "",
            initialFocus = false,
        ).apply {
            text.update(value.text, true)
            selection = value.selectionStart..value.selectionEnd
        }
    }

    override fun SaverScope.save(value: MarkdownTextEditorState): MarkdownTextEditorState.SavedState {
        return MarkdownTextEditorState.SavedState(
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
