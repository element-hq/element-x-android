/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.classic.missingkeybackup

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta

@Inject
class MissingKeyBackupPresenter(
    private val buildMeta: BuildMeta,
) : Presenter<MissingKeyBackupState> {
    @Composable
    override fun present(): MissingKeyBackupState {
        return MissingKeyBackupState(
            appName = buildMeta.applicationName,
        )
    }
}
