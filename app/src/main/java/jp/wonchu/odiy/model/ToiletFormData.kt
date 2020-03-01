package jp.wonchu.odiy.model

import com.google.android.gms.maps.model.LatLng

// トイレ情報の追加フォームのデータを保持するためのデータクラス
data class ToiletFormData(
    val name: String,
    val comment: String,
    val location: LatLng,
    val availableDays: String,
    availableUsers: String,
    type: String,
    shed: String,
    installationfloor: String,
    importantPoint: String
)
