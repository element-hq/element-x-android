/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.api

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.user.MatrixUser

data class ConfirmingStartDmWithMatrixUser(
    val matrixUser: MatrixUser,
) : AsyncAction.Confirming
