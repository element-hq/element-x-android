/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils

import com.google.common.truth.Subject

/**
 * A helper class to assert expected results in tests. It can be used to assert either the type of result or its value.
 */
data class ExpectedResult<T : Any>(
    val resultClass: Class<out T>,
    val result: T? = null,
) {
    constructor(result: T) : this(result::class.java, result)
}

fun <T : Any> Subject.match(expectedResult: ExpectedResult<T>) {
    if (expectedResult.result != null) {
        isEqualTo(expectedResult.result)
    } else {
        isInstanceOf(expectedResult.resultClass)
    }
}
