/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.permalink

import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.runCatchingExceptions
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

/**
 * This class turns a uri to a [PermalinkData].
 * element-based domains (e.g. https://app.element.io/#/user/@chagai95:matrix.org) permalinks
 * or matrix.to permalinks (e.g. https://matrix.to/#/@chagai95:matrix.org)
 * or client permalinks (e.g. <clientPermalinkBaseUrl>user/@chagai95:matrix.org)
 * or matrix: permalinks (e.g. matrix:u/chagai95:matrix.org)
 */
@ContributesBinding(AppScope::class)
class DefaultPermalinkParser(
    private val matrixToConverter: MatrixToConverter
) : PermalinkParser {
    /**
     * Turns a uri string to a [PermalinkData].
     * https://github.com/matrix-org/matrix-doc/blob/master/proposals/1704-matrix.to-permalinks.md
     */
    override fun parse(uriString: String): PermalinkData {
        val uri = uriString.toUri()
        val matrixToUri = if (uri.scheme == "matrix") {
            // take matrix: URI as is to [parseMatrixEntityFrom]
            uri
        } else {
            // the client or element-based domain permalinks (e.g. https://app.element.io/#/user/@chagai95:matrix.org) don't have the
            // mxid in the first param (like matrix.to does - https://matrix.to/#/@chagai95:matrix.org) but rather in the second after /user/ so /user/mxid
            // so convert URI to matrix.to to simplify parsing process
            matrixToConverter.convert(uri) ?: return PermalinkData.FallbackLink(uri)
        }

        val result = runCatchingExceptions {
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
