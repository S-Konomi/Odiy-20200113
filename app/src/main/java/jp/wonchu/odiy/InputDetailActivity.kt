package jp.wonchu.odiy

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import jp.wonchu.odiy.databinding.ActivityInputDetailBinding
import jp.wonchu.odiy.model.ToiletFormData

// トイレ情報の入力画面。
// フォームデータはViewModelで保持し、
// 画面表示はデータバインディングを利用。
class InputDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputDetailBinding
    private lateinit var viewModel: InputDetailViewModel
    private lateinit var location: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val center = intent?.getParcelableExtra<CameraPosition?>("center")?.target
        if (center == null) {
            Toast.makeText(this, "緯度経度が未指定です。", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        location = center

        // データバインディング
        binding = DataBindingUtil.setContentView(this, R.layout.activity_input_detail)
        viewModel = ViewModelProvider(this).get(InputDetailViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // 投稿/キャンセルボタンの処理
        binding.btnSubmit.setOnClickListener {
            val name = viewModel.name.value
            val availableTime = viewModel.availableTime.value
            val availableDays = viewModel.currentAvailableDays()
            val availableUsers = viewModel.availableUsers()
            val type = viewModel.type()
            val shed = viewModel.shed()
            val installationfloor = viewModel.installationfloor.value
            val importantPoint = viewModel.importantPoint.value

            if (name.isNullOrBlank() || availableTime.isNullOrBlank() ||
                availableDays == null || availableUsers == null ||
                type == null || shed == null ||
                installationfloor.isNullOrBlank() || importantPoint.isNullOrBlank()) {
                Toast.makeText(this, "入力された情報が正しくありません", Toast.LENGTH_LONG).show()
            } else {
                viewModel.postNewToilet(ToiletFormData(name, availableTime, location, availableDays,availableUsers,type,shed,installationfloor,importantPoint))
            }
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }

        // postNewToiletの結果をViewModel経由で受け取る。
        viewModel.successResult.observe(this, Observer { result ->
            if (result == true) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("lat_lng", location)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        })
        viewModel.failureResult.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, "トイレ情報を投稿できません", Toast.LENGTH_LONG).show()
            }
        })
    }
}
