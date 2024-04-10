/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
