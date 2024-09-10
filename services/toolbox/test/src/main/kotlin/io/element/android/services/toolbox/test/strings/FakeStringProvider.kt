/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.toolbox.test.strings

import io.element.android.services.toolbox.api.strings.StringProvider

class FakeStringProvider(
    private val defaultResult: String = "A string"
) : StringProvider {
    override fun getString(resId: Int): String {
        return defaultResult
    }

    override fun getString(resId: Int, vararg formatArgs: Any?): String {
        return defaultResult + formatArgs.joinToString()
    }

    override fun getQuantityString(resId: Int, quantity: Int, vararg formatArgs: Any?): String {
        return defaultResult + " ($quantity) " + formatArgs.joinToString()
    }
}
