/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions.permissions

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.RoomScope
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
class ChangeRoomPermissionsNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ChangeRoomPermissionsPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    @Parcelize
    data class Inputs(
        val section: ChangeRoomPermissionsSection,
    ) : NodeInputs, Parcelable

    private val inputs: Inputs = inputs()

    private val presenter = presenterFactory.run {
        create(inputs.section)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        ChangeRoomPermissionsView(
            modifier = modifier,
            state = state,
            onBackPressed = this::navigateUp,
        )
    }
}

@Parcelize
enum class ChangeRoomPermissionsSection : Parcelable {
    RoomDetails,
    MessagesAndContent,
    MembershipModeration,
}
