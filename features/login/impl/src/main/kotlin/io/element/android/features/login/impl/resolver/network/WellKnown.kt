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

package io.element.android.features.login.impl.resolver.network

import io.element.android.libraries.core.bool.orFalse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://matrix.org/docs/spec/client_server/r0.4.0.html#server-discovery
 * <pre>
 * {
 *     "m.homeserver": {
 *         "base_url": "https://matrix.org"
 *     },
 *     "m.identity_server": {
 *         "base_url": "https://vector.im"
 *     },
 *     "org.matrix.msc3575.proxy": {
 *         "url": "https://slidingsync.lab.matrix.org"
 *     }
 * }
 * </pre>
 * .
 */
@Serializable
data class WellKnown(
    @SerialName("m.homeserver")
    val homeServer: WellKnownBaseConfig? = null,
    @SerialName("m.identity_server")
    val identityServer: WellKnownBaseConfig? = null,
    @SerialName("org.matrix.msc3575.proxy")
    val slidingSyncProxy: WellKnownSlidingSyncConfig? = null,
) {
    fun isValid(): Boolean {
        return homeServer?.baseURL?.isNotBlank().orFalse()
    }

    fun supportSlidingSync(): Boolean {
        return slidingSyncProxy?.url?.isNotBlank().orFalse()
    }
}
