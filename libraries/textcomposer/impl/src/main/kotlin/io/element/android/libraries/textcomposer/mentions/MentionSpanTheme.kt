/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.mentions

import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.Spanned
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.buildSpannedString
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.rememberTypeface
import io.element.android.libraries.designsystem.theme.currentUserMentionPillBackground
import io.element.android.libraries.designsystem.theme.currentUserMentionPillText
import io.element.android.libraries.designsystem.theme.mentionPillBackground
import io.element.android.libraries.designsystem.theme.mentionPillText
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.ui.messages.RoomInfoCache
import io.element.android.libraries.matrix.ui.messages.RoomMemberProfilesCache
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Theme used for mention spans.
 * To make this work, you need to:
 * 1. Provide [LocalMentionSpanTheme] in a composable that wraps the ones where you want to use mentions.
 * 2. Call [MentionSpanTheme.updateStyles] with the current [UserId] so the colors and sizes are computed.
 * 3. Use either [MentionSpanTheme.updateMentionStyles] or [MentionSpan.update] to update the styles of the mention spans.
 */
@Stable
class MentionSpanTheme @Inject constructor() {
    internal var currentUserId: UserId? = null
    internal var currentUserTextColor: Int = 0
    internal var currentUserBackgroundColor: Int = Color.WHITE
    internal var otherTextColor: Int = 0
    internal var otherBackgroundColor: Int = Color.WHITE

    private val paddingValues = PaddingValues(start = 4.dp, end = 6.dp)
    internal val paddingValuesPx = mutableStateOf(0 to 0)
    internal val typeface = mutableStateOf(Typeface.DEFAULT)

    /**
     * Updates the styles of the mention spans based on the [ElementTheme] and [currentUserId].
     */
    @Suppress("ComposableNaming")
    @Composable
    fun updateStyles(currentUserId: UserId) {
        this.currentUserId = currentUserId
        currentUserTextColor = ElementTheme.colors.currentUserMentionPillText.toArgb()
        currentUserBackgroundColor = ElementTheme.colors.currentUserMentionPillBackground.toArgb()
        otherTextColor = ElementTheme.colors.mentionPillText.toArgb()
        otherBackgroundColor = ElementTheme.colors.mentionPillBackground.toArgb()

        typeface.value = ElementTheme.typography.fontBodyLgMedium.rememberTypeface().value
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        paddingValuesPx.value = remember(paddingValues, density, layoutDirection) {
            with(density) {
                val leftPadding = paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
                val rightPadding = paddingValues.calculateRightPadding(layoutDirection).roundToPx()
                leftPadding to rightPadding
            }
        }
    }
}

/**
 * Updates the styles of the mention spans in the given [CharSequence].
 */
fun MentionSpanTheme.updateMentionStyles(charSequence: CharSequence) {
    val spanned = charSequence as? Spanned ?: return
    val mentionSpans = spanned.getMentionSpans()
    for (span in mentionSpans) {
        span.update(this)
    }
}

/**
 * Composition local containing the current [MentionSpanTheme].
 */
val LocalMentionSpanTheme = staticCompositionLocalOf {
    MentionSpanTheme()
}

 @PreviewsDayNight
 @Composable
 internal fun MentionSpanThemePreview() {
    ElementPreview {
        val mentionSpanTheme = remember { MentionSpanTheme() }
        val provider = remember {
            MentionSpanProvider(
                mentionSpanFormatter = object : MentionSpanFormatter {
                    override fun formatDisplayText(mentionType: MentionType): CharSequence {
                        return when (mentionType) {
                            is MentionType.User -> mentionType.userId.value
                            is MentionType.Room -> mentionType.roomIdOrAlias.identifier
                            is MentionType.Message -> "\uD83D\uDCAC️ > ${mentionType.roomIdOrAlias.identifier}"
                            is MentionType.Everyone -> "@room"
                        }
                    }
                },
                permalinkParser = object : PermalinkParser {
                    override fun parse(uriString: String): PermalinkData {
                        return when (uriString) {
                            "https://matrix.to/#/@me:matrix.org" -> PermalinkData.UserLink(UserId("@me:matrix.org"))
                            "https://matrix.to/#/@other:matrix.org" -> PermalinkData.UserLink(UserId("@other:matrix.org"))
                            "https://matrix.to/#/#room:matrix.org" -> PermalinkData.RoomLink(
                                roomIdOrAlias = RoomAlias("#room:matrix.org").toRoomIdOrAlias(),
                                eventId = null,
                                viaParameters = persistentListOf(),
                            )
                            "@room" -> PermalinkData.FallbackLink(Uri.EMPTY, false)
                            else -> throw AssertionError("Unexpected value $uriString")
                        }
                    }
                },
            )
        }

        val textColor = ElementTheme.colors.textPrimary.toArgb()
        fun mentionSpanMe() = provider.getMentionSpanFor("mention", "https://matrix.to/#/@me:matrix.org")
        fun mentionSpanOther() = provider.getMentionSpanFor("mention", "https://matrix.to/#/@other:matrix.org")
        fun mentionSpanRoom() = provider.getMentionSpanFor("room:matrix.org", "https://matrix.to/#/#room:matrix.org")
        fun mentionSpanEveryone() = provider.getMentionSpanFor("@room", "@room")
        mentionSpanTheme.updateStyles(currentUserId = UserId("@me:matrix.org"))

        CompositionLocalProvider(
            LocalMentionSpanTheme provides mentionSpanTheme
        ) {
            AndroidView(factory = { context ->
                TextView(context).apply {
                    includeFontPadding = false
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    text = buildSpannedString {
                        append("This is a ")
                        append("@mention", mentionSpanMe(), 0)
                        append(" to the current user and this is a ")
                        append("@mention", mentionSpanOther(), 0)
                        append(" to other user. This is for everyone in the ")
                        append("@room", mentionSpanEveryone(), 0)
                        append(". This one is for a link to another room: ")
                        append("#room:matrix.org", mentionSpanRoom(), 0)
                        append("\n\n")
                        append("This ")
                        append("mention", mentionSpanMe(), 0)
                        append(" didn't have an '@' and it was automatically added, same as this ")
                        append("room:matrix.org", mentionSpanRoom(), 0)
                        append(" one, which had no leading '#'.")
                    }
                    mentionSpanTheme.updateMentionStyles(text)
                    setTextColor(textColor)
                }
            })
        }
    }
 }
