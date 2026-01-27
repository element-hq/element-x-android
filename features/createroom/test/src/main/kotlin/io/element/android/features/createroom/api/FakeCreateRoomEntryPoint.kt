/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.tests.testutils.lambda.lambdaError

class FakeCreateRoomEntryPoint : CreateRoomEntryPoint {
    class Builder : CreateRoomEntryPoint.Builder {
        override fun setIsSpace(isSpace: Boolean): Builder = this
        override fun setParentSpace(parentSpaceId: RoomId): Builder = this
        override fun build(): Node = lambdaError()
    }

    override fun builder(
        parentNode: Node,
        buildContext: BuildContext,
        callback: CreateRoomEntryPoint.Callback,
    ): Builder = lambdaError()
}
