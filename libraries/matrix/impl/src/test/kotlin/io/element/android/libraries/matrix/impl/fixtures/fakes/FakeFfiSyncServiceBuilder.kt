/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.SyncService
import org.matrix.rustcomponents.sdk.SyncServiceBuilder

class FakeFfiSyncServiceBuilder : SyncServiceBuilder(NoPointer) {
    override fun withOfflineMode(): SyncServiceBuilder = this
    override suspend fun finish(): SyncService = FakeFfiSyncService()
}
