/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.permalink

import android.net.Uri

/**
 * Mapping of an input URI to a matrix.to compliant URI.
 */
interface MatrixToConverter {
    /**
     * Try to convert a URL from an element web instance or from a client permalink to a matrix.to url.
     * Examples:
     * - https://riot.im/develop/#/room/#element-android:matrix.org  ->  https://matrix.to/#/#element-android:matrix.org
     * - https://app.element.io/#/room/#element-android:matrix.org   ->  https://matrix.to/#/#element-android:matrix.org
     * - https://www.example.org/#/room/#element-android:matrix.org  ->  https://matrix.to/#/#element-android:matrix.org
     */
    fun convert(uri: Uri): Uri?
}
