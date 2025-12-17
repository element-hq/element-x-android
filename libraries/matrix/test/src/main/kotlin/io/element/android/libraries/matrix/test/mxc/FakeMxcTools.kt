/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.mxc

import io.element.android.libraries.matrix.api.mxc.MxcTools
import io.element.android.libraries.matrix.impl.mxc.DefaultMxcTools

class FakeMxcTools(
    private val delegate: MxcTools = DefaultMxcTools()
) : MxcTools by delegate
