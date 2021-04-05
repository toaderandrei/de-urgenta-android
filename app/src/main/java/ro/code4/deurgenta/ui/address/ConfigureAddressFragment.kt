package ro.code4.deurgenta.ui.address

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapview.*
import kotlinx.android.synthetic.main.onboarding_configure_addresses.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import ro.code4.deurgenta.R
import ro.code4.deurgenta.databinding.OnboardingConfigureAddressesBinding
import ro.code4.deurgenta.helper.PermissionUtils
import ro.code4.deurgenta.interfaces.LocateMeCallback
import ro.code4.deurgenta.interfaces.SaveProgressCallback
import ro.code4.deurgenta.ui.base.ViewModelFragment

@SuppressLint("LongLogTag")
class ConfigureAddressFragment : ViewModelFragment<ConfigureAddressViewModel>() {

    override val layout: Int
        get() = R.layout.onboarding_configure_addresses

    override val screenName: Int
        get() = R.string.configure_addresses


    override val viewModel: ConfigureAddressViewModel by viewModel()
    private lateinit var mapView: MapView

    private var permissionsRequestor: PermissionUtils? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
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
            PermissionUtils(requireActivity())

        permissionsRequestor!!.request(object : PermissionUtils.ResultListener {
            override fun permissionsGranted() {
                Log.d(TAG, "permission granted.")
                initLocationServices()
                initMapData()
                loadLastKnownLocation(false)
            }

            override fun permissionsDenied() {
                Log.e(TAG, "Permissions denied by user.")
            }
        })
    }

    fun initLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

    }

    private fun initMapData() {
        viewBinding.saveCallback = object : SaveProgressCallback {
            override fun save() {
                Log.d(TAG, "save address")
            }
        }

        viewBinding.locateMeCallback = object : LocateMeCallback {
            override fun locateMe() {
                loadLastKnownLocation(true)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadLastKnownLocation(showLocation: Boolean) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    loadMapScene(it, showLocation)
                }
            }
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

    private fun loadMapScene(location: Location, showLocation: Boolean) {
        // Load a scene from the SDK to render the map with a map style.
        mapView.mapScene
            .loadScene(MapScheme.NORMAL_DAY) { errorCode ->
                if (errorCode == null) {
                    val distanceInMeters = (100 * 10).toDouble()
                    val geoCoordinates = GeoCoordinates(location.latitude, location.longitude)
                    mapView.camera.lookAt(
                        geoCoordinates, distanceInMeters
                    )
                    if (showLocation) {
                        mapView.mapScene.addMapMarker(createPoiMapMarker(geoCoordinates))
                        viewBinding.btSaveAddress.visibility = View.VISIBLE
                    }
                } else {
                    Log.d(TAG, "onLoadScene failed: $errorCode")
                }
            }
    }

    private fun createPoiMapMarker(geoCoordinates: GeoCoordinates): MapMarker {
        val mapImage: MapImage =
            MapImageFactory.fromResource(requireContext().resources, R.drawable.poi)
        return MapMarker(geoCoordinates, mapImage)
    }

    companion object {
        const val TAG: String = "ConfigureAccountFragment"
    }
}