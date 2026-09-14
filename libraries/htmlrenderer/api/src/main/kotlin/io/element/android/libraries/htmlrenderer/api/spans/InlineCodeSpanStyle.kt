/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api.spans

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import io.element.android.compound.theme.ElementTheme

/*
 * Copyright 2026 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

/**
 * The style for inline code spans.
 */
val InlineCodeSpanStyle: SpanStyle = SpanStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = ElementTheme.typography.fontBodySmRegular.fontSize,
)
