/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api.parcel

import android.os.Parcel
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TtsAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.isSpecified
import kotlinx.parcelize.Parceler

/**
 * A [Parceler] for the [AnnotatedString]s produced by [io.element.android.libraries.htmlrenderer.api.HtmlMessageParser].
 *
 * It keeps:
 * - the text,
 * - the [SpanStyle] ranges, field by field (see [writeSpanStyle] for the fields that are skipped),
 * - every string annotation (links, inline code and the inline content placeholders used for mentions),
 * - [LinkAnnotation.Url] annotations (without their styles or interaction listener).
 *
 * The relative order of all these annotations is preserved, since [AnnotatedString.equals] depends on it.
 *
 * Anything that can't be parcelled is skipped: paragraph styles, [LinkAnnotation.Clickable] annotations, TTS annotations, deprecated URL annotations, and some
 * [SpanStyle] fields. A [SpanStyle] with a skipped field still keeps its other fields.
 */
object AnnotatedStringParceler : Parceler<AnnotatedString> {
    override fun create(parcel: Parcel): AnnotatedString = buildAnnotatedString {
        append(parcel.readString().orEmpty())
        repeat(parcel.readInt()) {
            val type = parcel.readInt()
            val start = parcel.readInt()
            val end = parcel.readInt()
            when (type) {
                ANNOTATION_SPAN_STYLE -> addStyle(parcel.readSpanStyle(), start, end)
                ANNOTATION_STRING -> addStringAnnotation(
                    tag = parcel.readString().orEmpty(),
                    annotation = parcel.readString().orEmpty(),
                    start = start,
                    end = end,
                )
                ANNOTATION_LINK_URL -> addLink(LinkAnnotation.Url(parcel.readString().orEmpty()), start, end)
            }
        }
    }

    override fun AnnotatedString.write(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        val ranges = parcelableRanges()
        parcel.writeInt(ranges.size)
        ranges.forEach { range ->
            val item = range.item
            parcel.writeInt(
                when (item) {
                    is SpanStyle -> ANNOTATION_SPAN_STYLE
                    is LinkAnnotation.Url -> ANNOTATION_LINK_URL
                    else -> ANNOTATION_STRING
                }
            )
            parcel.writeInt(range.start)
            parcel.writeInt(range.end)
            when (item) {
                is SpanStyle -> parcel.writeSpanStyle(item)
                is LinkAnnotation.Url -> parcel.writeString(item.url)
                is String -> {
                    parcel.writeString(range.tag)
                    parcel.writeString(item)
                }
            }
        }
    }

    /**
     * Returns the parcelable annotations of this [AnnotatedString], in their original order. String annotations are returned as [String] items.
     */
    private fun AnnotatedString.parcelableRanges(): List<AnnotatedString.Range<out Any>> {
        // The string annotation values are wrapped in an internal type, so they are read from the public getter, which keeps the same order
        val stringAnnotations = getStringAnnotations(0, length).iterator()
        var nextStringAnnotation = stringAnnotations.nextOrNull()
        val ranges = mutableListOf<AnnotatedString.Range<out Any>>()
        // mapAnnotations is the only public API that iterates over all the annotations in their original order
        mapAnnotations { range ->
            when (range.item) {
                is SpanStyle, is LinkAnnotation.Url -> ranges.add(range)
                // Skipped, they either hold listeners or aren't produced by the parser
                is ParagraphStyle, is LinkAnnotation.Clickable, is TtsAnnotation -> Unit
                else -> {
                    val stringAnnotation = nextStringAnnotation
                    if (stringAnnotation != null && stringAnnotation.hasSameRangeAs(range)) {
                        ranges.add(stringAnnotation)
                        nextStringAnnotation = stringAnnotations.nextOrNull()
                    }
                    // Otherwise it's an unknown annotation type, skip it
                }
            }
            range
        }
        return ranges
    }
}

private fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null

private fun AnnotatedString.Range<*>.hasSameRangeAs(other: AnnotatedString.Range<*>) = start == other.start && end == other.end && tag == other.tag

private const val ANNOTATION_SPAN_STYLE = 0
private const val ANNOTATION_STRING = 1
private const val ANNOTATION_LINK_URL = 2

// Bit flags for the SpanStyle fields present in the parcel
private const val FIELD_COLOR = 1 shl 0
private const val FIELD_FONT_SIZE = 1 shl 1
private const val FIELD_FONT_WEIGHT = 1 shl 2
private const val FIELD_FONT_STYLE = 1 shl 3
private const val FIELD_FONT_FAMILY = 1 shl 4
private const val FIELD_FONT_FEATURE_SETTINGS = 1 shl 5
private const val FIELD_LETTER_SPACING = 1 shl 6
private const val FIELD_BASELINE_SHIFT = 1 shl 7
private const val FIELD_TEXT_GEOMETRIC_TRANSFORM = 1 shl 8
private const val FIELD_LOCALE_LIST = 1 shl 9
private const val FIELD_BACKGROUND = 1 shl 10
private const val FIELD_TEXT_DECORATION = 1 shl 11
private const val FIELD_SHADOW = 1 shl 12

private const val TEXT_DECORATION_UNDERLINE = 1 shl 0
private const val TEXT_DECORATION_LINE_THROUGH = 1 shl 1

// Only the generic font families can be parcelled, custom ones reference fonts that can't be
private val genericFontFamilies = listOf(
    FontFamily.Default,
    FontFamily.SansSerif,
    FontFamily.Serif,
    FontFamily.Monospace,
    FontFamily.Cursive,
)

/**
 * Writes the parcelable fields of [style], preceded by a bit mask of the fields that are present.
 * [SpanStyle.brush], [SpanStyle.drawStyle], [SpanStyle.platformStyle], [SpanStyle.fontSynthesis] and non generic [FontFamily]s are skipped.
 */
