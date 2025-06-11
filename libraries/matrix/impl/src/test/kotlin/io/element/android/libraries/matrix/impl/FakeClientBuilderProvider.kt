/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClientBuilder
import org.matrix.rustcomponents.sdk.ClientBuilder

class FakeClientBuilderProvider : ClientBuilderProvider {
    override fun provide(): ClientBuilder {
        return FakeFfiClientBuilder()
    }
}
