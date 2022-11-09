package io.element.android.x.features.onboarding

import com.airbnb.mvrx.MavericksState

data class OnBoardingViewState(
    val currentPage: Int = 0,
) : MavericksState
