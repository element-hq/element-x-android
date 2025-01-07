/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.toolbox.test.intent

import android.content.Intent
import io.element.android.services.toolbox.api.intent.ExternalIntentLauncher

class FakeExternalIntentLauncher(
    var launchLambda: (Intent) -> Unit = {},
) : ExternalIntentLauncher {
    override fun launch(intent: Intent) {
        launchLambda(intent)
    }
}
