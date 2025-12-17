/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.toolbox.impl.strings

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.services.toolbox.api.strings.StringProvider

@ContributesBinding(AppScope::class)
class AndroidStringProvider(private val resources: Resources) : StringProvider {
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
