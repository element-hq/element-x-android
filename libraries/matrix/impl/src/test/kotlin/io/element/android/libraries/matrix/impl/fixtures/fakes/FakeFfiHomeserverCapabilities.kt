/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.tests.testutils.lambda.lambdaError
import org.matrix.rustcomponents.sdk.ExtendedProfileFields
import org.matrix.rustcomponents.sdk.HomeserverCapabilities
import org.matrix.rustcomponents.sdk.NoHandle

class FakeFfiHomeserverCapabilities(
    private val refresh: () -> Unit = { lambdaError() },
    private val canChangeDisplayName: () -> Boolean = { lambdaError() },
    private val canChangeAvatar: () -> Boolean = { lambdaError() },
    private val canChangePassword: () -> Boolean = { lambdaError() },
    private val canChangeThirdPartyIds: () -> Boolean = { lambdaError() },
    private val canGetLoginToken: () -> Boolean = { lambdaError() },
    private val forgetsRoomWhenLeaving: () -> Boolean = { lambdaError() },
    private val extendedProfileFields: () -> ExtendedProfileFields = { lambdaError() },
) : HomeserverCapabilities(NoHandle) {
    override suspend fun refresh() = refresh.invoke()
    override suspend fun canChangeDisplayname(): Boolean = canChangeDisplayName.invoke()
    override suspend fun canChangeAvatar(): Boolean = canChangeAvatar.invoke()
    override suspend fun canChangePassword(): Boolean = canChangePassword.invoke()
    override suspend fun canChangeThirdpartyIds(): Boolean = canChangeThirdPartyIds.invoke()
    override suspend fun canGetLoginToken(): Boolean = canGetLoginToken.invoke()
    override suspend fun forgetsRoomWhenLeaving(): Boolean = forgetsRoomWhenLeaving.invoke()
    override suspend fun extendedProfileFields(): ExtendedProfileFields = extendedProfileFields.invoke()
}
