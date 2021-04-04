package ro.code4.deurgenta.ui.address

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.onboarding_configure_addresses.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import ro.code4.deurgenta.R
import ro.code4.deurgenta.databinding.OnboardingConfigureAddressesBinding
import ro.code4.deurgenta.ui.base.ViewModelFragment

class ConfigureAddressFragment : ViewModelFragment<ConfigureAddressViewModel>() {

    override val layout: Int
        get() = R.layout.onboarding_configure_addresses

    override val screenName: Int
        get() = R.string.configure_addresses

    override val viewModel: ConfigureAddressViewModel by viewModel()

    lateinit var viewBinding: OnboardingConfigureAddressesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = DataBindingUtil.inflate(inflater, layout, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.lifecycleOwner = viewLifecycleOwner
        viewBinding.appbar.toolbar.setOnClickListener {
            Log.d(TAG, "clicked x")
            findNavController().navigate(R.id.back_to_configure_profile)
        }
    }

    companion object {
        const val TAG: String = "ConfigureAccountFragment"
    }
}