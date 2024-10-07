/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.architecture

import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins

interface NodeInputs : Plugin

inline fun <reified I : NodeInputs> Node.inputs(): I {
    return requireNotNull(plugins<I>().firstOrNull()) { "Make sure to actually pass NodeInputs plugin to your node" }
}
