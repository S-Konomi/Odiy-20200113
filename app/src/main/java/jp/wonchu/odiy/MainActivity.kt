package jp.wonchu.odiy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.clustering.ClusterManager
import jp.wonchu.odiy.model.DefaultLocation
import jp.wonchu.odiy.model.Toilet

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val REQUEST_CODE_PERMISSION = 10
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var fab: FloatingActionButton

    private var googleMap: GoogleMap? = null
    private lateinit var userLocationLiveData: UserLocationLiveData
    private var userLocationMarker: Marker? = null
    private var userLocationCircle: Circle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        setContentView(R.layout.activity_main)

        // トイレ情報の追加ボタン押下時のイベントハンドラ
        fab = findViewById(R.id.btn_to_select_location)
        fab.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity::class.java)
            googleMap?.cameraPosition?.let { intent.putExtra("center", it) }
            startActivity(intent)
            overridePendingTransition(0, 0)
        }


        // Google Mapの準備ができたらonMapReadyをコールバックしてもらう。
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 現在地の更新リクエスト
        userLocationLiveData = UserLocationLiveData(this)
        requestLocationPermission()
    }

    // GoogleMapのロードが完了したタイミングで１回だけ呼ばれる。
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val clusterManager = ClusterManager<Toilet>(this, map)
        clusterManager.setOnClusterItemInfoWindowClickListener { toilet ->
            val intent = Intent(this, ToiletDetailActivity::class.java)
            intent.putExtra("id", toilet.id)
            startActivity(intent)
        }
        val clusterRenderer = ToiletClusterRenderer(this, map, clusterManager)
        clusterManager.renderer = clusterRenderer
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener { marker ->
            onInfoWindowShown()
            clusterManager.onMarkerClick(marker)
        }
        map.setOnInfoWindowClickListener(clusterManager)
        map.setOnInfoWindowCloseListener { marker ->
            onInfowindowHidden()
        }

        // 初期位置は明洞
        val myeong_dong = LatLng(37.560989, 126.986187)
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                myeong_dong,18.0f
            )
        )

        //現在位置ボタンの設置
        map.uiSettings.run {
            map.isMyLocationEnabled = true
        }

        //回転操作不可の設定
        map.uiSettings.isRotateGesturesEnabled = false

        // 位置情報指定があれば、そこに向かって移動する。
        // トイレ情報の追加を終えて戻ってくるときに、緯度経度指定がされるので、ここを通る。
        intent?.getParcelableExtra<LatLng?>("lat_lng")?.let { latLng ->
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }

        // トイレ一覧に変更があれば、マーカーリストを再構築する。
        viewModel.toilets.observe(this, Observer { list ->
            clusterManager.apply {
                clearItems()
                addItems(list)
                cluster()
            }
        })

        // 現在地情報があれば、ユーザの現在地を表すマーカーを更新する。
        userLocationLiveData.value?.let {
            updateUserLocationIndicator(map, it)
        }
    }

    // 位置取得の権限があれば、現在地のアップデートを即座に開始する。
    // 権限が足りなければ、位置情報取得権限をリクエストする。（そのコールバックで、許可がされていれば現在地のアップデートを開始する）
    private fun requestLocationPermission() {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val requestPermissions = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (requestPermissions.isEmpty()) {
            observeLocationUpdates()
            return
        }

        val explanationNeeded = requestPermissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }
        if (explanationNeeded) {
            Toast.makeText(this, "現在地を表示するには、位置情報の測位の許可をしていただく必要があります。", Toast.LENGTH_LONG).show()
        }

        ActivityCompat.requestPermissions(
            this,
            requestPermissions.toTypedArray(),
            REQUEST_CODE_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != REQUEST_CODE_PERMISSION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        // ネットワーク測位かGPS測位のいずれかが許可されれば、とりあえず現在地測位を開始する。
        if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
            observeLocationUpdates()
        }
    }

    // 現在地測位の開始・現在地の監視
    private fun observeLocationUpdates() {
        // 現在地情報が更新されたら、ユーザの現在地を表すマーカーを更新する。
        userLocationLiveData.observe(this, Observer { location ->
            googleMap?.let { updateUserLocationIndicator(it, location) }
        })
    }

    // ユーザの現在地を表すマーカーを更新する
    private fun updateUserLocationIndicator(map: GoogleMap, location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val accuracy = location.accuracy.toDouble()

        // 中心のアイコン画像
        userLocationMarker?.let {
            it.position = LatLng(location.latitude, location.longitude)
        } ?: run {
            val opt = MarkerOptions()
                .title("現在地")
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

        // accuracyが半径の円
        userLocationCircle?.let {
            it.center = latLng
            it.radius = accuracy
        } ?: run {
            val opt = CircleOptions()
                .center(latLng)
                .fillColor(ContextCompat.getColor(this, R.color.user_location_circle_fill_color))
                .strokeColor(
                    ContextCompat.getColor(
                        this,
                        R.color.user_location_circle_stroke_color
                    )
                )
                .strokeWidth(2.0f)
                .radius(accuracy)
            userLocationCircle = map.addCircle(opt)
        }
    }

    private fun onInfoWindowShown() {
        fab.animate().apply {
            cancel()
            // Google Mapのナビゲーションをよけるために、FABを少し上に移動させる。
            translationY(-fab.height.toFloat())
            start()
        }
    }

    private fun onInfowindowHidden() {
        fab.animate().apply {
            cancel()
            translationY(0.0f)
            // 直後にonInfoWindowShownが呼ばれてアニメーションがガタガタっとならないよう、
            // 少しアニメーション開始を遅延させる。
            startDelay = 30
            start()
        }
    }

    // MarkerOptionsのiconにはR.drawable.xxxなどを直接指定はできず、
    // 画像リソースを一旦Canvasに描画してBitmapDescriptorに変換したものを指定する必要がある。
    // このメソッドは画像リソースからBitmapDescriptorを生成するユーティリティメソッドです。
    private fun createBitmapDescriptorFromVector(@DrawableRes vectorResourceId: Int, @ColorInt tintColor: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, vectorResourceId, null)
            ?: return BitmapDescriptorFactory.defaultMarker()

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, tintColor)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
