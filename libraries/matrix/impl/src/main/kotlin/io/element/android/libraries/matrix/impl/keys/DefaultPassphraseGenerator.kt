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

package io.element.android.libraries.matrix.impl.keys

import android.util.Base64
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import java.security.SecureRandom
import javax.inject.Inject

private const val SECRET_SIZE = 256

@ContributesBinding(AppScope::class)
class DefaultPassphraseGenerator @Inject constructor() : PassphraseGenerator {
    override fun generatePassphrase(): String? {
        val key = ByteArray(size = SECRET_SIZE)
        SecureRandom().nextBytes(key)
        return Base64.encodeToString(key, Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
