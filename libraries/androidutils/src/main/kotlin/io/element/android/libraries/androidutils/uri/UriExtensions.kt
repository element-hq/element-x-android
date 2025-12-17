/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2021-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.uri

import android.net.Uri
import androidx.core.net.toUri

const val IGNORED_SCHEMA = "ignored"

fun createIgnoredUri(path: String): Uri = "$IGNORED_SCHEMA://$path".toUri()
