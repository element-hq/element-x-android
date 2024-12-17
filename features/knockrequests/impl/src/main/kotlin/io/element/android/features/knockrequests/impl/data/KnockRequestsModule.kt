/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.data

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.room.MatrixRoom

@Module
@ContributesTo(RoomScope::class)
object KnockRequestsModule {
    @Provides
    @SingleIn(RoomScope::class)
    fun knockRequestsService(room: MatrixRoom): KnockRequestsService {
        return KnockRequestsService(room.knockRequestsFlow, room.roomCoroutineScope)
    }
}
