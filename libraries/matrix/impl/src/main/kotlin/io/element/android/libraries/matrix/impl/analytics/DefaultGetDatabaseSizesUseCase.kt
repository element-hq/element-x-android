/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.analytics

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.data.bytes
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.analytics.GetDatabaseSizesUseCase
import io.element.android.libraries.matrix.api.analytics.SdkStoreSizes
import io.element.android.libraries.matrix.api.core.SessionId
import java.io.File
import kotlin.math.max

/**
 * An implementation of [GetDatabaseSizesUseCase] that manually calculates the database sizes by checking the file system.
 * This is not as accurate as the SDK's internal method, but is used as a fallback for Sentry's non-suspendable hook where
 * calling the SDK's suspendable method blocks the main thread and causes ANRs.
 */
@ContributesBinding(AppScope::class)
class DefaultGetDatabaseSizesUseCase(
    private val clientProvider: Lazy<MatrixClientProvider>,
) : GetDatabaseSizesUseCase {
    override fun invoke(sessionId: SessionId): Result<SdkStoreSizes> {
        val client = clientProvider.value.getOrNull(sessionId)
            ?: return Result.failure(IllegalArgumentException("No MatrixClient for session $sessionId"))

        val fileDir = client.sessionPaths.fileDirectory
        val cacheDir = client.sessionPaths.cacheDirectory

        val cryptoSize = getDatabaseSize(fileDir, "matrix-sdk-crypto")
        val stateSize = getDatabaseSize(fileDir, "matrix-sdk-state")

        val eventCacheSize = getDatabaseSize(cacheDir, "matrix-sdk-event-cache")
        val mediaCacheSize = getDatabaseSize(cacheDir, "matrix-sdk-media")

        return Result.success(SdkStoreSizes(
            stateStore = stateSize.bytes,
            cryptoStore = cryptoSize.bytes,
            eventCacheStore = eventCacheSize.bytes,
            mediaStore = mediaCacheSize.bytes,
        ))
    }

    private fun getDatabaseSize(baseDirectory: File, databaseFileName: String): Long {
        // Check the sizes of both the main database file and the -wal file, and return the max of the two:
        // The -wal file can contain additional data not yet merged into the main file, becoming larger than it.
        val databaseFile = File(baseDirectory, "$databaseFileName.sqlite3")
        val walFile = File(baseDirectory, "$databaseFileName.sqlite3-wal")

        val databaseSize = if (databaseFile.exists()) databaseFile.length() else 0L
        val walSize = if (walFile.exists()) walFile.length() else 0L

        return max(databaseSize, walSize)
    }
}
