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

package io.element.android.libraries.matrix.impl.permalink

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.MatrixConfiguration
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilderError
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultPermalinkBuilder @Inject constructor() : PermalinkBuilder {
    private val permalinkBaseUrl
        get() = (MatrixConfiguration.clientPermalinkBaseUrl ?: MatrixConfiguration.MATRIX_TO_PERMALINK_BASE_URL).also {
            var baseUrl = it
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/"
            }
            if (!baseUrl.endsWith("/#/")) {
                baseUrl += "/#/"
            }
        }

    override fun permalinkForUser(userId: UserId): Result<String> {
        return if (MatrixPatterns.isUserId(userId.value)) {
            val url = buildString {
                append(permalinkBaseUrl)
                if (!isMatrixTo()) {
                    append(USER_PATH)
                }
                append(userId.value)
            }
            Result.success(url)
        } else {
            Result.failure(PermalinkBuilderError.InvalidUserId)
        }
    }

    override fun permalinkForRoomAlias(roomAlias: String): Result<String> {
        return if (MatrixPatterns.isRoomAlias(roomAlias)) {
            Result.success(permalinkForRoomAliasOrId(roomAlias))
        } else {
            Result.failure(PermalinkBuilderError.InvalidRoomAlias)
        }
    }

    override fun permalinkForRoomId(roomId: RoomId): Result<String> {
        return if (MatrixPatterns.isRoomId(roomId.value)) {
            Result.success(permalinkForRoomAliasOrId(roomId.value))
        } else {
            Result.failure(PermalinkBuilderError.InvalidRoomId)
        }
    }

    private fun permalinkForRoomAliasOrId(value: String): String {
        val id = escapeId(value)
        return buildString {
            append(permalinkBaseUrl)
            if (!isMatrixTo()) {
                append(ROOM_PATH)
            }
            append(id)
        }
    }

    private fun escapeId(value: String) = value.replace("/", "%2F")

    private fun isMatrixTo(): Boolean = permalinkBaseUrl.startsWith(MatrixConfiguration.MATRIX_TO_PERMALINK_BASE_URL)

    companion object {
        private const val ROOM_PATH = "room/"
        private const val USER_PATH = "user/"
    }
}
