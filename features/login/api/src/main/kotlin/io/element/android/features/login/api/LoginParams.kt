/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Parameters to start the login flow, when the application is opened
 * from a mobile.element.io link.
 */
@Parcelize
data class LoginParams(
    val accountProvider: String,
    val loginHint: String?
) : Parcelable
