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

package io.element.android.features.login.impl.util

import io.element.android.features.login.impl.accountprovider.AccountProvider

object LoginConstants {
    const val MATRIX_ORG_URL = "https://matrix.org"

    const val DEFAULT_HOMESERVER_URL = "https://matrix.org"
    const val SLIDING_SYNC_READ_MORE_URL = "https://github.com/matrix-org/sliding-sync/blob/main/docs/Landing.md"
}

val defaultAccountProvider = AccountProvider(
    url = LoginConstants.DEFAULT_HOMESERVER_URL,
    subtitle = null,
    isPublic = LoginConstants.DEFAULT_HOMESERVER_URL == LoginConstants.MATRIX_ORG_URL,
    isMatrixOrg = LoginConstants.DEFAULT_HOMESERVER_URL == LoginConstants.MATRIX_ORG_URL,
)
