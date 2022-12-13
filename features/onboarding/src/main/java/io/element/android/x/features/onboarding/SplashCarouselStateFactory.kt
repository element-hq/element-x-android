package io.element.android.x.features.onboarding

import androidx.annotation.DrawableRes
import io.element.android.x.element.resources.R as ElementR

class SplashCarouselStateFactory {
    fun create(): SplashCarouselState {
        val lightTheme = true
        fun background(@DrawableRes lightDrawable: Int) =
            if (lightTheme) lightDrawable else R.drawable.bg_color_background

        fun hero(@DrawableRes lightDrawable: Int, @DrawableRes darkDrawable: Int) =
            if (lightTheme) lightDrawable else darkDrawable
        return SplashCarouselState(
            listOf(
                SplashCarouselState.Item(
                    ElementR.string.ftue_auth_carousel_secure_title,
                    ElementR.string.ftue_auth_carousel_secure_body,
                    hero(
                        R.drawable.ic_splash_conversations,
                        R.drawable.ic_splash_conversations_dark
                    ),
                    background(R.drawable.bg_carousel_page_1)
                ),
                SplashCarouselState.Item(
                    ElementR.string.ftue_auth_carousel_control_title,
                    ElementR.string.ftue_auth_carousel_control_body,
                    hero(R.drawable.ic_splash_control, R.drawable.ic_splash_control_dark),
                    background(R.drawable.bg_carousel_page_2)
                ),
                SplashCarouselState.Item(
                    ElementR.string.ftue_auth_carousel_encrypted_title,
                    ElementR.string.ftue_auth_carousel_encrypted_body,
                    hero(R.drawable.ic_splash_secure, R.drawable.ic_splash_secure_dark),
                    background(R.drawable.bg_carousel_page_3)
                ),
                SplashCarouselState.Item(
                    collaborationTitle(),
                    ElementR.string.ftue_auth_carousel_workplace_body,
                    hero(
                        R.drawable.ic_splash_collaboration,
                        R.drawable.ic_splash_collaboration_dark
                    ),
                    background(R.drawable.bg_carousel_page_4)
                )
            )
        )
    }

    private fun collaborationTitle(): Int {
        return when {
            true -> R.string.cut_the_slack_from_teams
            else -> ElementR.string.ftue_auth_carousel_workplace_title
        }
    }
}
