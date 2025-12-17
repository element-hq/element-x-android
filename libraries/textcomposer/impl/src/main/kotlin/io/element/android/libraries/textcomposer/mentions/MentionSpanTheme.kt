/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.mentions

import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.Spanned
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.buildSpannedString
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.rememberTypeface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.messageFromMeBackground
import io.element.android.libraries.designsystem.theme.messageFromOtherBackground
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import kotlinx.collections.immutable.persistentListOf

/**
 * Theme used for mention spans.
 * To make this work, you need to:
 * 1. Call [MentionSpanTheme.updateStyles] so the colors and sizes are computed.
 * 2. Use either [MentionSpanTheme.updateMentionStyles] or [MentionSpan.updateTheme] to update the styles of the mention spans.
 */
@Stable
@SingleIn(SessionScope::class)
class MentionSpanTheme(val currentUserId: UserId) {
    @Inject
    constructor(matrixClient: MatrixClient) : this(matrixClient.sessionId)

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
    fun updateStyles() {
        currentUserTextColor = ElementTheme.colors.textBadgeAccent.toArgb()
        currentUserBackgroundColor = ElementTheme.colors.bgBadgeAccent.toArgb()
        otherTextColor = ElementTheme.colors.textPrimary.toArgb()
        otherBackgroundColor = ElementTheme.colors.bgBadgeDefault.toArgb()

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
        span.updateTheme(this)
    }
}

@PreviewsDayNight
@Composable
internal fun MentionSpanThemePreview() {
    ElementPreview {
        val mentionSpanTheme = remember { MentionSpanTheme(UserId("@me:matrix.org")) }
        val provider = remember {
            MentionSpanProvider(
                mentionSpanTheme = mentionSpanTheme,
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
        fun mentionSpanEveryone() = provider.createEveryoneMentionSpan()
        mentionSpanTheme.updateStyles()

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
                setTextColor(textColor)
            }
        })
    }
}

@Composable
private fun MentionSpanThemeInTimelineContent(
    bgColor: Int,
    modifier: Modifier = Modifier,
) {
    val mentionSpanTheme = remember { MentionSpanTheme(UserId("@me:matrix.org")) }
    val provider = remember {
        MentionSpanProvider(
            mentionSpanTheme = mentionSpanTheme,
            mentionSpanFormatter = object : MentionSpanFormatter {
                override fun formatDisplayText(mentionType: MentionType): CharSequence {
                    return when (mentionType) {
                        is MentionType.User -> mentionType.userId.value
                        else -> throw AssertionError("Unexpected value $mentionType")
                    }
                }
            },
            permalinkParser = object : PermalinkParser {
                override fun parse(uriString: String): PermalinkData {
                    return when (uriString) {
                        "https://matrix.to/#/@me:matrix.org" -> PermalinkData.UserLink(UserId("@me:matrix.org"))
                        "https://matrix.to/#/@other:matrix.org" -> PermalinkData.UserLink(UserId("@other:matrix.org"))
                        else -> throw AssertionError("Unexpected value $uriString")
                    }
                }
            },
        )
    }

    val textColor = ElementTheme.colors.textPrimary.toArgb()
    fun mentionSpanMe() = provider.getMentionSpanFor("mention", "https://matrix.to/#/@me:matrix.org")
    fun mentionSpanOther() = provider.getMentionSpanFor("mention", "https://matrix.to/#/@other:matrix.org")
    mentionSpanTheme.updateStyles()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                includeFontPadding = false
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                text = buildSpannedString {
                    append("Hello ")
                    append("@mention", mentionSpanMe(), 0)
                    append(" ")
                    append("@mention", mentionSpanOther(), 0)
                }
                setTextColor(textColor)
                setBackgroundColor(bgColor)
            }
        }
    )
}

@PreviewsDayNight
@Composable
internal fun MentionSpanThemeInTimelinePreview() = ElementPreview {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Message from me
        Text(
            text = "Message from me",
            style = ElementTheme.typography.fontBodySmMedium,
        )
        ElementTheme.colors.messageFromMeBackground.let { color ->
            MentionSpanThemeInTimelineContent(
                modifier = Modifier
                    .padding(start = 60.dp, end = 8.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(8.dp),
                bgColor = color.toArgb()
            )
        }
        // Message from other
        ElementTheme.colors.messageFromOtherBackground.let { color ->
            Text(
                text = "Message from other",
                style = ElementTheme.typography.fontBodySmMedium,
            )
            MentionSpanThemeInTimelineContent(
                modifier = Modifier
                    .padding(start = 8.dp, end = 60.dp)
                    .padding(4.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp),
                bgColor = color.toArgb()
            )
        }
        // Composer
        ElementTheme.colors.bgSubtleSecondary.let { color ->
            Text(
                text = "Composer",
                style = ElementTheme.typography.fontBodySmMedium,
            )
            MentionSpanThemeInTimelineContent(
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .background(color)
                    .padding(8.dp),
                bgColor = color.toArgb()
            )
        }
    }
}
