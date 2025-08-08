/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.tools

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class RecoveryKeyHiddenVisualTransformation : VisualTransformation {
    private var rc = RecoveryKeyVisualTransformation()
    private var pw = PasswordVisualTransformation()

    override fun filter(text: AnnotatedString): TransformedText =
        rc.filter(pw.filter(text).text)
}
