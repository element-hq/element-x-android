/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class KonsistLicenseTest {
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
    fun `assert that FOSS files have the correct license header`() {
        Konsist
            .scopeFromProject()
            .files
            .filter {
                it.path.contains("/enterprise/features").not() &&
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
