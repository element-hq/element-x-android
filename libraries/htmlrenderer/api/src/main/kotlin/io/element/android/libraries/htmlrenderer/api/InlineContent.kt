/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api

import androidx.compose.runtime.Immutable

/**
 * Content that occupies an inline-content placeholder inside a [ParagraphNode.text]
 * (see [androidx.compose.foundation.text.appendInlineContent]). The renderer looks each
 * placeholder id up in [ParagraphNode.inlineContent] and draws the matching content.
 *
 * The two kinds are [MentionNodeContent] (a mention pill) and [ImageNodeContent] (an inline image).
 */
@Immutable
sealed interface InlineContent

/**
 * An inline image (`<img>`).
 *
 * @param url the raw image source (an `mxc://` or `http(s)` URL). The renderer maps this to an image
 * loader model, so `mxc://` resolution happens outside this library.
 * @param alt alternative text, used as the content description and as a text fallback.
 * @param width the intrinsic width in pixels from the `<img width>` attribute, or null if absent.
 * @param height the intrinsic height in pixels from the `<img height>` attribute, or null if absent.
 * @param isEmoticon true for a custom emoji (`data-mx-emoticon`), which is always rendered emoji-sized.
 */
@Immutable
data class ImageNodeContent(
    val url: String,
    val alt: String?,
    val width: Int?,
    val height: Int?,
    val isEmoticon: Boolean,
) : InlineContent
