/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appnav.di

import io.element.android.libraries.matrix.api.room.MatrixRoom

interface RoomComponentFactory {
    fun create(room: MatrixRoom): Any
}
