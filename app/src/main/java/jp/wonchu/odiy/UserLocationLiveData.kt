package jp.wonchu.odiy

import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import com.google.android.gms.location.*

// 現在地情報を保持するLiveData。
// 参考: https://proandroiddev.com/android-tutorial-on-location-update-with-livedata-774f8fcc9f15
class UserLocationLiveData(context: Context) : LiveData<Location>() {
    private val locationProvider: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private val callback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.lastLocation?.let { value = it }
        }
    }

    override fun onActive() {
        locationProvider.lastLocation.addOnSuccessListener { location ->
            location?.let { value = it }
        }
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationProvider.requestLocationUpdates(locationRequest, callback, null)
    }

    override fun onInactive() {
        locationProvider.removeLocationUpdates(callback)
    }
}
