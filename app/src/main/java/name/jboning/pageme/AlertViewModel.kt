package name.jboning.pageme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AlertViewModel : ViewModel() {
    private val _hasAcknowledged: MutableLiveData<Boolean> = MutableLiveData(false)
    private val _repliedWith: MutableLiveData<String> = MutableLiveData()
    val hasAcknowledged: LiveData<Boolean> = _hasAcknowledged
    val repliedWith: LiveData<String> = _repliedWith

    fun setAcknowledged() {
        _hasAcknowledged.postValue(true);
    }

    fun setRepliedWith(reply: String) {
        _repliedWith.postValue(reply)
    }
}