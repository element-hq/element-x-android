/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.pusher

data class SetHttpPusherData(
    val pushKey: String,
    val appId: String,
    val url: String,
    val appDisplayName: String,
    val deviceDisplayName: String,
    val profileTag: String?,
    val lang: String,
    val defaultPayload: String,
)
