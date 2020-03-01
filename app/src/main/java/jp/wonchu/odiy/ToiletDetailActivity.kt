package jp.wonchu.odiy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import jp.wonchu.odiy.databinding.ActivityToiletDetailBinding
import jp.wonchu.odiy.model.Toilet

// トイレ詳細情報の表示画面
// トイレIDを受け取り、それをもとにFirebaseのDatabaseの情報をリアルタイム表示している。
// 画面表示はデータバインディングを利用。
class ToiletDetailActivity : AppCompatActivity() {

    private lateinit var toilet: LiveData<Toilet?>
    private lateinit var binding: ActivityToiletDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.getStringExtra("id")?.let { id ->
            val childRef = FirebaseDatabase.getInstance().getReference("public").child("toilets")
            toilet = Transformations.map(FirebaseLiveData(childRef.child(id))) { snapshot ->
                // 削除された場合に、keyはあるがvalueがnullのsnapshotが通知される。
                snapshot.value?.let { Toilet.fromDataSnapshot(snapshot) }
            }
        } ?: run {
            Toast.makeText(this, "IDが正しくないか、すでに削除されています。", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // データバインディング
        binding = DataBindingUtil.setContentView(this, R.layout.activity_toilet_detail)

        // トイレ情報の取得・画面更新
        toilet.observe(this, Observer { toilet ->
            if (toilet == null) {
                // 画面を開いている最中に、トイレ情報が（別の誰か, adminなどによって）削除された場合にここに入る。
                // レアケース。
                Toast.makeText(this, "削除されました。", Toast.LENGTH_LONG).show()
                finish()
            } else {
                // トイレ情報が取得できた/更新されたタイミングで、ここに入る。
                binding.toilet = toilet
            }
        })
    }
}
