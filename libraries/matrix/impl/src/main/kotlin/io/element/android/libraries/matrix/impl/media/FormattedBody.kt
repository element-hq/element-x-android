/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.media

import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import org.matrix.rustcomponents.sdk.FormattedBody as RustFormattedBody
import org.matrix.rustcomponents.sdk.MessageFormat as RustMessageFormat

fun FormattedBody.map(): RustFormattedBody = RustFormattedBody(
    format = format.map(),
    body = body,
)

private fun io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat.map(): RustMessageFormat {
    return when (this) {
        io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat.HTML -> RustMessageFormat.Html
        io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat.UNKNOWN -> RustMessageFormat.Unknown("")
    }
}
