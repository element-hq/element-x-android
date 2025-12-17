/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.test.core.app.ApplicationProvider

@Composable
fun withConfigurationAndContext(content: @Composable () -> Any?): Any? {
    var result: Any? = null
    CompositionLocalProvider(
        LocalConfiguration provides Configuration(),
        LocalContext provides ApplicationProvider.getApplicationContext(),
    ) {
        result = content()
    }
    return result
}
