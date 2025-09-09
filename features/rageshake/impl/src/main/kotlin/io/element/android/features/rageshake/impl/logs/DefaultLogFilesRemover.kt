/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.logs

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.features.rageshake.api.logs.LogFilesRemover
import io.element.android.features.rageshake.impl.reporter.DefaultBugReporter
import java.io.File

@ContributesBinding(AppScope::class)
@Inject class DefaultLogFilesRemover(
    private val bugReporter: DefaultBugReporter,
) : LogFilesRemover {
    override suspend fun perform(predicate: (File) -> Boolean) {
        bugReporter.deleteAllFiles(predicate)
    }
}
