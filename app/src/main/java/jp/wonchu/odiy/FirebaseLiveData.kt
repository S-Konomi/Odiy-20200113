package jp.wonchu.odiy

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

// 参考:
// https://developers-jp.googleblog.com/2018/03/using-android-architecture-components.html
class FirebaseLiveData(private val ref: DatabaseReference) : LiveData<DataSnapshot>() {
    private val listener = object : ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
            Log.e("FirebaseLiveData", "Database error", databaseError.toException())
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            value = dataSnapshot
        }
    }

    override fun onActive() {
        ref.addValueEventListener(listener)
    }

    override fun onInactive() {
        ref.removeEventListener(listener)
    }
}
