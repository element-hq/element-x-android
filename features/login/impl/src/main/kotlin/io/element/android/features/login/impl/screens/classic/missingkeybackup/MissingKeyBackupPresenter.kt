/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.classic.missingkeybackup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.login.impl.classic.ElementClassicConnection
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta

@Inject
class MissingKeyBackupPresenter(
    private val buildMeta: BuildMeta,
    private val elementClassicConnection: ElementClassicConnection,
) : Presenter<MissingKeyBackupState> {
    @Composable
    override fun present(): MissingKeyBackupState {
        var resumeCounter by remember { mutableIntStateOf(0) }
        fun handleEvent(event: MissingKeyBackupEvent) {
            when (event) {
                MissingKeyBackupEvent.OnResume -> {
                    resumeCounter++
                    if (resumeCounter > 1) {
                        // The user has returned to this screen, we can assume they have gone to the backup flow and are now back here
                        elementClassicConnection.requestSession()
                    }
                }
            }
        }

        return MissingKeyBackupState(
            appName = buildMeta.applicationName,
            eventSink = ::handleEvent,
        )
    }
}
