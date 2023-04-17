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

package io.element.android.libraries.pushstore.impl.clientsecret

import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretFactory

private const val A_SECRET_PREFIX = "A_SECRET_"

class FakePushClientSecretFactory : PushClientSecretFactory {
    private var index = 0

    override fun create() = getSecretForUser(index++)

    fun getSecretForUser(i: Int): String {
        return A_SECRET_PREFIX + i
    }
}
