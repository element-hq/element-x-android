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

    /**
     * Returns a localized formatted string from the application's package's
     * default string table, substituting the format arguments as defined in
     * [java.util.Formatter] and [java.lang.String.format], based on the given quantity.
     */
    fun getQuantityString(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any?): String

    /**
     * Similar to [getQuantityString] but with separate resource ids for singular and plural values.
     * Useful when we want to use different strings for singular and plural forms but not mentioning the actual quantity in the string.
     * In this case, we cannot use getQuantityString, because some locales have more than two plural forms, and require the quantity to
     * be part of the resulting strings.
     * @param resIdForOne Resource id for the case when [quantity] is 1.
     * @param resIdForOthers Resource id for the other cases ([quantity] is not 1).
     * @param quantity The quantity to determine whether to use singular or plural form. Must be greater than or equal to 1.
     * @param formatArgs The format arguments that will be used for substitution in the resulting string. Will be applied to either
     * the singular or plural string depending on the quantity.
     * @return The localized string corresponding to the given quantity.
     */
    fun getSimpleQuantityString(
        @StringRes resIdForOne: Int,
        @StringRes resIdForOthers: Int,
        quantity: Int,
        vararg formatArgs: Any?,
    ): String
}
