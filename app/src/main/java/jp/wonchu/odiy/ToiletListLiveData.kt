package jp.wonchu.odiy

import android.util.Log
import androidx.lifecycle.LiveData
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.database.ChangeEventListener
import com.firebase.ui.database.FirebaseArray
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import jp.wonchu.odiy.model.Toilet

// トイレ情報の一覧を保持するLiveData。
class ToiletListLiveData : LiveData<List<Toilet>>() {
    private val childRef = FirebaseDatabase.getInstance().getReference("public").child("toilets")
    private val toiletList =
        FirebaseArray<Toilet>(childRef, SnapshotParser<Toilet> { Toilet.fromDataSnapshot(it) })

    private val listener = object : ChangeEventListener {
        override fun onDataChanged() {
            value = toiletList
        }

        override fun onChildChanged(
            type: ChangeEventType,
            snapshot: DataSnapshot,
            newIndex: Int,
            oldIndex: Int
        ) {
            value = toiletList
        }

        override fun onError(e: DatabaseError) {
            Log.d("ToiletListLiveData", "Database error", e.toException())
        }
    }

    override fun onActive() {
        toiletList.addChangeEventListener(listener)
    }

    override fun onInactive() {
        toiletList.removeChangeEventListener(listener)
    }
}
