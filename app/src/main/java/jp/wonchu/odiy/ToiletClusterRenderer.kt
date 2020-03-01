package jp.wonchu.odiy

import android.R.attr.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import jp.wonchu.odiy.model.DefaultLocation
import jp.wonchu.odiy.model.Toilet
import android.graphics.drawable.Icon
import com.google.maps.android.clustering.Cluster
import android.app.Person
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker
import android.R.attr.name








class ToiletClusterRenderer(
    private val context: Context,
    private val googleMap: GoogleMap,
    private val clusterManager: ClusterManager<Toilet>
) : DefaultClusterRenderer<Toilet>(context, googleMap, clusterManager), GoogleMap.OnCameraIdleListener {

    override fun onCameraIdle() {
        // GoogleMapの場所が変わった際に、次回以降の起動時のデフォルト位置を更新する
        val cameraPosition = googleMap.cameraPosition
        DefaultLocation(context).update(cameraPosition.target, cameraPosition.zoom)
    }

    override fun onBeforeClusterItemRendered(item: Toilet?, markerOptions: MarkerOptions?) {
        markerOptions?.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_1))
    }
}