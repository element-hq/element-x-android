/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.exception

sealed interface ErrorKind {
    /**
     * M_BAD_ALIAS
     *
     * One or more room aliases within the m.room.canonical_alias event do
     * not point to the room ID for which the state event is to be sent to.
     *
     * room aliases: https://spec.matrix.org/latest/client-server-api/#room-aliases
     */
    data object BadAlias : ErrorKind

    /**
     * M_BAD_JSON
     *
     * The request contained valid JSON, but it was malformed in some way, e.g.
     * missing required keys, invalid values for keys.
     */
    data object BadJson : ErrorKind

    /**
     * M_BAD_STATE
     *
     * The state change requested cannot be performed, such as attempting to
     * unban a user who is not banned.
     */
    data object BadState : ErrorKind

    /**
     * M_BAD_STATUS
     *
     * The application service returned a bad status.
     */
    data class BadStatus(
        /**
         * The HTTP status code of the response.
         */
        val status: Int?,
        /**
         * The body of the response.
         */
        val body: String?
    ) : ErrorKind

    /**
     * M_CANNOT_LEAVE_SERVER_NOTICE_ROOM
     *
     * The user is unable to reject an invite to join the server notices
     * room.
     *
     * server notices: https://spec.matrix.org/latest/client-server-api/#server-notices
     */
    data object CannotLeaveServerNoticeRoom : ErrorKind

    /**
     * M_CANNOT_OVERWRITE_MEDIA
     *
     * The create_content_async endpoint was called with a media ID that
     * already has content.
     *
     */
    data object CannotOverwriteMedia : ErrorKind

    /**
     * M_CAPTCHA_INVALID
     *
     * The Captcha provided did not match what was expected.
     */
    data object CaptchaInvalid : ErrorKind

    /**
     * M_CAPTCHA_NEEDED
     *
     * A Captcha is required to complete the request.
     */
    data object CaptchaNeeded : ErrorKind

    /**
     * M_CONNECTION_FAILED
     *
     * The connection to the application service failed.
     */
    data object ConnectionFailed : ErrorKind

    /**
     * M_CONNECTION_TIMEOUT
     *
     * The connection to the application service timed out.
     */
    data object ConnectionTimeout : ErrorKind

    /**
     * M_DUPLICATE_ANNOTATION
     *
     * The request is an attempt to send a duplicate annotation.
     *
     * duplicate annotation: https://spec.matrix.org/latest/client-server-api/#avoiding-duplicate-annotations
     */
    data object DuplicateAnnotation : ErrorKind

    /**
     * M_EXCLUSIVE
     *
     * The resource being requested is reserved by an application service, or
     * the application service making the request has not created the
     * resource.
     */
    data object Exclusive : ErrorKind

    /**
     * M_FORBIDDEN
     *
     * Forbidden access, e.g. joining a room without permission, failed login.
     */
    data object Forbidden : ErrorKind

    /**
     * M_GUEST_ACCESS_FORBIDDEN
     *
     * The room or resource does not permit guests to access it.
     *
     * guests: https://spec.matrix.org/latest/client-server-api/#guest-access
     */
    data object GuestAccessForbidden : ErrorKind

    /**
     * M_INCOMPATIBLE_ROOM_VERSION
     *
     * The client attempted to join a room that has a version the server does
     * not support.
     */
    data class IncompatibleRoomVersion(
        /**
         * The room's version.
         */
        val roomVersion: String
    ) : ErrorKind

    /**
     * M_INVALID_PARAM
     *
     * A parameter that was specified has the wrong value. For example, the
     * server expected an integer and instead received a string.
     */
    data object InvalidParam : ErrorKind

    /**
     * M_INVALID_ROOM_STATE
     *
     * The initial state implied by the parameters to the create_room
     * request is invalid, e.g. the user's power_level is set below that
     * necessary to set the room name.
     *
     */
    data object InvalidRoomState : ErrorKind

