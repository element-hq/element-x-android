/*
 * MIT License
 *
 * Copyright (c) 2024. DINUM
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fr.gouv.tchap.android.tests.konsist

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
}
