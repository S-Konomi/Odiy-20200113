package jp.wonchu.odiy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import jp.wonchu.odiy.model.DefaultLocation

// 位置選択の画面。
// MainActivityとほぼ同一の内容だが、Firebaseとのやりとりがないので別Activityにした。
class SelectLocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null
    private lateinit var userLocationLiveData: UserLocationLiveData
    private var userLocationMarker: Marker? = null
    private var userLocationCircle: Circle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_location)

        findViewById<View>(R.id.btn_to_input_detail).setOnClickListener {
            // 中心座標が正しく取れた場合に限り、詳細画面に遷移する。
            googleMap?.cameraPosition?.let { cameraPosition ->
                val intent = Intent(this, InputDetailActivity::class.java)
                intent.putExtra("center", cameraPosition)
                startActivity(intent)
            }
        }
        findViewById<View>(R.id.btn_cancel).setOnClickListener {
            finish()
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        userLocationLiveData = UserLocationLiveData(this)
        startLocationUpdateIfPossible()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 初期位置の指定。
        // MainActivityから中心位置が渡されていればそれを使い、ない場合にはDefaultLocationに保存されているものを使う。
        intent?.getParcelableExtra<CameraPosition?>("center")?.let { cameraPosition ->
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    cameraPosition.target,
                    cameraPosition.zoom
                )
            )
        } ?: run {
            val defaultLocation = DefaultLocation(this)
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    defaultLocation.location,
                    defaultLocation.zoom
                )
            )
        }
    }

    private fun startLocationUpdateIfPossible() {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val locationUpdatable = requiredPermissions.all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (locationUpdatable) {
            observeLocationUpdates()
        }
    }

    private fun observeLocationUpdates() {
        userLocationLiveData.observe(this, Observer { location ->
            googleMap?.let { updateUserLocationIndicator(it, location) }
        })
    }

    private fun updateUserLocationIndicator(map: GoogleMap, location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val accuracy = location.accuracy.toDouble()
        userLocationMarker?.let {
            it.position = LatLng(location.latitude, location.longitude)
        } ?: run {
            val opt = MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 0.5f) // 画像の真ん中がポイントしたい場所になるようにする。
                .icon(
                    createBitmapDescriptorFromVector(
                        R.drawable.ic_my_location_black_24dp,
                        ContextCompat.getColor(this, R.color.user_location_marker_color)
                    )
                )
            userLocationMarker = map.addMarker(opt)
        }

        userLocationCircle?.let {
            it.center = latLng
            it.radius = accuracy
        } ?: run {
            val opt = CircleOptions()
                .center(latLng)
                .fillColor(ContextCompat.getColor(this, R.color.user_location_circle_fill_color))
                .strokeColor(ContextCompat.getColor(this, R.color.user_location_circle_stroke_color))
                .strokeWidth(2.0f)
                .radius(accuracy)
            userLocationCircle = map.addCircle(opt)
        }
    }

    private fun createBitmapDescriptorFromVector(@DrawableRes vectorResourceId: Int, @ColorInt tintColor: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, vectorResourceId, null)
            ?: return BitmapDescriptorFactory.defaultMarker()

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        DrawableCompat.setTint(vectorDrawable, tintColor)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
