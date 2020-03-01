package jp.wonchu.odiy.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.maps.android.clustering.ClusterItem

// トイレ情報。
// マーカー表示/詳細画面で必要な情報をもつデータクラス。
data class Toilet(
    val id: String,
    val name: String,
    val comment: String,
    val location: LatLng,
    val availableDays: String
) : ClusterItem {
    override fun getTitle() = name

    override fun getSnippet() = comment

    override fun getPosition() = location

    companion object {
        // Firebase Databaseのデータスナップショットからの復元。
        // 型違いの例外はキャッチせず、そのまま上げる。
        fun fromDataSnapshot(dataSnapshot: DataSnapshot): Toilet {
            val attributes = dataSnapshot.value as Map<String, Any>
            val locationAttributes = attributes["location"] as Map<String, Any>
            return Toilet(
                id = dataSnapshot.key!!,
                name = attributes["name"].toString(),
                comment = attributes["comment"].toString(),
                location = LatLng(
                    locationAttributes["latitude"] as Double,
                    locationAttributes["longitude"] as Double
                ),
                availableDays = attributes["availableDays"].toString()
            )
        }
    }
}

