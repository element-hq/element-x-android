/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.tracing

import io.element.android.libraries.designsystem.components.preferences.DropdownOption

enum class LogLevelItem : DropdownOption {
    ERROR {
        override val text: String = "Error"
    },
    WARN {
        override val text: String = "Warn"
    },
    INFO {
        override val text: String = "Info"
    },
    DEBUG {
        override val text: String = "Debug"
    },
    TRACE {
        override val text: String = "Trace"
    }
}
