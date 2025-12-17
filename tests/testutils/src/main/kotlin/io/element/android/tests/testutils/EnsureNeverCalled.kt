/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils

import io.element.android.tests.testutils.lambda.lambdaError

class EnsureNeverCalled : () -> Unit {
    override fun invoke() {
        lambdaError()
    }
}

class EnsureNeverCalledWithParam<T> : (T) -> Unit {
    override fun invoke(p1: T) {
        lambdaError("Should not be called and is called with $p1")
    }
}

class EnsureNeverCalledWithParamAndResult<T, R> : (T) -> R {
    override fun invoke(p1: T): R {
        lambdaError("Should not be called and is called with $p1")
    }
}

class EnsureNeverCalledWithTwoParams<T, U> : (T, U) -> Unit {
    override fun invoke(p1: T, p2: U) {
        lambdaError("Should not be called and is called with $p1 and $p2")
    }
}

class EnsureNeverCalledWithTwoParamsAndResult<T, U, R> : (T, U) -> R {
    override fun invoke(p1: T, p2: U): R {
        lambdaError("Should not be called and is called with $p1 and $p2")
    }
}

class EnsureNeverCalledWithThreeParams<T, U, V> : (T, U, V) -> Unit {
    override fun invoke(p1: T, p2: U, p3: V) {
        lambdaError("Should not be called and is called with $p1, $p2 and $p3")
    }
}
