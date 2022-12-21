package io.element.android.x.matrix.session

import io.element.android.x.matrix.core.SessionId
import org.matrix.rustcomponents.sdk.Session

fun Session.sessionId() = SessionId("${userId}_${deviceId}")
