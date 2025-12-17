/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.utils.formatter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.element.android.libraries.androidutils.filesize.AndroidFileSizeFormatter
import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.ui.utils.version.LocalSdkIntVersionProvider

@Composable
fun rememberFileSizeFormatter(): FileSizeFormatter {
    val context = LocalContext.current
    val sdkIntProvider = LocalSdkIntVersionProvider.current
    return remember {
        AndroidFileSizeFormatter(context, sdkIntProvider)
    }
}
