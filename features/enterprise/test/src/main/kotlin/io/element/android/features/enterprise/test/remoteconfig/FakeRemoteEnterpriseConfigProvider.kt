/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.test.remoteconfig

import io.element.android.features.enterprise.api.remoteconfig.RemoteEnterpriseConfig
import io.element.android.features.enterprise.api.remoteconfig.RemoteEnterpriseConfigProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult

class FakeRemoteEnterpriseConfigProvider(
    private val getResult: (SessionId) -> WellknownRetrieverResult<RemoteEnterpriseConfig> = { WellknownRetrieverResult.NotFound }
) : RemoteEnterpriseConfigProvider {
    override suspend fun get(sessionId: SessionId): WellknownRetrieverResult<RemoteEnterpriseConfig> {
        return getResult(sessionId)
    }
}
