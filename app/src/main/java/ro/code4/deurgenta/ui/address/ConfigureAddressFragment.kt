package ro.code4.deurgenta.ui.address

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView
import kotlinx.android.synthetic.main.onboarding_configure_addresses.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import ro.code4.deurgenta.R
import ro.code4.deurgenta.databinding.OnboardingConfigureAddressesBinding
import ro.code4.deurgenta.helper.PermissionsRequestor
import ro.code4.deurgenta.ui.base.ViewModelFragment

@SuppressLint("LongLogTag")
class ConfigureAddressFragment : ViewModelFragment<ConfigureAddressViewModel>() {

    override val layout: Int
        get() = R.layout.onboarding_configure_addresses

    override val screenName: Int
        get() = R.string.configure_addresses

    override val viewModel: ConfigureAddressViewModel by viewModel()
    private lateinit var mapView: MapView

    private var permissionsRequestor: PermissionsRequestor? = null

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
            Log.d(TAG, "clicked close.")
            findNavController().navigate(R.id.back_to_configure_profile)
        }
        // Get a MapView instance from layout.
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadMap(savedInstanceState)
        handleAndroidPermissions()
    }

    private fun handleAndroidPermissions() {
        permissionsRequestor =
            PermissionsRequestor(activity)

        permissionsRequestor!!.request(object : PermissionsRequestor.ResultListener {
            override fun permissionsGranted() {
                loadMapScene()
            }

            override fun permissionsDenied() {
                Log.e(TAG, "Permissions denied by user.")
            }

        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionsRequestor!!.onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun loadMap(savedInstanceState: Bundle?) {
        // Get a MapView instance from layout.
        mapView = activity?.findViewById(R.id.map_view)!!
        mapView.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    private fun loadMapScene() {
        // Load a scene from the SDK to render the map with a map style.
        mapView.mapScene
            .loadScene(MapScheme.NORMAL_DAY) { errorCode ->
                if (errorCode == null) {
                    val distanceInMeters = (1000 * 10).toDouble()
                    mapView.camera.lookAt(
                        GeoCoordinates(52.530932, 13.384915), distanceInMeters
                    )
                } else {
                    Log.d(TAG, "onLoadScene failed: $errorCode")
                }
            }
    }

    companion object {
        const val TAG: String = "ConfigureAccountFragment"
    }
}