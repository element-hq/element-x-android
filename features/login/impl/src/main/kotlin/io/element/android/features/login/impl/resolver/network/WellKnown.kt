/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
