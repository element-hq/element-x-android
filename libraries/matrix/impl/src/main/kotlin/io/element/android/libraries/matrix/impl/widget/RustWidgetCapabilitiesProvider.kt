/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.widget

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.widget.MatrixWidgetCapabilitiesPolicy
import io.element.android.libraries.matrix.api.widget.MatrixWidgetPermission
import io.element.android.libraries.matrix.api.widget.MatrixWidgetPermission.Kind
import org.matrix.rustcomponents.sdk.WidgetCapabilities
import org.matrix.rustcomponents.sdk.WidgetCapabilitiesProvider
import org.matrix.rustcomponents.sdk.WidgetEventFilter
import org.matrix.rustcomponents.sdk.getElementCallRequiredPermissions

internal class RustWidgetCapabilitiesProvider(
    private val policy: MatrixWidgetCapabilitiesPolicy,
    private val userId: String,
    private val deviceId: String,
) : WidgetCapabilitiesProvider {
    override suspend fun acquireCapabilities(capabilities: WidgetCapabilities): WidgetCapabilities {
        return when (val currentPolicy = policy) {
            MatrixWidgetCapabilitiesPolicy.ElementCall -> getElementCallRequiredPermissions(userId, deviceId)
            MatrixWidgetCapabilitiesPolicy.DenyAll -> deniedWidgetCapabilities()
            is MatrixWidgetCapabilitiesPolicy.AskUser -> {
                // Copy the lists before prompting so approval and the granted filters describe the same request.
                val requested = capabilities.copy(read = capabilities.read.toList(), send = capabilities.send.toList())
                val permissions = requested.permissions()
                // requiresClient only describes a transport requirement; it discloses no data and grants no action.
                val approved = permissions.isEmpty() || runCatchingExceptions { currentPolicy.approve(permissions) }.getOrDefault(false)
                if (approved) requested else deniedWidgetCapabilities()
            }
        }
    }
}

private fun deniedWidgetCapabilities() = WidgetCapabilities(
    read = emptyList(),
    send = emptyList(),
    requiresClient = false,
    updateDelayedEvent = false,
    sendDelayedEvent = false,
    downloadFiles = false,
    rtcTransports = false,
    rtcLivekitGetToken = false,
    rtcLivekitDelegateDelayedLeave = false,
)

internal fun WidgetCapabilities.permissions(): List<MatrixWidgetPermission> = buildList {
    addAll(read.map { it.permission(isSending = false) })
    addAll(send.map { it.permission(isSending = true) })
    if (sendDelayedEvent) add(MatrixWidgetPermission(Kind.SendDelayedEvents))
    if (updateDelayedEvent) add(MatrixWidgetPermission(Kind.UpdateDelayedEvents))
    if (downloadFiles) add(MatrixWidgetPermission(Kind.DownloadFiles))
    if (rtcTransports) add(MatrixWidgetPermission(Kind.DiscoverRtcTransports))
    if (rtcLivekitGetToken) add(MatrixWidgetPermission(Kind.ObtainLivekitToken))
    if (rtcLivekitDelegateDelayedLeave) add(MatrixWidgetPermission(Kind.DelegateLivekitLeave))
}

private fun WidgetEventFilter.permission(isSending: Boolean): MatrixWidgetPermission = when (this) {
    is WidgetEventFilter.MessageLikeWithType -> MatrixWidgetPermission(
        kind = if (isSending) Kind.SendMessages else Kind.ReadMessages,
        eventType = eventType,
    )
    is WidgetEventFilter.RoomMessageWithMsgtype -> MatrixWidgetPermission(
        kind = if (isSending) Kind.SendMessages else Kind.ReadMessages,
        eventType = "m.room.message",
        messageType = msgtype,
    )
    is WidgetEventFilter.StateWithType -> MatrixWidgetPermission(
        kind = if (isSending) Kind.SendState else Kind.ReadState,
        eventType = eventType,
    )
    is WidgetEventFilter.StateWithTypeAndStateKey -> MatrixWidgetPermission(
        kind = if (isSending) Kind.SendState else Kind.ReadState,
        eventType = eventType,
        stateKey = stateKey,
    )
    is WidgetEventFilter.ToDevice -> MatrixWidgetPermission(
        kind = if (isSending) Kind.SendToDevice else Kind.ReadToDevice,
        eventType = eventType,
    )
}
