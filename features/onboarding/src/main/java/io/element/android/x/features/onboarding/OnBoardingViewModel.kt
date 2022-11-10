package io.element.android.x.features.onboarding

import com.airbnb.mvrx.MavericksViewModel

class OnBoardingViewModel(initialState: OnBoardingViewState) :
    MavericksViewModel<OnBoardingViewState>(initialState) {

    fun onPageChanged(page: Int) {
        setState {
            copy(
                currentPage = page,
            )
        }
    }
}