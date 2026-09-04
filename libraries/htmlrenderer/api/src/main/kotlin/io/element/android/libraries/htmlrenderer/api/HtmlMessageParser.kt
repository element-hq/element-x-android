/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api

import org.jsoup.nodes.Document

/**
 * Parses the HTML [Document] of a formatted message body into a tree of [MessageNode]s
 * that a Compose renderer can turn into composables.
 *
 * The [Document] is expected to have already been sanitized (e.g. via
 * `FormattedBody.toHtmlDocument`) so that only supported tags remain.
 */
interface HtmlMessageParser {
    /**
     * Converts [document] into a [DocumentNode]. Never throws: unknown or empty content
     * yields a [DocumentNode] with no children.
     */
    fun parse(document: Document): DocumentNode

    companion object {
        /**
         * Tag used for the string annotation attached to link (`<a href>`) text ranges in a
         * [ParagraphNode.text]. The annotation value is the link target URL.
         */
        const val LINK_ANNOTATION_TAG = "url"
    }
}
