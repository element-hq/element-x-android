/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.net.toUri

open class EditableAvatarViewUriProvider : PreviewParameterProvider<Uri?> {
    override val values: Sequence<Uri?>
        get() = sequenceOf(
            null,
            "mxc://matrix.org/123456".toUri(),
            "https://example.com/avatar.jpg".toUri(),
        )
}
