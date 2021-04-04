package ro.code4.deurgenta.ui.onboarding

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.activity_onboarding.*
import org.koin.android.viewmodel.ext.android.viewModel
import ro.code4.deurgenta.R
import ro.code4.deurgenta.ui.base.BaseAnalyticsActivity

class OnboardingActivity : BaseAnalyticsActivity<OnboardingViewModel>() {

    override val layout: Int
        get() = R.layout.activity_onboarding
    override val screenName: Int
        get() = R.string.analytics_title_onboarding

    override val viewModel: OnboardingViewModel by viewModel()

    private var navHostFragment: NavHostFragment? = null

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    companion object {
        private const val TAG = "OnboardingActivity"
    }
}