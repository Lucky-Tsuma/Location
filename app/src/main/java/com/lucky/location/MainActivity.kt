package com.lucky.location

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.lucky.location.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        const val REQUEST_LOCATION = 100
        const val REQUEST_CHECK_SETTINGS = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        /*SETTING UP A LOCATION REQUEST
    LocationRequest object defines settings to enable certain services on the device such as GPS or WIFI scanning. Does this indirectly by specifying the desired
    * level of accuracy/power consumption and desired update interval for the application*/
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 //update interval
            fastestInterval =
                5000 //fastest rate in milliseconds by which the app can handle location updates
            priority =
                LocationRequest.PRIORITY_HIGH_ACCURACY //Gives google play services a strong hint about what location sources to use e.g

            /*PRIORITY_BALANCED_POWER_ACCURACY: precision within a city block. say 100m. coarse level accuracy. low power consumption. uses wifi and cell tower positioning.
            * PRIORITY_HIGH_ACCURACY: high precision using GPS to determine location
            * PRIORITY_LOW_POWER: city level precision. say 10km. coarse level location access
            * PRIORITY_NO_POWER: app triggers no location updates. uses location triggered by other apps.*/
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        /*We just created a fused location services client on the statement above*/

        binding.btnLocation.setOnClickListener {
            /*GETTING CURRENT LOCATION SETTINGS
    * We do this by LocationSettingsRequest.builder and adding a LocationRequest object to it as below*/
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

            //Next, check whether the current location settings are satisfied as below
            val client: SettingsClient = LocationServices.getSettingsClient(this)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

//            All location settings are satisfied. The client can make location requests
            task.addOnSuccessListener { locationSettingsResponse ->
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_LOCATION
                    )
                } else {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            if (location != null) {
                                binding.txtLocation.text =
                                    "Latitude: ${location.latitude}\nLongitude: ${location.longitude}"
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Cannot trace location, still in production mode",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }

            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException){
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(this,
                            REQUEST_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }

        }

    }

}