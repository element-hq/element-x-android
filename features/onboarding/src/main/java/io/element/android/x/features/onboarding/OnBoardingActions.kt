package io.element.android.x.features.onboarding

sealed interface OnBoardingActions {
    data class GoToPage(val page: Int) : OnBoardingActions
}
