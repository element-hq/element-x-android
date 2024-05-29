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
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.room.Mention
import io.element.android.libraries.textcomposer.components.markdown.StableCharSequence
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.libraries.textcomposer.mentions.ResolvedMentionSuggestion
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
    var currentMentionSuggestion by mutableStateOf<Suggestion?>(null)

    fun insertMention(
        mention: ResolvedMentionSuggestion,
        mentionSpanProvider: MentionSpanProvider,
        permalinkBuilder: PermalinkBuilder,
    ) {
        val suggestion = currentMentionSuggestion ?: return
        when (mention) {
            is ResolvedMentionSuggestion.AtRoom -> {
                val currentText = SpannableStringBuilder(text.value())
                val replaceText = "@room"
                val roomPill = mentionSpanProvider.getMentionSpanFor(replaceText, "")
                currentText.replace(suggestion.start, suggestion.end, ". ")
                val end = suggestion.start + 1
                currentText.setSpan(roomPill, suggestion.start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.update(currentText, true)
                selection = IntRange(end + 1, end + 1)
            }
            is ResolvedMentionSuggestion.Member -> {
                val currentText = SpannableStringBuilder(text.value())
                val text = mention.roomMember.displayName?.prependIndent("@") ?: mention.roomMember.userId.value
                val link = permalinkBuilder.permalinkForUser(mention.roomMember.userId).getOrNull() ?: return
                val mentionPill = mentionSpanProvider.getMentionSpanFor(text, link)
                currentText.replace(suggestion.start, suggestion.end, ". ")
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
            val mentions = charSequence.getSpans(0, charSequence.length, MentionSpan::class.java)
            buildString {
                append(charSequence.toString())
                if (mentions != null && mentions.isNotEmpty()) {
                    for (mention in mentions.reversed()) {
                        val start = charSequence.getSpanStart(mention)
                        val end = charSequence.getSpanEnd(mention)
                        if (mention.type == MentionSpan.Type.USER) {
                            if (mention.rawValue == "@room") {
                                replace(start, end, "@room")
                            } else {
                                val link = permalinkBuilder.permalinkForUser(UserId(mention.rawValue)).getOrNull() ?: continue
                                replace(start, end, "[${mention.text}]($link)")
                            }
                        }
                    }
                }
            }
        } else {
            charSequence.toString()
        }
    }

    fun getMentions(): List<Mention> {
        val text = SpannableString(text.value())
        val mentionSpans = text.getSpans<MentionSpan>(0, text.length)
        return mentionSpans.mapNotNull { mentionSpan ->
            when (mentionSpan.type) {
                MentionSpan.Type.USER -> {
                    if (mentionSpan.rawValue == "@room") {
                        Mention.AtRoom
                    } else {
                        Mention.User(UserId(mentionSpan.rawValue))
                    }
                }
                else -> null
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
