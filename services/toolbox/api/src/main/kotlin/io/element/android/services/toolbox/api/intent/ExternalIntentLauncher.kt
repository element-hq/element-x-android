/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.toolbox.api.intent

import android.content.Intent

/**
 * Used to launch external intents from anywhere in the app.
 */
interface ExternalIntentLauncher {
    fun launch(intent: Intent)
}
