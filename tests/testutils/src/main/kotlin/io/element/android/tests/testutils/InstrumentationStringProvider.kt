/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils

import androidx.test.platform.app.InstrumentationRegistry
import io.element.android.services.toolbox.api.strings.StringProvider

class InstrumentationStringProvider : StringProvider {
    private val resource = InstrumentationRegistry.getInstrumentation().context.resources
    override fun getString(resId: Int): String {
        return resource.getString(resId)
    }

    override fun getString(resId: Int, vararg formatArgs: Any?): String {
        return resource.getString(resId, *formatArgs)
    }

    override fun getQuantityString(resId: Int, quantity: Int, vararg formatArgs: Any?): String {
        return resource.getQuantityString(resId, quantity, *formatArgs)
    }
}
