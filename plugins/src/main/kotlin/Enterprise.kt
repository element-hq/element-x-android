/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

/**
 * Are we building with the enterprise sources?
 * The value is computed once in settings.gradle.kts and stored as an extra property of the Gradle instance,
 * see the `element.enterprise` Gradle property.
 */
val Project.isEnterpriseBuild: Boolean
    get() = gradle.extra["isEnterpriseBuild"] as Boolean
