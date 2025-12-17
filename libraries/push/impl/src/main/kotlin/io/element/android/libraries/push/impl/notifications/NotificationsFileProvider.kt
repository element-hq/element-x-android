/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import androidx.core.content.FileProvider

/**
 * We have to declare our own file provider to avoid collision with other modules
 * having their own.
 */
class NotificationsFileProvider : FileProvider()
