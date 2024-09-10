/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.architecture.overlay.operation

import com.bumble.appyx.core.navigation.Operation
import com.bumble.appyx.navmodel.backstack.BackStack

interface OverlayOperation<T> : Operation<T, BackStack.State>
