/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.reportroom.impl

import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.matrix.api.core.RoomId

data class RoomIdPlugin(val roomId: RoomId): Plugin
