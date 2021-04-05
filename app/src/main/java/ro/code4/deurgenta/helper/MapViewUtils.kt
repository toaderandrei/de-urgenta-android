package ro.code4.deurgenta.helper

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.here.sdk.core.*
import com.here.sdk.mapview.*
import com.here.sdk.search.*
import ro.code4.deurgenta.R

class MapViewUtils(
    private val mapView: MapView,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val callback: MapViewCallback
) {

    private val mapMarkerList = mutableListOf<MapMarker>()
    private val searchEngine: SearchEngine = SearchEngine()
    private lateinit var lastGeoCoordinates: GeoCoordinates

    private fun loadMapScene(location: Location, showLocation: Boolean) {
        mapView.mapScene
            .loadScene(MapScheme.NORMAL_DAY) { errorCode ->
                if (errorCode == null) {

                    val distanceInMeters = (100 * 10).toDouble()
                    val geoCoordinates = GeoCoordinates(location.latitude, location.longitude)

                    mapView.camera.lookAt(geoCoordinates, distanceInMeters)
                    lastGeoCoordinates = geoCoordinates

                    if (showLocation) {
                        createAndAddMarker(geoCoordinates)
                    }
                } else {
                    callback.onError("cannot load map")
                }
            }
    }

    private fun createAndAddMarker(geoCoordinates: GeoCoordinates) {
        val mapMarker = createPoiMapMarker(geoCoordinates)
        mapView.mapScene.addMapMarker(mapMarker)
        mapMarkerList.add(mapMarker)
        callback.onAddMarker()
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

    fun searchOnMap(queryString: String) {
        clearMap()
        if (queryString.length < 2) {
            return
        }

        val viewportGeoBox: GeoBox = getMapViewGeoBox()
        val query = TextQuery(queryString, viewportGeoBox)

        val maxItems = 100
        val searchOptions = SearchOptions(LanguageCode.EN_GB, maxItems)
        searchEngine.search(query, searchOptions, querySearchCallback)
    }

    fun searchOnMapAutoSuggestion(queryString: String) {
        clearMap()
        if (queryString.length < 2) {
            return
        }

        val viewportGeoBox: GeoBox = getMapViewGeoBox()
        val query = TextQuery(queryString, viewportGeoBox)

        val maxItems = 100
        val searchOptions = SearchOptions(LanguageCode.EN_GB, maxItems)

        searchEngine.suggest(query, searchOptions, autosuggestCallback)
    }


    private val querySearchCallback = SearchCallback { searchError, list ->
        if (searchError != null) {
            callback.onError(searchError.toString())
            return@SearchCallback
        }

        // Add new marker for each search result on map.
        for (searchResult in list!!) {
            createAndAddMarker(searchResult.geoCoordinates!!)
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
        for (mapMarker in mapMarkerList) {
            mapView.mapScene.removeMapMarker(mapMarker)
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
    fun loadLastKnownLocation(showLocation: Boolean) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    loadMapScene(it, showLocation)
                }
            }
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
        fun onSuggestionError(toString: String)
    }
}