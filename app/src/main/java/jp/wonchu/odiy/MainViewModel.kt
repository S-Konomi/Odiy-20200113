package jp.wonchu.odiy

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val toilets = ToiletListLiveData()
}
