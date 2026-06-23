/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.File

class SvgDimensionExtractor(
    private val defaultWidth: Long = 640L,
    private val defaultHeight: Long = 480L,
) {
    fun extractDimensions(file: File): Pair<Long, Long> {
        return file.inputStream().use { inputStream ->
            try {
                val parser = Xml.newPullParser()
                parser.setInput(inputStream, null)
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name.equals("svg", ignoreCase = true)) {
                        val width = parser.getAttributeValue(null, "width")
                        val height = parser.getAttributeValue(null, "height")
                        val viewBox = parser.getAttributeValue(null, "viewBox")

                        val parsedWidth = width?.let { parseLength(it) }
                        val parsedHeight = height?.let { parseLength(it) }

                        if (parsedWidth != null && parsedHeight != null) {
                            return parsedWidth to parsedHeight
                        }

                        if (viewBox != null) {
                            val parts = viewBox.trim().split("\\s+".toRegex()).map { it.toFloatOrNull() }
                            if (parts.size == 4 && parts[2] != null && parts[3] != null) {
                                val vbWidth = parts[2]!!.toLong().coerceAtLeast(1)
                                val vbHeight = parts[3]!!.toLong().coerceAtLeast(1)
                                return vbWidth to vbHeight
                            }
                        }

                        return (parsedWidth ?: defaultWidth) to (parsedHeight ?: defaultHeight)
                    }
                    eventType = parser.next()
                }
                defaultWidth to defaultHeight
            } catch (_: Exception) {
                defaultWidth to defaultHeight
            }
        }
    }

    private fun parseLength(value: String): Long? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        return try {
            val numericPart = trimmed.replace(Regex("[^\\d.]"), "")
            if (numericPart.isEmpty()) null else numericPart.toFloat().toLong().coerceAtLeast(1)
        } catch (_: NumberFormatException) {
            null
        }
    }
}
