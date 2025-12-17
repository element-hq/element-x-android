/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl.previews

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import io.element.android.services.toolbox.api.strings.StringProvider

class PreviewStringProvider(
    private val resources: Resources
) : StringProvider {
    override fun getString(@StringRes resId: Int): String {
        return resources.getString(resId)
    }

    override fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String {
        return resources.getString(resId, *formatArgs)
    }

    override fun getQuantityString(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any?): String {
        return resources.getQuantityString(resId, quantity, *formatArgs)
    }
}
