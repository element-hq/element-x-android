/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.test.remoteconfig

import io.element.android.features.enterprise.api.remoteconfig.RemoteEnterpriseConfig
import io.element.android.features.enterprise.api.remoteconfig.SessionRemoteEnterpriseConfigProvider
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult

class FakeSessionRemoteEnterpriseConfigProvider(
    private val getResult: () -> WellknownRetrieverResult<RemoteEnterpriseConfig> = { WellknownRetrieverResult.NotFound }
) : SessionRemoteEnterpriseConfigProvider {
    override suspend fun get(): WellknownRetrieverResult<RemoteEnterpriseConfig> {
        return getResult()
    }
}
