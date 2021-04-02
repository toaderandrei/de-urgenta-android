package ro.code4.deurgenta.ui.address

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.here.sdk.mapview.*
import org.koin.android.viewmodel.ext.android.viewModel
import ro.code4.deurgenta.R
import ro.code4.deurgenta.data.model.AddressType
import ro.code4.deurgenta.databinding.FragmentConfigureAddressBinding
import ro.code4.deurgenta.helper.*
import ro.code4.deurgenta.interfaces.ViewCallback
import ro.code4.deurgenta.ui.base.ViewModelFragment
import java.util.*

@SuppressLint("LongLogTag")
class ConfigureAddressFragment : ViewModelFragment<ConfigureAddressViewModel>(),
    PermissionManager.PermissionListener {

    override val layout: Int
        get() = R.layout.fragment_configure_address

    override val screenName: Int
        get() = R.string.configure_addresses

    override val viewModel: ConfigureAddressViewModel by viewModel()
    private lateinit var mapView: MapView
    private lateinit var permissionsUtils: PermissionManager

    private lateinit var mapViewUtils: MapViewUtils
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var viewBinding: FragmentConfigureAddressBinding

    var mapAddressType: AddressType = AddressType.HOME

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
        viewBinding.appbarSearch.toolbarSearch.setOnClickListener {
            findNavController().navigate(R.id.back_to_configure_profile)
        }

        viewModel.saveResult().observe(viewLifecycleOwner, { result ->
            result.handle(onSuccess = {
                val direction =
                    ConfigureAddressFragmentDirections.actionNavigateSaveAddress(mapAddress = it!!)
                findNavController().navigate(direction)
            })
        })

        arguments?.let { bundle ->
            bundle.getParcelable<AddressType>("mapAddressType")?.let { it ->
                mapAddressType = it
            }
        }

        arguments?.let { bundle ->
            bundle.getInt("titleResourceId").let { resId ->
                viewBinding.toolbarTitle = getString(resId)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initMapView(savedInstanceState)
        initLocationServicesAndMapUtils()
        handleAndroidPermissions()
    }

    private fun initMapView(savedInstanceState: Bundle?) {
        mapView = viewBinding.mapViewLayout.mapView
        mapView.onCreate(savedInstanceState)
    }

    private fun handleAndroidPermissions() {
        permissionsUtils =
            PermissionManager(requireActivity(), this)

        permissionsUtils.requestLocationRelatedPermissions(this)
    }

    private fun initSearchView() {
        viewBinding.appbarSearch.querySearch.apply {
            setOnQueryTextListener(onQueryTextListener(this@apply))
        }
    }

    private fun initMapButtonCallbacks() {
        viewBinding.saveAddressCallback = object : ViewCallback {
            override fun call() {
                Log.d(TAG, "save address")
                findNavController().navigate(R.id.save_address)
            }
        }

        viewBinding.locateMeCallback = object : ViewCallback {
            override fun call() {
                mapViewUtils.loadLastKnownLocation(true)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PermissionManager.PERMISSION_REQUEST && grantResults.isNotEmpty()) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED } && grantResults.size == permissions.size) {
                onPermissionsGranted()
            } else {
                onPermissionDenied()
            }
        }
    }

    private fun initLocationServicesAndMapUtils() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mapViewUtils = MapViewUtils(mapView, fusedLocationClient, mapViewCallback)
    }

    private val mapViewCallback = object : MapViewUtils.MapViewCallback {
        override fun onError(error: String) {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }

        override fun onSuggestionError(error: String) {
            Log.d(TAG, "error loading autosuggestion results:$error")
        }

        override fun onAddMarker() {
            updateSaveButtonVisibility(View.VISIBLE)
        }

        override fun suggest(suggest: String) {
            Log.d(TAG, "autosuggestion text.$suggest")
        }

        override fun onWarn(warnResId: Int) {
            Log.w(TAG, "searching warning." + getString(warnResId))
            Toast.makeText(requireContext(), getString(warnResId), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapViewUtils.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapViewUtils.onPause()
    }

    private fun onQueryTextListener(view: View): SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                hideSoftInput(view)
                mapViewUtils.searchOnMap(query, MapViewUtils.SearchType.STANDARD)
                return true
            }

            override fun onQueryTextChange(queryString: String): Boolean {
                // Hack
                if (queryString.isNullOrEmpty()) {
                    updateSaveButtonVisibility(View.GONE)
                }
                mapViewUtils.searchOnMap(queryString, MapViewUtils.SearchType.AUTOSUGGEST)
                return true
            }
        }
    }

    override fun onPermissionsGranted() {
        Log.d(TAG, "permission granted.")
        initMapButtonCallbacks()
        mapViewUtils.loadLastKnownLocation()
        initSearchView()
    }

    override fun onPermissionDenied() {
        Log.e(TAG, "Permissions denied by user, return back to the configure profile.")
        findNavController().navigate(R.id.configure_account)
    }

    private fun updateSaveButtonVisibility(flag: Int) {
        viewBinding.mapViewLayout.saveAddress.visibility = flag
        viewBinding.mapViewLayout.saveAddress.setOnClickListener {
            mapViewUtils.getCurrentAddress()
                ?.let { mapAddress ->
                    mapAddress.type = mapAddressType
                    viewModel.saveAddress(mapAddress)
                }
        }
    }

    companion object {
        const val TAG: String = "ConfigureAccountFragment"
    }
}