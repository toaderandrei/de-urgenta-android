package ro.code4.deurgenta.ui.onboarding

import android.content.Intent
import org.koin.android.viewmodel.ext.android.viewModel
import ro.code4.deurgenta.R
import ro.code4.deurgenta.ui.base.BaseAnalyticsActivity

class OnboardingDetailsActivity : BaseAnalyticsActivity<OnboardingDetailsViewModel>() {
    override val layout: Int
        get() = R.layout.details_activity_onboarding
    override val screenName: Int
        get() = R.string.analytics_title_onboarding

    override val viewModel: OnboardingDetailsViewModel by viewModel()


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

    }
}