/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

import java.io.File

/**
 * Are we building with the enterprise sources?
 */
val isEnterpriseBuild = File("enterprise/README.md").exists()
