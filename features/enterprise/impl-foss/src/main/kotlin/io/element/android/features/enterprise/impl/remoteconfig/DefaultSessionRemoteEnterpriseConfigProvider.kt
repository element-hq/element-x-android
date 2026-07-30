/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.impl.remoteconfig

import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.enterprise.api.remoteconfig.RemoteEnterpriseConfig
import io.element.android.features.enterprise.api.remoteconfig.SessionRemoteEnterpriseConfigProvider
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult

@ContributesBinding(SessionScope::class)
class DefaultSessionRemoteEnterpriseConfigProvider : SessionRemoteEnterpriseConfigProvider {
    override suspend fun get(): WellknownRetrieverResult<RemoteEnterpriseConfig> {
        return WellknownRetrieverResult.NotFound
    }
}
