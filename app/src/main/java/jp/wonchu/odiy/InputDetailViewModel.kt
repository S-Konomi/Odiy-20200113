package jp.wonchu.odiy

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import jp.wonchu.odiy.model.ToiletFormData

class InputDetailViewModel : ViewModel() {
    val name = MutableLiveData<String>()
    val availableTime = MutableLiveData<String>()
    val availableOnSun = MutableLiveData<Boolean>(false)
    val availableOnMon = MutableLiveData<Boolean>(false)
    val availableOnTue = MutableLiveData<Boolean>(false)
    val availableOnWed = MutableLiveData<Boolean>(false)
    val availableOnThu = MutableLiveData<Boolean>(false)
    val availableOnFri = MutableLiveData<Boolean>(false)
    val availableOnSat = MutableLiveData<Boolean>(false)
    val availableAnyone = MutableLiveData<Boolean>(false)
    val availableUsersOnly = MutableLiveData<Boolean>(false)
    val type_HotWaterToiletSeat = MutableLiveData<Boolean>(false)
    val type_WesternStyle = MutableLiveData<Boolean>(false)
    val type_JapaneseStyle = MutableLiveData<Boolean>(false)
    val shed_possible = MutableLiveData<Boolean>(false)
    val shed_impossible = MutableLiveData<Boolean>(false)
    val installationfloor = MutableLiveData<String>()
    val importantPoint = MutableLiveData<String>()

    fun currentAvailableDays(): String {
        return mapOf(
            "(日)" to availableOnSun,
            "(月)" to availableOnMon,
            "(火)" to availableOnTue,
            "(水)" to availableOnWed,
            "(木)" to availableOnThu,
            "(金)" to availableOnFri,
            "(土)" to availableOnSat
        ).filterValues { livedata -> livedata.value == true }.keys.joinToString(", ")
    }

    fun availableUsers(): String {
        return mapOf(
            "誰でもＯＫ" to availableAnyone,
            "建物利用者のみ" to availableUsersOnly
        ).filterValues { livedata -> livedata.value == true }.keys.joinToString(", ")
    }

    fun type(): String {
        return mapOf(
            "温水便座" to type_HotWaterToiletSeat,
            "洋式" to type_WesternStyle,
            "和式" to type_JapaneseStyle
        ).filterValues { livedata -> livedata.value == true }.keys.joinToString(", ")
    }

    fun shed(): String {
        return mapOf(
            "流せる" to shed_possible,
            "流せない" to shed_impossible
        ).filterValues { livedata -> livedata.value == true }.keys.joinToString(", ")
    }

    fun postNewToilet(toilet: ToiletFormData) {
        val childRef = FirebaseDatabase.getInstance().getReference("public").child("toilets")
        childRef.push().setValue(toilet) { databaseError, databaseReference ->
            databaseError?.let {
                failureResult.value = it.toException()
            } ?: run {
                successResult.value = true
            }
        }
    }

    val successResult = MutableLiveData<Boolean>()
    val failureResult = MutableLiveData<Exception>()
}
