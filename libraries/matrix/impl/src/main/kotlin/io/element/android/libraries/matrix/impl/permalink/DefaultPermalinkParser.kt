/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.permalink

import android.net.Uri
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.MatrixToConverter
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import kotlinx.collections.immutable.toImmutableList
import org.matrix.rustcomponents.sdk.MatrixId
import org.matrix.rustcomponents.sdk.parseMatrixEntityFrom
import javax.inject.Inject

/**
 * This class turns a uri to a [PermalinkData].
 * element-based domains (e.g. https://app.element.io/#/user/@chagai95:matrix.org) permalinks
 * or matrix.to permalinks (e.g. https://matrix.to/#/@chagai95:matrix.org)
 * or client permalinks (e.g. <clientPermalinkBaseUrl>user/@chagai95:matrix.org)
 */
@ContributesBinding(AppScope::class)
class DefaultPermalinkParser @Inject constructor(
    private val matrixToConverter: MatrixToConverter
) : PermalinkParser {
    /**
     * Turns a uri string to a [PermalinkData].
     * https://github.com/matrix-org/matrix-doc/blob/master/proposals/1704-matrix.to-permalinks.md
     */
    override fun parse(uriString: String): PermalinkData {
        val uri = Uri.parse(uriString)
        // the client or element-based domain permalinks (e.g. https://app.element.io/#/user/@chagai95:matrix.org) don't have the
        // mxid in the first param (like matrix.to does - https://matrix.to/#/@chagai95:matrix.org) but rather in the second after /user/ so /user/mxid
        // so convert URI to matrix.to to simplify parsing process
        val matrixToUri = matrixToConverter.convert(uri) ?: return PermalinkData.FallbackLink(uri)

        val result = runCatching {
            parseMatrixEntityFrom(matrixToUri.toString())
        }.getOrNull()
        return if (result == null) {
            PermalinkData.FallbackLink(uri)
        } else {
            val viaParameters = result.via.toImmutableList()
            when (val id = result.id) {
                is MatrixId.User -> PermalinkData.UserLink(
                    userId = UserId(id.id),
                )
                is MatrixId.Room -> PermalinkData.RoomLink(
                    roomIdOrAlias = RoomId(id.id).toRoomIdOrAlias(),
                    viaParameters = viaParameters,
                )
                is MatrixId.RoomAlias -> PermalinkData.RoomLink(
                    roomIdOrAlias = RoomAlias(id.alias).toRoomIdOrAlias(),
                    viaParameters = viaParameters,
                )
                is MatrixId.EventOnRoomId -> PermalinkData.RoomLink(
                    roomIdOrAlias = RoomId(id.roomId).toRoomIdOrAlias(),
                    eventId = EventId(id.eventId),
                    viaParameters = viaParameters,
                )
                is MatrixId.EventOnRoomAlias -> PermalinkData.RoomLink(
                    roomIdOrAlias = RoomAlias(id.alias).toRoomIdOrAlias(),
                    eventId = EventId(id.eventId),
                    viaParameters = viaParameters,
                )
            }
        }
    }
}
