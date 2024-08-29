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
        (?:.*\n)* \* Copyright 20\d\d((, |-)20\d\d)? New Vector Ltd.
        (?:.*\n)* \*
         \* SPDX-License-Identifier: AGPL-3.0-only
         \* Please see LICENSE in the repository root for full details.
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
