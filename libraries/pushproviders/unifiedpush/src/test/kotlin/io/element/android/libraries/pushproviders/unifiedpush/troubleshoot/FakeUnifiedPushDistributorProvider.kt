/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush.troubleshoot

import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushDistributorProvider

class FakeUnifiedPushDistributorProvider(
    private var getDistributorsResult: List<Distributor> = emptyList()
) : UnifiedPushDistributorProvider {
    override fun getDistributors(): List<Distributor> {
        return getDistributorsResult
    }

    fun setDistributorsResult(list: List<Distributor>) {
        getDistributorsResult = list
    }
}
