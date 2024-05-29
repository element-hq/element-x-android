/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.tests.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withParameter
import com.lemonappdev.konsist.api.verify.assertEmpty
import org.junit.Test

class KonsistParameterNameTest {
    @Test
    fun `Function parameter should not end with 'Press' but with 'Click'`() {
        Konsist.scopeFromProject()
            .functions()
            .withParameter { parameter ->
                parameter.name.endsWith("Press")
            }
            .assertEmpty(additionalMessage = "Please rename the parameter, for instance from 'onBackPress' to 'onBackClick'.")
    }
}
