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
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class KonsistLicenseTest {
    private val tchapLicense = """
        /\*
         \* MIT License
         \*
         \* Copyright \(c\) 20\d\d\. DINUM
         \*
         \* Permission is hereby granted, free of charge, to any person obtaining a copy
         \* of this software and associated documentation files \(the "Software"\), to deal
         \* in the Software without restriction, including without limitation the rights
         \* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
         \* copies of the Software, and to permit persons to whom the Software is
         \* furnished to do so, subject to the following conditions:
         \*
         \* The above copyright notice and this permission notice shall be included in all
         \* copies or substantial portions of the Software\.
         \*
         \* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
         \* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
         \* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT\.
         \* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
         \* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
         \* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
         \* OR OTHER DEALINGS IN THE SOFTWARE\.
         \*/
        """.trimIndent().toRegex()

    private val publicLicense = """
        /\*
        (?:.*\n)* \* Copyright \(c\) 20\d\d New Vector Ltd
        (?:.*\n)* \*
         \* Licensed under the Apache License, Version 2\.0 \(the "License"\);
         \* you may not use this file except in compliance with the License\.
         \* You may obtain a copy of the License at
         \*
         \* {5}https?://www\.apache\.org/licenses/LICENSE-2\.0
         \*
         \* Unless required by applicable law or agreed to in writing, software
         \* distributed under the License is distributed on an "AS IS" BASIS,
         \* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied\.
         \* See the License for the specific language governing permissions and
         \* limitations under the License\.
         \*/
        """.trimIndent().toRegex()

    private val enterpriseLicense = """
        /\*
         \* © 20\d\d New Vector Limited, Element Software SARL, Element Software Inc\.,
         \* and Element Software GmbH \(the "Element Group"\) only make this file available
         \* under a proprietary license model\.
         \*
         \* Without a proprietary license with us, you cannot use this file\. The terms of
         \* the proprietary license agreement between you and any member of the Element Group
         \* shall always apply to your use of this file\. Unauthorised use, copying, distribution,
         \* or modification of this file, via any medium, is strictly prohibited\.
         \*
         \* For details about the licensing terms, you must either visit our website or contact
         \* a member of our sales team\.
         \*/
        """.trimIndent().toRegex()

    @Test
    fun `assert that Tchap files have the correct license header`() {
        Konsist
            .scopeFromProject()
            .files
            .filter {
                it.path.contains("/fr/gouv/tchap/")
            }
            .assertTrue {
                tchapLicense.containsMatchIn(it.text)
            }
    }

    @Test
    fun `assert that FOSS files have the correct license header`() {
        Konsist
            .scopeFromProject()
            .files
            .filter {
                it.path.contains("/enterprise/features").not() &&
                    it.path.contains("/fr/gouv/tchap/").not() &&
                    it.nameWithExtension != "locales.kt" &&
                    it.name.startsWith("Template ").not()
            }
            .assertTrue {
                publicLicense.containsMatchIn(it.text)
            }
    }

    @Test
    fun `assert that Enterprise files have the correct license header`() {
        Konsist
            .scopeFromProject()
            .files
            .filter {
                it.path.contains("/enterprise/features")
            }
            .assertTrue {
                enterpriseLicense.containsMatchIn(it.text)
            }
    }
}
