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
