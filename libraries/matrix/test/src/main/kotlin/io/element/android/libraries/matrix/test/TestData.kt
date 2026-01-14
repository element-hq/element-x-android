/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test

import androidx.annotation.ColorInt
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

const val A_USER_NAME = "alice"
const val A_USER_NAME_2 = "Bob"
const val A_PASSWORD = "password"
const val A_PASSPHRASE = "passphrase"
const val A_SECRET = "secret"
const val AN_APPLICATION_NAME = "AppName"
const val AN_APPLICATION_NAME_DESKTOP = "AppNameDesktop"

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
val A_ROOM_ID_4 = RoomId("!aRoomId4:domain")
val A_THREAD_ID = ThreadId("\$aThreadId")
val A_THREAD_ID_2 = ThreadId("\$aThreadId2")
val AN_EVENT_ID = EventId("\$anEventId")
val AN_EVENT_ID_2 = EventId("\$anEventId2")
val AN_EVENT_ID_3 = EventId("\$anEventId3")
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
const val A_REASON = "A reason"

const val A_SPACE_NAME = "A space name"

const val A_REDACTION_REASON = "A redaction reason"

const val A_HOMESERVER_URL = "matrix.org"
const val A_HOMESERVER_URL_2 = "matrix-client.org"

const val AN_ACCOUNT_PROVIDER_URL = "https://account.provider.org"
const val AN_ACCOUNT_PROVIDER = "matrix.org"
const val AN_ACCOUNT_PROVIDER_2 = "element.io"
const val AN_ACCOUNT_PROVIDER_3 = "other.io"

val A_ROOM_NOTIFICATION_MODE = RoomNotificationMode.MUTE

const val AN_AVATAR_URL = "mxc://data"

const val A_FAILURE_REASON = "There has been a failure"

@Suppress("unused")
val A_THROWABLE = Throwable(A_FAILURE_REASON)
val AN_EXCEPTION = Exception(A_FAILURE_REASON)

const val A_RECOVERY_KEY = "1234 5678"

val A_SERVER_LIST = listOf("server1", "server2")

const val A_TIMESTAMP = 567L
const val A_FORMATTED_DATE = "April 6, 1980 at 6:35 PM"

const val A_LOGIN_HINT = "mxid:@alice:example.org"

@ColorInt
const val A_COLOR_INT: Int = 0xFFFF0000.toInt()

// From https://github.com/matrix-org/matrix-rust-sdk/blob/3a63838cdb50cde3d74da920186fbae0a2e6db37/crates/matrix-sdk-crypto/src/types/qr_login.rs#L275
// Test vector for the QR code data, copied from the MSC.
@Suppress("ktlint:standard:argument-list-wrapping")
val QR_CODE_DATA = listOf(
    0x4D, 0x41, 0x54, 0x52, 0x49, 0x58, 0x02, 0x03, 0xd8, 0x86, 0x68, 0x6a, 0xb2, 0x19, 0x7b,
    0x78, 0x0e, 0x30, 0x0a, 0x9d, 0x4a, 0x21, 0x47, 0x48, 0x07, 0x00, 0xd7, 0x92, 0x9f, 0x39,
    0xab, 0x31, 0xb9, 0xe5, 0x14, 0x37, 0x02, 0x48, 0xed, 0x6b, 0x00, 0x47, 0x68, 0x74, 0x74,
    0x70, 0x73, 0x3a, 0x2f, 0x2f, 0x72, 0x65, 0x6e, 0x64, 0x65, 0x7a, 0x76, 0x6f, 0x75, 0x73,
    0x2e, 0x6c, 0x61, 0x62, 0x2e, 0x65, 0x6c, 0x65, 0x6d, 0x65, 0x6e, 0x74, 0x2e, 0x64, 0x65,
    0x76, 0x2f, 0x65, 0x38, 0x64, 0x61, 0x36, 0x33, 0x35, 0x35, 0x2d, 0x35, 0x35, 0x30, 0x62,
    0x2d, 0x34, 0x61, 0x33, 0x32, 0x2d, 0x61, 0x31, 0x39, 0x33, 0x2d, 0x31, 0x36, 0x31, 0x39,
    0x64, 0x39, 0x38, 0x33, 0x30, 0x36, 0x36, 0x38,
).map { it.toByte() }.toByteArray()

// Test vector for the QR code data, copied from the MSC, with the mode set to reciprocate.
@Suppress("ktlint:standard:argument-list-wrapping")
val QR_CODE_DATA_RECIPROCATE = listOf(
    0x4D, 0x41, 0x54, 0x52, 0x49, 0x58, 0x02, 0x04, 0xd8, 0x86, 0x68, 0x6a, 0xb2, 0x19, 0x7b,
    0x78, 0x0e, 0x30, 0x0a, 0x9d, 0x4a, 0x21, 0x47, 0x48, 0x07, 0x00, 0xd7, 0x92, 0x9f, 0x39,
    0xab, 0x31, 0xb9, 0xe5, 0x14, 0x37, 0x02, 0x48, 0xed, 0x6b, 0x00, 0x47, 0x68, 0x74, 0x74,
    0x70, 0x73, 0x3a, 0x2f, 0x2f, 0x72, 0x65, 0x6e, 0x64, 0x65, 0x7a, 0x76, 0x6f, 0x75, 0x73,
    0x2e, 0x6c, 0x61, 0x62, 0x2e, 0x65, 0x6c, 0x65, 0x6d, 0x65, 0x6e, 0x74, 0x2e, 0x64, 0x65,
    0x76, 0x2f, 0x65, 0x38, 0x64, 0x61, 0x36, 0x33, 0x35, 0x35, 0x2d, 0x35, 0x35, 0x30, 0x62,
    0x2d, 0x34, 0x61, 0x33, 0x32, 0x2d, 0x61, 0x31, 0x39, 0x33, 0x2d, 0x31, 0x36, 0x31, 0x39,
    0x64, 0x39, 0x38, 0x33, 0x30, 0x36, 0x36, 0x38, 0x00, 0x0A, 0x6d, 0x61, 0x74, 0x72, 0x69,
    0x78, 0x2e, 0x6f, 0x72, 0x67,
).map { it.toByte() }.toByteArray()
