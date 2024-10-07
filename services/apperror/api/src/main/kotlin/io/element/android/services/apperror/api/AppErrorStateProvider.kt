/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.apperror.api

fun aAppErrorState() = AppErrorState.Error(
    title = "An error occurred",
    body = "Something went wrong, and the details of that would go here.",
    dismiss = {},
)
