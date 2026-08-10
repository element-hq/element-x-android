/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.test.remoteconfig

import io.element.android.features.enterprise.api.remoteconfig.RemoteEnterpriseConfigProvider
import io.element.android.libraries.matrix.api.GetUrlResolver

class FakeRemoteEnterpriseConfigProviderFactory(
    private val remoteEnterpriseConfigProvider: RemoteEnterpriseConfigProvider = FakeRemoteEnterpriseConfigProvider(),
) : RemoteEnterpriseConfigProvider.Factory {
    override fun create(getUrlResolver: GetUrlResolver): RemoteEnterpriseConfigProvider = remoteEnterpriseConfigProvider
}
