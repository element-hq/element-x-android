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

package io.element.android.features.messages.impl.voicemessages.timeline

import android.text.Spanned
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.text.getSpans
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.multibindings.IntoMap
import io.element.android.features.messages.impl.timeline.di.TimelineItemEventContentKey
import io.element.android.features.messages.impl.timeline.di.TimelineItemPresenterFactory
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.utils.UserProfileCache
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.wysiwyg.view.spans.CustomMentionSpan

@Module
@ContributesTo(RoomScope::class)
interface TextMessagePresenterModule {
    @Binds
    @IntoMap
    @TimelineItemEventContentKey(TimelineItemTextBasedContent::class)
    fun bindTextMessagePresenterFactory(factory: TextMessagePresenter.Factory): TimelineItemPresenterFactory<*, *>
}

class TextMessagePresenter @AssistedInject constructor(
    private val profileCache: UserProfileCache,
    @Assisted private val content: TimelineItemTextBasedContent,
) : Presenter<TextMessageState> {
    @AssistedFactory
    fun interface Factory : TimelineItemPresenterFactory<TimelineItemTextBasedContent, TextMessageState> {
        override fun create(content: TimelineItemTextBasedContent): TextMessagePresenter
    }

    @Composable
    override fun present(): TextMessageState {
        val formattedBody by remember {
            updateMentionSpans(content.formattedBody)
            mutableStateOf(content.formattedBody)
        }
        var needsRefresh by remember { mutableIntStateOf(0) }

        LaunchedEffect(profileCache.lastCacheUpdate, content.htmlBody) {
            if (updateMentionSpans(formattedBody)) {
                // If the mention spans have been updated, we need to force a refresh of the content
                needsRefresh++
            }
        }

        return TextMessageState(
            text = content.body,
            formattedText = key(needsRefresh) { formattedBody },
        )
    }

    private fun updateMentionSpans(text: CharSequence?): Boolean {
        return if (text != null && text is Spanned) {
            val mentionSpans = text.getSpans<CustomMentionSpan>()
            if (mentionSpans.isNotEmpty()) {
                var changedContents = false
                for (mentionSpan in mentionSpans) {
                    val providedSpan = mentionSpan.providedSpan as? MentionSpan ?: continue
                    val displayName = profileCache.getDisplayName(UserId(providedSpan.rawValue)) ?: providedSpan.rawValue
                    if (providedSpan.text != displayName) {
                        changedContents = true
                        providedSpan.text = displayName
                    }
                }
                changedContents
            } else {
                false
            }
        } else {
            false
        }
    }
}

data class TextMessageState(
    val text: CharSequence,
    val formattedText: CharSequence?,
)
