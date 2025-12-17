/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.toolbox.api.strings

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

interface StringProvider {
    /**
     * Returns a localized string from the application's package's
     * default string table.
     *
     * @param resId Resource id for the string
     * @return The string data associated with the resource, stripped of styled
     * text information.
     */
    fun getString(@StringRes resId: Int): String

    /**
     * Returns a localized formatted string from the application's package's
     * default string table, substituting the format arguments as defined in
     * [java.util.Formatter] and [java.lang.String.format].
     *
     * @param resId Resource id for the format string
     * @param formatArgs The format arguments that will be used for
     * substitution.
     * @return The string data associated with the resource, formatted and
     * stripped of styled text information.
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String
    fun getQuantityString(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any?): String
}
