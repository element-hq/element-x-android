/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.exception
import io.element.android.libraries.matrix.api.exception.ErrorKind
import org.matrix.rustcomponents.sdk.ErrorKind as RustErrorKind

fun RustErrorKind.map(): ErrorKind {
    return when (this) {
        RustErrorKind.BadAlias -> ErrorKind.BadAlias
        RustErrorKind.BadJson -> ErrorKind.BadJson
        RustErrorKind.BadState -> ErrorKind.BadState
        is RustErrorKind.BadStatus -> ErrorKind.BadStatus(status?.toInt(), body)
        RustErrorKind.CannotLeaveServerNoticeRoom -> ErrorKind.CannotLeaveServerNoticeRoom
        RustErrorKind.CannotOverwriteMedia -> ErrorKind.CannotOverwriteMedia
        RustErrorKind.CaptchaInvalid -> ErrorKind.CaptchaInvalid
        RustErrorKind.CaptchaNeeded -> ErrorKind.CaptchaNeeded
        RustErrorKind.ConnectionFailed -> ErrorKind.ConnectionFailed
        RustErrorKind.ConnectionTimeout -> ErrorKind.ConnectionTimeout
        is RustErrorKind.Custom -> ErrorKind.Custom(errcode)
        RustErrorKind.DuplicateAnnotation -> ErrorKind.DuplicateAnnotation
        RustErrorKind.Exclusive -> ErrorKind.Exclusive
        RustErrorKind.Forbidden -> ErrorKind.Forbidden
        RustErrorKind.GuestAccessForbidden -> ErrorKind.GuestAccessForbidden
        is RustErrorKind.IncompatibleRoomVersion -> ErrorKind.IncompatibleRoomVersion(roomVersion)
        RustErrorKind.InvalidParam -> ErrorKind.InvalidParam
        RustErrorKind.InvalidRoomState -> ErrorKind.InvalidRoomState
        RustErrorKind.InvalidUsername -> ErrorKind.InvalidUsername
        is RustErrorKind.LimitExceeded -> ErrorKind.LimitExceeded(retryAfterMs?.toLong())
        RustErrorKind.MissingParam -> ErrorKind.MissingParam
        RustErrorKind.MissingToken -> ErrorKind.MissingToken
        RustErrorKind.NotFound -> ErrorKind.NotFound
        RustErrorKind.NotJson -> ErrorKind.NotJson
        RustErrorKind.NotYetUploaded -> ErrorKind.NotYetUploaded
        is RustErrorKind.ResourceLimitExceeded -> ErrorKind.ResourceLimitExceeded(adminContact)
        RustErrorKind.RoomInUse -> ErrorKind.RoomInUse
        RustErrorKind.ServerNotTrusted -> ErrorKind.ServerNotTrusted
        RustErrorKind.ThreepidAuthFailed -> ErrorKind.ThreepidAuthFailed
        RustErrorKind.ThreepidDenied -> ErrorKind.ThreepidDenied
        RustErrorKind.ThreepidInUse -> ErrorKind.ThreepidInUse
        RustErrorKind.ThreepidMediumNotSupported -> ErrorKind.ThreepidMediumNotSupported
        RustErrorKind.ThreepidNotFound -> ErrorKind.ThreepidNotFound
        RustErrorKind.TooLarge -> ErrorKind.TooLarge
        RustErrorKind.UnableToAuthorizeJoin -> ErrorKind.UnableToAuthorizeJoin
        RustErrorKind.UnableToGrantJoin -> ErrorKind.UnableToGrantJoin
        RustErrorKind.Unauthorized -> ErrorKind.Unauthorized
        RustErrorKind.Unknown -> ErrorKind.Unknown
        is RustErrorKind.UnknownToken -> ErrorKind.UnknownToken(softLogout)
        RustErrorKind.Unrecognized -> ErrorKind.Unrecognized
        RustErrorKind.UnsupportedRoomVersion -> ErrorKind.UnsupportedRoomVersion
        RustErrorKind.UrlNotSet -> ErrorKind.UrlNotSet
        RustErrorKind.UserDeactivated -> ErrorKind.UserDeactivated
        RustErrorKind.UserInUse -> ErrorKind.UserInUse
        RustErrorKind.UserLocked -> ErrorKind.UserLocked
        RustErrorKind.UserSuspended -> ErrorKind.UserSuspended
        RustErrorKind.WeakPassword -> ErrorKind.WeakPassword
        is RustErrorKind.WrongRoomKeysVersion -> ErrorKind.WrongRoomKeysVersion(currentVersion)
    }
}
