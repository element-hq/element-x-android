/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.storage

import io.element.android.libraries.matrix.impl.paths.SessionPaths

class FakeSqliteStoreBuilderProvider : SqliteStoreBuilderProvider {
    override fun provide(sessionPaths: SessionPaths): SqliteStoreBuilder {
        return FakeSqliteStoreBuilder()
    }
}
