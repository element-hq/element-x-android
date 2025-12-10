/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.text

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset

fun String.urlEncoded(charset: Charset = Charsets.UTF_8): String = URLEncoder.encode(this, charset.name())
fun String.urlDecoded(charset: Charset = Charsets.UTF_8): String = URLDecoder.decode(this, charset.name())
