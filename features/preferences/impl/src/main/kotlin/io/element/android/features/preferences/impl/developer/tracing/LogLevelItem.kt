/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.tracing

import androidx.compose.runtime.Composable
import io.element.android.libraries.designsystem.components.preferences.DropdownOption

enum class LogLevelItem : DropdownOption {
    ERROR {
        @Composable
        override fun getText(): String = "Error"
    },
    WARN {
        @Composable
        override fun getText(): String = "Warn"
    },
    INFO {
        @Composable
        override fun getText(): String = "Info"
    },
    DEBUG {
        @Composable
        override fun getText(): String = "Debug"
    },
    TRACE {
        @Composable
        override fun getText(): String = "Trace"
    }
}
