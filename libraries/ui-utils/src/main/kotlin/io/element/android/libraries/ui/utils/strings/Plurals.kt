/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.utils.strings

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource

/**
 * Similar to [androidx.compose.ui.res.pluralStringResource] but with separate resource ids for singular and plural values.
 * Useful when we want to use different strings for singular and plural forms but not mentioning the actual quantity in the string.
 * In this case, we cannot use getQuantityString, because some locales have more than two plural forms, and require the quantity to
 * be part of the resulting strings.
 * @param resIdForOne Resource id for the case when [count] is 1.
 * @param resIdForOthers Resource id for the other cases ([count] is not 1).
 * @param count The quantity to determine whether to use singular or plural form. Must be greater than or equal to 1.
 * @param formatArgs The format arguments that will be used for substitution in the resulting string. Will be applied to either
 * the singular or plural string depending on the quantity.
 * @return The localized string corresponding to the given quantity.
 */
@Composable
@ReadOnlyComposable
fun simplePluralStringResource(
    @StringRes resIdForOne: Int,
    @StringRes resIdForOthers: Int,
    count: Int,
    vararg formatArgs: Any,
): String {
    val resId = if (count == 1) resIdForOne else resIdForOthers
    return stringResource(resId, *formatArgs)
}
