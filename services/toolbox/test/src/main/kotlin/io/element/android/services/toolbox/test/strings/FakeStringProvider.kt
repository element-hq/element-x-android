/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.toolbox.test.strings

import io.element.android.services.toolbox.api.strings.StringProvider

class FakeStringProvider(
    private val defaultResult: String = "A string"
) : StringProvider {
    var lastResIdParam: Int? = null
    override fun getString(resId: Int): String {
        lastResIdParam = resId
        return defaultResult
    }

    override fun getString(resId: Int, vararg formatArgs: Any?): String {
        lastResIdParam = resId
        return defaultResult + formatArgs.joinToString()
    }

    override fun getQuantityString(resId: Int, quantity: Int, vararg formatArgs: Any?): String {
        lastResIdParam = resId
        return defaultResult + " ($quantity) " + formatArgs.joinToString()
    }
}
