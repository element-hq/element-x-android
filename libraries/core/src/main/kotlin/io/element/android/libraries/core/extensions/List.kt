/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.core.extensions

/**
 * Returns the first element if the list contains exactly one element, otherwise returns null.
 */
inline fun <reified T> List<T>.firstIfSingle(): T? {
    return if (size == 1) first() else null
}
