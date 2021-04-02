package ro.code4.deurgenta.helper

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.here.sdk.core.GeoBox
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.LanguageCode
import com.here.sdk.core.Point2D
import com.here.sdk.mapview.*
import com.here.sdk.search.*
import ro.code4.deurgenta.R
import ro.code4.deurgenta.data.model.MapAddress
import java.lang.StringBuilder

class MapViewUtils(
    private val mapView: MapView,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val callback: MapViewCallback
) {

    private val mapMarkerList = mutableMapOf<MapAddress, MapMarker>()
    private val searchEngine: SearchEngine = SearchEngine()

    private fun loadMapScene(location: Location, showMarker: Boolean = false) {
        mapView.mapScene
            .loadScene(MapScheme.NORMAL_DAY) { errorCode ->
                if (errorCode == null) {

                    val distanceInMeters = DEFAULT_DISTANCE_IN_METERS.toDouble()
                    val geoCoordinates = GeoCoordinates(location.latitude, location.longitude)

                    mapView.camera.lookAt(geoCoordinates, distanceInMeters)

                    if (showMarker) {
                        val searchOptions = SearchOptions(LanguageCode.RO_RO, 1)
                        searchEngine.search(geoCoordinates, searchOptions, querySearchCallback)
                    }
                } else {
                    Log.e(TAG, "error loading results:$errorCode")
                    if (errorCode != MapError.OPERATION_IN_PROGRESS) {
                        callback.onError("cannot load map. Error:$errorCode")
                    }
                }
            }
    }

    private fun createAndAddMarker(searchResult: Place) {
        clearMap()

        if (searchResult.geoCoordinates == null) {
            Log.d(TAG, "no coordinates to add")
            return
        }

        val mapMarker = createPoiMapMarker(searchResult.geoCoordinates!!)
        mapView.mapScene.addMapMarker(mapMarker)

        val mapAddress = MapAddress(
            latitude = searchResult.geoCoordinates!!.latitude,
            longitude = searchResult.geoCoordinates!!.longitude,
            fullAddress = searchResult.address.addressText
        )
        mapAddress.streetAddress = getStreetAddress(searchResult.address)

        mapMarkerList[mapAddress] = mapMarker
    }

    private fun getStreetAddress(address: Address): String {
        val builder = StringBuilder()
        builder.append(address.street)
        builder.append(address.postalCode)
        builder.append(COMMA)
        builder.append(address.city)
        return builder.toString()
    }

    private fun getMapViewGeoBox(): GeoBox {
        val mapViewWidthInPixels = mapView.width
        val mapViewHeightInPixels = mapView.height
        val bottomLeftPoint2D = Point2D(0.0, mapViewHeightInPixels.toDouble())
        val topRightPoint2D = Point2D(mapViewWidthInPixels.toDouble(), 0.0)
        val southWestCorner = mapView.viewToGeoCoordinates(bottomLeftPoint2D)
        val northEastCorner = mapView.viewToGeoCoordinates(topRightPoint2D)
        if (southWestCorner == null || northEastCorner == null) {
            throw RuntimeException("GeoBox creation failed, corners are null.")
        }

        // Note: This algorithm assumes an unrotated map view.
        return GeoBox(southWestCorner, northEastCorner)
    }

    fun searchOnMap(
        queryString: String,
        searchType: SearchType = SearchType.STANDARD,
        maxSearchResults: Int = DEFAULT_MAX_ITEMS
    ) {
        clearMap()
        if (queryString.length < 2) {
            return
        }

        val viewportGeoBox: GeoBox = getMapViewGeoBox()
        val query = TextQuery(queryString, viewportGeoBox)

        val searchOptions = SearchOptions(LanguageCode.RO_RO, maxSearchResults)

        if (searchType == SearchType.STANDARD) {
            searchEngine.search(query, searchOptions, querySearchCallback)
        } else {
            searchEngine.suggest(query, searchOptions, autosuggestCallback)
        }
    }

    private val querySearchCallback = SearchCallback { searchError, list ->
        if (searchError != null) {
            callback.onError(searchError.toString())
            return@SearchCallback
        }

        // Add new marker for each search result on map.
        for (searchResult in list!!) {
            createAndAddMarker(searchResult)
        }

        if (list.size == 1) {
            callback.onAddMarker()
        } else if (list.size > 1) {
            callback.onWarn(R.string.more_results_found);
        }
    }

    private var autosuggestCallback = SuggestCallback { searchError, list ->
        if (searchError != null) {
            callback.onSuggestionError(searchError.toString())
            return@SuggestCallback
        }

        // If error is null, list is guaranteed to be not empty.
        for (autosuggestResult in list!!) {
            val place = autosuggestResult.place
            if (place != null) {
                callback.suggest(place.address.addressText)
            }
        }
    }

    private fun clearMap() {
        for (address in mapMarkerList.keys) {
            mapMarkerList[address]?.let {
                mapView.mapScene.removeMapMarker(it)
            }
        }
        mapMarkerList.clear()
    }

    private fun createPoiMapMarker(geoCoordinates: GeoCoordinates): MapMarker {
        val mapImage: MapImage =
            MapImageFactory.fromResource(mapView.context.resources, R.drawable.poi)
        return MapMarker(geoCoordinates, mapImage)
    }

    fun onResume() {
        mapView.onResume()
    }

    fun onPause() {
        mapView.onPause()
    }

    @SuppressLint("MissingPermission")
    fun loadLastKnownLocation(showMarker: Boolean = false) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    loadMapScene(it, showMarker)
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to localize.$it")
            }
    }

    fun getCurrentAddress(): MapAddress? {
        if (mapMarkerList.isEmpty()) {
            return null
        }

        return mapMarkerList.keys.toTypedArray()[0]
    }

    interface MapViewCallback {
        /**
         * Callback when an error occurs loading results or the map.
         */
        fun onError(error: String)

        /**
         * Callback after a marker has been added to the map.
         */
        fun onAddMarker()

        /**
         * Callback for when suggest api is called.
         */
        fun suggest(suggest: String)

        /**
         * Callback for when suggestion api does not return any results.
         */
        fun onSuggestionError(suggestError: String)

        /**
         * Callback for a warning.
         */
        fun onWarn(warn: Int);
    }

    companion object {
        private const val TAG = "MapViewUtils"
        private const val DEFAULT_DISTANCE_IN_METERS = 100 * 10
        private const val DEFAULT_MAX_ITEMS = 30
        private const val COMMA = ", "
    }

    enum class SearchType {
        STANDARD,
        AUTOSUGGEST
    }
}