private fun Parcel.writeSpanStyle(style: SpanStyle) {
    val fontFamilyIndex = genericFontFamilies.indexOf(style.fontFamily)
    var fields = 0
    if (style.color.isSpecified) fields = fields or FIELD_COLOR
    if (style.fontSize.isSpecified) fields = fields or FIELD_FONT_SIZE
    if (style.fontWeight != null) fields = fields or FIELD_FONT_WEIGHT
    if (style.fontStyle != null) fields = fields or FIELD_FONT_STYLE
    if (fontFamilyIndex != -1) fields = fields or FIELD_FONT_FAMILY
    if (style.fontFeatureSettings != null) fields = fields or FIELD_FONT_FEATURE_SETTINGS
    if (style.letterSpacing.isSpecified) fields = fields or FIELD_LETTER_SPACING
    if (style.baselineShift != null) fields = fields or FIELD_BASELINE_SHIFT
    if (style.textGeometricTransform != null) fields = fields or FIELD_TEXT_GEOMETRIC_TRANSFORM
    if (style.localeList != null) fields = fields or FIELD_LOCALE_LIST
    if (style.background.isSpecified) fields = fields or FIELD_BACKGROUND
    if (style.textDecoration != null) fields = fields or FIELD_TEXT_DECORATION
    if (style.shadow != null) fields = fields or FIELD_SHADOW
    writeInt(fields)

    // Each field is written only if it's present, in the same order as the flags above
    if (style.color.isSpecified) writeColor(style.color)
    if (style.fontSize.isSpecified) writeTextUnit(style.fontSize)
    style.fontWeight?.let { writeInt(it.weight) }
    style.fontStyle?.let { writeInt(it.value) }
    if (fontFamilyIndex != -1) writeInt(fontFamilyIndex)
    style.fontFeatureSettings?.let { writeString(it) }
    if (style.letterSpacing.isSpecified) writeTextUnit(style.letterSpacing)
    style.baselineShift?.let { writeFloat(it.multiplier) }
    style.textGeometricTransform?.let {
        writeFloat(it.scaleX)
        writeFloat(it.skewX)
    }
    style.localeList?.let { localeList -> writeString(localeList.joinToString(",") { it.toLanguageTag() }) }
    if (style.background.isSpecified) writeColor(style.background)
    style.textDecoration?.let {
        var decorations = 0
        if (it.contains(TextDecoration.Underline)) decorations = decorations or TEXT_DECORATION_UNDERLINE
        if (it.contains(TextDecoration.LineThrough)) decorations = decorations or TEXT_DECORATION_LINE_THROUGH
        writeInt(decorations)
    }
    style.shadow?.let {
        writeColor(it.color)
        writeFloat(it.offset.x)
        writeFloat(it.offset.y)
        writeFloat(it.blurRadius)
    }
}

private fun Parcel.readSpanStyle(): SpanStyle {
    val fields = readInt()
    return SpanStyle(
        color = if (fields has FIELD_COLOR) readColor() else Color.Unspecified,
        fontSize = if (fields has FIELD_FONT_SIZE) readTextUnit() else TextUnit.Unspecified,
        fontWeight = if (fields has FIELD_FONT_WEIGHT) FontWeight(readInt()) else null,
        fontStyle = if (fields has FIELD_FONT_STYLE) FontStyle(readInt()) else null,
        fontFamily = if (fields has FIELD_FONT_FAMILY) genericFontFamilies.getOrNull(readInt()) else null,
        fontFeatureSettings = if (fields has FIELD_FONT_FEATURE_SETTINGS) readString() else null,
        letterSpacing = if (fields has FIELD_LETTER_SPACING) readTextUnit() else TextUnit.Unspecified,
        baselineShift = if (fields has FIELD_BASELINE_SHIFT) BaselineShift(readFloat()) else null,
        textGeometricTransform = if (fields has FIELD_TEXT_GEOMETRIC_TRANSFORM) {
            TextGeometricTransform(scaleX = readFloat(), skewX = readFloat())
        } else {
            null
        },
        localeList = if (fields has FIELD_LOCALE_LIST) LocaleList(readString().orEmpty()) else null,
        background = if (fields has FIELD_BACKGROUND) readColor() else Color.Unspecified,
        textDecoration = if (fields has FIELD_TEXT_DECORATION) {
            val decorations = readInt()
            TextDecoration.combine(
                buildList {
                    if (decorations has TEXT_DECORATION_UNDERLINE) add(TextDecoration.Underline)
                    if (decorations has TEXT_DECORATION_LINE_THROUGH) add(TextDecoration.LineThrough)
                }
            )
        } else {
            null
        },
        shadow = if (fields has FIELD_SHADOW) {
            Shadow(color = readColor(), offset = Offset(readFloat(), readFloat()), blurRadius = readFloat())
        } else {
            null
        },
    )
}

private fun Parcel.writeColor(color: Color) = writeLong(color.value.toLong())

private fun Parcel.readColor(): Color = Color(readLong().toULong())

private fun Parcel.writeTextUnit(textUnit: TextUnit) {
    writeInt(if (textUnit.type == TextUnitType.Em) TEXT_UNIT_EM else TEXT_UNIT_SP)
    writeFloat(textUnit.value)
}

private fun Parcel.readTextUnit(): TextUnit {
    val type = if (readInt() == TEXT_UNIT_EM) TextUnitType.Em else TextUnitType.Sp
    return TextUnit(readFloat(), type)
}

private const val TEXT_UNIT_SP = 0
private const val TEXT_UNIT_EM = 1

private infix fun Int.has(flag: Int) = this and flag != 0
