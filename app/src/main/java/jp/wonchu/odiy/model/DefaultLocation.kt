package jp.wonchu.odiy.model

import android.content.Context
import androidx.core.content.edit
import com.google.android.gms.maps.model.LatLng

// 起動時にどの位置を初期表示するか。
class DefaultLocation(context: Context) {
    private val sharedPreference = context.getSharedPreferences("default_location", Context.MODE_PRIVATE)

    companion object {
        // デフォルトのマップの中心位置とズーム。（東京駅）
        // https://www.google.com/maps/@35.6812405,139.7649308,15z
        private val DEFAULT_LATLNG = LatLng(35.6812405, 139.7649308)
        private const val DEFAULT_ZOOM = 15.0f
    }

    val location: LatLng
        // SharedPreferenceに保存済みの値があればそれを使い、なければDEFAULT_LATLNGを使う。
        get() = sharedPreference.getString(
            "latlng",
            null
        )?.split(",")?.let { latlngArray ->
            LatLng(latlngArray[0].toDouble(), latlngArray[1].toDouble())
        } ?: DEFAULT_LATLNG

    val zoom: Float
        // SharedPreferenceに保存済みの値があればそれを使い、なければDEFAULT_ZOOMを使う。
        get() = sharedPreference.getFloat("zoom", DEFAULT_ZOOM)

    // SharedPreferenceに保存する。
    fun update(location: LatLng, zoom: Float) {
        sharedPreference.edit{
            putString("latlng", "${location.latitude},${location.longitude}")
            putFloat("zoom", zoom)
        }
    }
}
