/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.toolbox.api.intent

import android.content.Intent

/**
 * Used to launch external intents from anywhere in the app.
 */
interface ExternalIntentLauncher {
    fun launch(intent: Intent)
}
