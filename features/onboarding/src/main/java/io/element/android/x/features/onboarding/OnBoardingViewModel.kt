package io.element.android.x.features.onboarding

import com.airbnb.mvrx.MavericksViewModel

class OnBoardingViewModel(initialState: OnBoardingViewState) :
    MavericksViewModel<OnBoardingViewState>(initialState) {

    fun handle(action: OnBoardingActions) {
        when (action) {
            is OnBoardingActions.GoToPage -> handleGoToPage(action)
        }
    }

    private fun handleGoToPage(action: OnBoardingActions.GoToPage) {
        setState {
            copy(currentPage = action.page)
        }
    }
}