    /**
     * M_INVALID_USERNAME
     *
     * The desired user name is not valid.
     */
    data object InvalidUsername : ErrorKind

    /**
     * M_LIMIT_EXCEEDED
     *
     * The request has been refused due to rate limiting: too many requests
     * have been sent in a short period of time.
     *
     * rate limiting: https://spec.matrix.org/latest/client-server-api/#rate-limiting
     */
    data class LimitExceeded(
        /**
         * How long a client should wait before they can try again.
         */
        val retryAfterMs: Long?
    ) : ErrorKind

    /**
     * M_MISSING_PARAM
     *
     * A required parameter was missing from the request.
     */
    data object MissingParam : ErrorKind

    /**
     * M_MISSING_TOKEN
     *
     * No access token was specified for the request, but one is required.
     *
     * access token: https://spec.matrix.org/latest/client-server-api/#client-authentication
     */
    data object MissingToken : ErrorKind

    /**
     * M_NOT_FOUND
     *
     * No resource was found for this request.
     */
    data object NotFound : ErrorKind

    /**
     * M_NOT_JSON
     *
     * The request did not contain valid JSON.
     */
    data object NotJson : ErrorKind

    /**
     * M_NOT_YET_UPLOADED
     *
     * An mxc URI generated was used and the content is not yet available.
     *
     */
    data object NotYetUploaded : ErrorKind

    /**
     * M_RESOURCE_LIMIT_EXCEEDED
     *
     * The request cannot be completed because the homeserver has reached a
     * resource limit imposed on it. For example, a homeserver held in a
     * shared hosting environment may reach a resource limit if it starts
     * using too much memory or disk space.
     */
    data class ResourceLimitExceeded(
        /**
         * A URI giving a contact method for the server administrator.
         */
        val adminContact: String
    ) : ErrorKind

    /**
     * M_ROOM_IN_USE
     *
     * The room alias specified in the request is already taken.
     *
     * room alias: https://spec.matrix.org/latest/client-server-api/#room-aliases
     */
    data object RoomInUse : ErrorKind

    /**
     * M_SERVER_NOT_TRUSTED
     *
     * The client's request used a third-party server, e.g. identity server,
     * that this server does not trust.
     */
    data object ServerNotTrusted : ErrorKind

    /**
     * M_THREEPID_AUTH_FAILED
     *
     * Authentication could not be performed on the third-party identifier.
     *
     * third-party identifier: https://spec.matrix.org/latest/client-server-api/#adding-account-administrative-contact-information
     */
    data object ThreepidAuthFailed : ErrorKind

    /**
     * M_THREEPID_DENIED
     *
     * The server does not permit this third-party identifier. This may
     * happen if the server only permits, for example, email addresses from
     * a particular domain.
     *
     * third-party identifier: https://spec.matrix.org/latest/client-server-api/#adding-account-administrative-contact-information
     */
    data object ThreepidDenied : ErrorKind

    /**
     * M_THREEPID_IN_USE
     *
     * The third-party identifier is already in use by another user.
     *
     * third-party identifier: https://spec.matrix.org/latest/client-server-api/#adding-account-administrative-contact-information
     */
    data object ThreepidInUse : ErrorKind

    /**
     * M_THREEPID_MEDIUM_NOT_SUPPORTED
     *
     * The homeserver does not support adding a third-party identifier of the
     * given medium.
     *
     * third-party identifier: https://spec.matrix.org/latest/client-server-api/#adding-account-administrative-contact-information
     */
    data object ThreepidMediumNotSupported : ErrorKind

    /**
     * M_THREEPID_NOT_FOUND
     *
     * No account matching the given third-party identifier could be found.
     *
     * third-party identifier: https://spec.matrix.org/latest/client-server-api/#adding-account-administrative-contact-information
     */
    data object ThreepidNotFound : ErrorKind

    /**
     * M_TOO_LARGE
     *
     * The request or entity was too large.
     */
    data object TooLarge : ErrorKind

