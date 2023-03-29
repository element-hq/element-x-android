/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.api.permalink

import io.element.android.libraries.matrix.api.config.MatrixConfiguration
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.RoomId

object PermalinkBuilder {
    private val permalinkBaseUrl get() = (MatrixConfiguration.clientPermalinkBaseUrl ?: MatrixConfiguration.matrixToPermalinkBaseUrl).also {
        var baseUrl = it
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/"
        }
        if (!baseUrl.endsWith("/#/")) {
            baseUrl += "/#/"
        }
    }

    fun permalinkForRoomAlias(roomAlias: String): Result<String> {
        return if (MatrixPatterns.isRoomAlias(roomAlias)) {
            Result.success(permalinkForRoomAliasOrId(roomAlias))
        } else {
            Result.failure(PermalinkBuilderError.InvalidRoomAlias)
        }
    }

    fun permalinkForRoomId(roomId: RoomId): Result<String> {
        return if (MatrixPatterns.isRoomId(roomId.value)) {
            Result.success(permalinkForRoomAliasOrId(roomId.value))
        } else {
            Result.failure(PermalinkBuilderError.InvalidRoomId)
        }
    }

    private fun permalinkForRoomAliasOrId(value: String): String {
        val id = escapeId(value)
        return permalinkBaseUrl + id
    }

    private fun escapeId(value: String) = value.replace("/", "%2F")
}

sealed class PermalinkBuilderError : Throwable() {
    object InvalidRoomAlias : PermalinkBuilderError()
    object InvalidRoomId : PermalinkBuilderError()
}
