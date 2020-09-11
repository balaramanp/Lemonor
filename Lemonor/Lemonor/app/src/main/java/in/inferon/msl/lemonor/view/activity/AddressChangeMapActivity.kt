package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.activity_address_change_map.*
import kotlinx.android.synthetic.main.activity_address_marker_map.saveBT

class AddressChangeMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraIdleListener {

    private val TAG = ShopMarkerMapActivity::class.java.simpleName
    private lateinit var mMap: GoogleMap
    internal var latitude: Double = 0.toDouble()
    internal var longitude: Double = 0.toDouble()
    private var mlatitude: Double = 0.toDouble()
    internal var mlongitude: Double = 0.toDouble()
    private var markedFromLocation: LatLng? = null
    internal var currentLocationMarker: Marker? = null
    internal var fromMarkerName: Marker? = null
    internal var locationMarked = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var useCurrentLocation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_change_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val erode = LatLng(11.3410364, 77.7171642)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(erode))

        mMap.setOnCameraMoveListener(this)
        mMap.setOnCameraIdleListener(this)

        Log.e(TAG, "Changed Latitude : " + Constants.changedLatitude)
        if (Constants.changedLatitude != 0.0) {
            mlatitude = Constants.changedLatitude
            mlongitude = Constants.changedLongitude
            markedFromLocation = LatLng(mlatitude, mlongitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markedFromLocation, 15f))
        } else {
            loadFusedLocation()
        }

        saveBT.setOnClickListener {
            mlatitude = mMap.cameraPosition.target.latitude
            mlongitude = mMap.cameraPosition.target.longitude
            loadLocationMarker()
            finish()
        }

        useCurrentLocationBT.setOnClickListener {
            useCurrentLocation = true
            loadFusedLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadFusedLocation() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                try {
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        Log.e(TAG, "Location : " + location.latitude + ", " + location.longitude)

                        mlatitude = location.latitude
                        mlongitude = location.longitude
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        Log.e(TAG, "Current Location : $latitude, $longitude")
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(currentLocation, 15f),
                            1000,
                            object : GoogleMap.CancelableCallback {
                                override fun onFinish() {
                                    if (useCurrentLocation) {
                                        Constants.changedLatitude = 0.0
                                        Constants.changedLongitude = 0.0
                                        mlatitude = 0.0
                                        mlongitude = 0.0
                                        loadLocationMarker()
                                        finish()
                                    }
                                }

                                override fun onCancel() {

                                }
                            })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            mlatitude = mLastLocation.latitude
            mlongitude = mLastLocation.longitude
            val currentLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            Log.e(TAG, "Current Location : $latitude, $longitude")
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
        }
    }

    override fun onCameraIdle() {
        if (::mMap.isInitialized) {
            val latLng = mMap.cameraPosition.target;
            Log.e("On Camera Idle", latLng.latitude.toString() + " - " + latLng.longitude.toString());
        }
    }

    override fun onCameraMove() {
    }

    private fun loadLocationMarker() {
        val intent = Intent("LocationChanged")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        Constants.changedLatitude = mlatitude
        Constants.changedLongitude = mlongitude
    }
}
