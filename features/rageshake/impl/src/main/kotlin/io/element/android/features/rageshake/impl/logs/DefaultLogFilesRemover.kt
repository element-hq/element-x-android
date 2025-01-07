/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.logs

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.rageshake.api.logs.LogFilesRemover
import io.element.android.features.rageshake.impl.reporter.DefaultBugReporter
import io.element.android.libraries.di.AppScope
import java.io.File
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultLogFilesRemover @Inject constructor(
    private val bugReporter: DefaultBugReporter,
) : LogFilesRemover {
    override suspend fun perform(predicate: (File) -> Boolean) {
        bugReporter.deleteAllFiles(predicate)
    }
}