    /**
     * M_UNABLE_TO_AUTHORISE_JOIN
     *
     * The room is restricted and none of the conditions can be validated by
     * the homeserver. This can happen if the homeserver does not know
     * about any of the rooms listed as conditions, for example.
     *
     * restricted: https://spec.matrix.org/latest/client-server-api/#restricted-rooms
     */
    data object UnableToAuthorizeJoin : ErrorKind

    /**
     * M_UNABLE_TO_GRANT_JOIN
     *
     * A different server should be attempted for the join. This is typically
     * because the resident server can see that the joining user satisfies
     * one or more conditions, such as in the case of restricted rooms,
     * but the resident server would be unable to meet the authorization
     * rules.
     *
     * restricted rooms: https://spec.matrix.org/latest/client-server-api/#restricted-rooms
     */
    data object UnableToGrantJoin : ErrorKind

    /**
     * M_UNAUTHORIZED
     *
     * The request was not correctly authorized. Usually due to login failures.
     */
    data object Unauthorized : ErrorKind

    /**
     * M_UNKNOWN
     *
     * An unknown error has occurred.
     */
    data object Unknown : ErrorKind

    /**
     * M_UNKNOWN_TOKEN
     *
     * The access or refresh token specified was not recognized.
     *
     * access or refresh token: https://spec.matrix.org/latest/client-server-api/#client-authentication
     */
    data class UnknownToken(
        /**
         * If this is true, the client is in a "soft logout" state, i.e.
         * the server requires re-authentication but the session is not
         * invalidated. The client can acquire a new access token by
         * specifying the device ID it is already using to the login API.
         *
         * soft logout: https://spec.matrix.org/latest/client-server-api/#soft-logout
         */
        val softLogout: Boolean
    ) : ErrorKind

    /**
     * M_UNRECOGNIZED
     *
     * The server did not understand the request.
     *
     * This is expected to be returned with a 404 HTTP status code if the
     * endpoint is not implemented or a 405 HTTP status code if the
     * endpoint is implemented, but the incorrect HTTP method is used.
     */
    data object Unrecognized : ErrorKind

    /**
     * M_UNSUPPORTED_ROOM_VERSION
     *
     * The request to create_room used a room version that the server does
     * not support.
     *
     */
    data object UnsupportedRoomVersion : ErrorKind

    /**
     * M_URL_NOT_SET
     *
     * The application service doesn't have a URL configured.
     */
    data object UrlNotSet : ErrorKind

    /**
     * M_USER_DEACTIVATED
     *
     * The user ID associated with the request has been deactivated.
     */
    data object UserDeactivated : ErrorKind

    /**
     * M_USER_IN_USE
     *
     * The desired user ID is already taken.
     */
    data object UserInUse : ErrorKind

    /**
     * M_USER_LOCKED
     *
     * The account has been locked and cannot be used at this time.
     *
     * locked: https://spec.matrix.org/latest/client-server-api/#account-locking
     */
    data object UserLocked : ErrorKind

    /**
     * M_USER_SUSPENDED
     *
     * The account has been suspended and can only be used for limited
     * actions at this time.
     *
     * suspended: https://spec.matrix.org/latest/client-server-api/#account-suspension
     */
    data object UserSuspended : ErrorKind

    /**
     * M_WEAK_PASSWORD
     *
     * The password was rejected by the server for being too weak.
     *
     * rejected: https://spec.matrix.org/latest/client-server-api/#notes-on-password-management
     */
    data object WeakPassword : ErrorKind

    /**
     * M_WRONG_ROOM_KEYS_VERSION
     *
     * The version of the room keys backup provided in the request does not
     * match the current backup version.
     *
     * room keys backup: https://spec.matrix.org/latest/client-server-api/#server-side-key-backups
     */
    data class WrongRoomKeysVersion(
        /**
         * The currently active backup version.
         */
        val currentVersion: String?
    ) : ErrorKind

    /**
     * A custom API error.
     */
    data class Custom(val errcode: String) : ErrorKind
}
