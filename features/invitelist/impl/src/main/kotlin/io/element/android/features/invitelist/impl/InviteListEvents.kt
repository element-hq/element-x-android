/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.invitelist.impl

import io.element.android.features.invitelist.impl.model.InviteListInviteSummary

sealed interface InviteListEvents {
    data class AcceptInvite(val invite: InviteListInviteSummary) : InviteListEvents
    data class DeclineInvite(val invite: InviteListInviteSummary) : InviteListEvents

    data object ConfirmDeclineInvite : InviteListEvents
    data object CancelDeclineInvite : InviteListEvents

    data object DismissAcceptError : InviteListEvents
    data object DismissDeclineError : InviteListEvents
}
