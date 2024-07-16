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

package io.element.android.libraries.matrix.test

import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.SpaceId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings

const val A_USER_NAME = "alice"
const val A_PASSWORD = "password"
const val A_SECRET = "secret"

val A_USER_ID = UserId("@alice:server.org")
val A_USER_ID_2 = UserId("@bob:server.org")
val A_USER_ID_3 = UserId("@carol:server.org")
val A_USER_ID_4 = UserId("@david:server.org")
val A_USER_ID_5 = UserId("@eve:server.org")
val A_USER_ID_6 = UserId("@justin:server.org")
val A_USER_ID_7 = UserId("@mallory:server.org")
val A_USER_ID_8 = UserId("@susie:server.org")
val A_USER_ID_9 = UserId("@victor:server.org")
val A_USER_ID_10 = UserId("@walter:server.org")
val A_SESSION_ID: SessionId = A_USER_ID
val A_SESSION_ID_2: SessionId = A_USER_ID_2
val A_SPACE_ID = SpaceId("!aSpaceId:domain")
val A_SPACE_ID_2 = SpaceId("!aSpaceId2:domain")
val A_ROOM_ID = RoomId("!aRoomId:domain")
val A_ROOM_ID_2 = RoomId("!aRoomId2:domain")
val A_ROOM_ID_3 = RoomId("!aRoomId3:domain")
val A_THREAD_ID = ThreadId("\$aThreadId")
val A_THREAD_ID_2 = ThreadId("\$aThreadId2")
val AN_EVENT_ID = EventId("\$anEventId")
val AN_EVENT_ID_2 = EventId("\$anEventId2")
val A_ROOM_ALIAS = RoomAlias("#alias1:domain")
val A_TRANSACTION_ID = TransactionId("aTransactionId")
const val A_UNIQUE_ID = "aUniqueId"

const val A_ROOM_NAME = "A room name"
const val A_ROOM_RAW_NAME = "A room raw name"
const val A_MESSAGE = "Hello world!"
const val A_REPLY = "OK, I'll be there!"
const val ANOTHER_MESSAGE = "Hello universe!"

const val A_HOMESERVER_URL = "matrix.org"
const val A_HOMESERVER_URL_2 = "matrix-client.org"

val A_HOMESERVER = MatrixHomeServerDetails(A_HOMESERVER_URL, supportsPasswordLogin = true, supportsOidcLogin = false)
val A_HOMESERVER_OIDC = MatrixHomeServerDetails(A_HOMESERVER_URL, supportsPasswordLogin = false, supportsOidcLogin = true)
val A_ROOM_NOTIFICATION_MODE = RoomNotificationMode.MUTE
val A_ROOM_NOTIFICATION_SETTINGS = RoomNotificationSettings(mode = A_ROOM_NOTIFICATION_MODE, isDefault = false)

const val AN_AVATAR_URL = "mxc://data"

const val A_FAILURE_REASON = "There has been a failure"

val A_THROWABLE = Throwable(A_FAILURE_REASON)
val AN_EXCEPTION = Exception(A_FAILURE_REASON)

const val A_RECOVERY_KEY = "1234 5678"

val A_SERVER_LIST = listOf("server1", "server2")
