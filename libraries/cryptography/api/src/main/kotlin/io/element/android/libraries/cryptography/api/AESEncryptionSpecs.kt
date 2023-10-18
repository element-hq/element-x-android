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

package io.element.android.libraries.cryptography.api

import android.security.keystore.KeyProperties

object AESEncryptionSpecs {
    const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    const val PADDINGS = KeyProperties.ENCRYPTION_PADDING_NONE
    const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    const val KEY_SIZE = 128
    const val CIPHER_TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDINGS"
}
