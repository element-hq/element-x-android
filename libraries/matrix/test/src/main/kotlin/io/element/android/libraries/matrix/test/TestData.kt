/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test

import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.SpaceId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings

const val A_USER_NAME = "alice"
const val A_USER_NAME_2 = "Bob"
const val A_PASSWORD = "password"
const val A_PASSPHRASE = "passphrase"
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
val A_DEVICE_ID = DeviceId("ILAKNDNASDLK")

val A_UNIQUE_ID = UniqueId("aUniqueId")
val A_UNIQUE_ID_2 = UniqueId("aUniqueId2")

const val A_ROOM_NAME = "A room name"
const val A_ROOM_TOPIC = "A room topic"
const val A_ROOM_RAW_NAME = "A room raw name"
const val A_MESSAGE = "Hello world!"
const val A_REPLY = "OK, I'll be there!"
const val ANOTHER_MESSAGE = "Hello universe!"
const val A_CAPTION = "A media caption"

const val A_REDACTION_REASON = "A redaction reason"

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

const val A_TIMESTAMP = 567L
