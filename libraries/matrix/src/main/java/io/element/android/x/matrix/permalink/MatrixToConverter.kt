package io.element.android.x.matrix.permalink

import android.net.Uri

/**
 * Mapping of an input URI to a matrix.to compliant URI.
 */
object MatrixToConverter {

    const val MATRIX_TO_URL_BASE = "https://matrix.to/#/"

    /**
     * Try to convert a URL from an element web instance or from a client permalink to a matrix.to url.
     * To be successfully converted, URL path should contain one of the [SUPPORTED_PATHS].
     * Examples:
     * - https://riot.im/develop/#/room/#element-android:matrix.org  ->  https://matrix.to/#/#element-android:matrix.org
     * - https://app.element.io/#/room/#element-android:matrix.org   ->  https://matrix.to/#/#element-android:matrix.org
     * - https://www.example.org/#/room/#element-android:matrix.org  ->  https://matrix.to/#/#element-android:matrix.org
     */
    fun convert(uri: Uri): Uri? {
        val uriString = uri.toString()

        return when {
            // URL is already a matrix.to
            uriString.startsWith(MATRIX_TO_URL_BASE) -> uri
            // Web or client url
            SUPPORTED_PATHS.any { it in uriString } -> {
                val path = SUPPORTED_PATHS.first { it in uriString }
                Uri.parse(MATRIX_TO_URL_BASE + uriString.substringAfter(path))
            }
            // URL is not supported
            else -> null
        }
    }

    private val SUPPORTED_PATHS = listOf(
        "/#/room/",
        "/#/user/",
        "/#/group/"
    )
}
