package current.location.sample.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val LOG_TAG: String? = "MainActivity"
    private val PERMISSIONS_REQUEST: Int = 101
    lateinit var mSettingsClient: SettingsClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initCurrentLocation()

        StartLocationUpdates.setOnClickListener({ initCurrentLocation() })
    }

    private fun initCurrentLocation() {
        // Get the location manager
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
            Toast.makeText(this, getString(R.string.please_enable_location_services), Toast.LENGTH_LONG).show()
        }

        val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (locationPermission == PackageManager.PERMISSION_GRANTED) {
            startLocationTrackerService()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST
            )
        }

//        mSettingsClient = LocationServices.getSettingsClient(this)
//        mSettingsClient
//            .checkLocationSettings(mLocationSettingsRequest)
//            .addOnSuccessListener(this, OnSuccessListener<LocationSettingsResponse> {
//                Log.i(TAG, "All location settings are satisfied.")
//
//                Toast.makeText(applicationContext, "Started location updates!", Toast.LENGTH_SHORT).show()
//
//
//                mFusedLocationClient.requestLocationUpdates(
//                    mLocationRequest,
//                    mLocationCallback, Looper.myLooper()
//                )
//
//                updateLocationUI()
//            })
//            .addOnFailureListener(this, OnFailureListener { e ->
//                val statusCode = (e as ApiException).statusCode
//                when (statusCode) {
//                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
//                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " + "location settings ")
//                        try {
//                            // Show the dialog by calling startResolutionForResult(), and check the
//                            // result in onActivityResult().
//                            val rae = e as ResolvableApiException
//                            rae.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTINGS)
//                        } catch (sie: IntentSender.SendIntentException) {
//                            Log.i(TAG, "PendingIntent unable to execute request.")
//                        }
//
//                    }
//                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
//                        val errorMessage =
//                            "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
//                        Log.e(TAG, errorMessage)
//
//                        Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
//                    }
//                }
//
//                updateLocationUI()
//            })
    }

    private fun startLocationTrackerService() {
        val locationRequest = LocationRequest()
        val INTERVAL_LOCATION_UPDATES: Long = 10000
        locationRequest.setInterval(INTERVAL_LOCATION_UPDATES)
        val FASTEST_INTERVAL_LOCATION_UPDATES: Long = 5000
        locationRequest.setFastestInterval(FASTEST_INTERVAL_LOCATION_UPDATES)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        Log.v(LOG_TAG, "locationRequest prepared")

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (locationPermission == PackageManager.PERMISSION_GRANTED) {
            Log.v(LOG_TAG, "before fusedLocationProviderClient.requestLocationUpdates()")
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.getLastLocation()
                    if (location != null) {
                        Log.v(LOG_TAG, "onLocationResult() lat: " + location.latitude + " lng:" + +location.getLongitude())
                        //TODO deliver updated location broadcast to Activity
                    } else
                        Log.e(LOG_TAG, "location null in onLocationResult()")
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                    Log.v(LOG_TAG, "onLocationAvailability() : " + locationAvailability)
                    super.onLocationAvailability(locationAvailability)
                }
            }, null)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v(LOG_TAG, "grantResults PackageManager.PERMISSION_GRANTED")
                initCurrentLocation()
            } else
                Log.e(LOG_TAG, "grantResults does not match:" + grantResults.size)
        } else
            Log.e(LOG_TAG, "requestCode does not match:$requestCode")
    }
}