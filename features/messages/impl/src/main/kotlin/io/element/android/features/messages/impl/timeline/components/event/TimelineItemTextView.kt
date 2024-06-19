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

package io.element.android.features.messages.impl.timeline.components.event

import android.text.SpannableString
import android.text.Spanned
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.core.text.getSpans
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContentProvider
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.messages.LocalUserProfileCache
import io.element.android.libraries.matrix.ui.messages.UserProfileCache
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.mentions.getMentionSpans
import io.element.android.wysiwyg.compose.EditorStyledText
import io.element.android.wysiwyg.view.spans.CustomMentionSpan

@Composable
fun TimelineItemTextView(
    content: TimelineItemTextBasedContent,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit = {},
) {
    CompositionLocalProvider(
        LocalContentColor provides ElementTheme.colors.textPrimary,
        LocalTextStyle provides ElementTheme.typography.fontBodyLgRegular
    ) {
        val body = getTextWithResolvedMentions(content)
        Box(modifier.semantics { contentDescription = body.toString() }) {
            EditorStyledText(
                text = body,
                onLinkClickedListener = onLinkClick,
                style = ElementRichTextEditorStyle.textStyle(),
                onTextLayout = ContentAvoidingLayout.measureLegacyLastTextLine(onContentLayoutChange = onContentLayoutChange),
                releaseOnDetach = false,
            )
        }
    }
}

@Composable
private fun getTextWithResolvedMentions(content: TimelineItemTextBasedContent): CharSequence {
    val userProfileCache = LocalUserProfileCache.current
    val lastCacheUpdate by userProfileCache.lastCacheUpdate.collectAsState()
    val formattedBody = remember(content.htmlBody, lastCacheUpdate) {
        updateMentionSpans(content.formattedBody, userProfileCache)
        SpannableString(content.formattedBody ?: content.body)
    }

    return formattedBody
}

private fun updateMentionSpans(text: CharSequence?, cache: UserProfileCache): Boolean {
    var changedContents = false
    if (text != null) {
        for (mentionSpan in text.getMentionSpans()) {
            when (mentionSpan.type) {
                MentionSpan.Type.USER -> {
                    val displayName = cache.getDisplayName(UserId(mentionSpan.rawValue)) ?: mentionSpan.rawValue
                    if (mentionSpan.text != displayName) {
                        changedContents = true
                        mentionSpan.text = displayName
                    }
                }
                // Nothing yet for room mentions
                else -> Unit
            }
        }
    }
    return changedContents
}

@PreviewsDayNight
@Composable
internal fun TimelineItemTextViewPreview(
    @PreviewParameter(TimelineItemTextBasedContentProvider::class) content: TimelineItemTextBasedContent
) = ElementPreview {
    TimelineItemTextView(
        content = content,
        onLinkClick = {},
    )
}
