/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.test

import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig

fun aCurrentUserPushConfig(
    url: String = "aUrl",
    pushKey: String = "aPushKey",
) = CurrentUserPushConfig(
    url = url,
    pushKey = pushKey,
)
