package name.jboning.pageme

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import name.jboning.pageme.config.ConfigManager
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val SHARED_PREFS = "prefs"
        const val PREF_SILENCE_UNTIL = "silenceUntil"
    }

    private val sharedPrefs = application.applicationContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
    private val _silenceUntil: MutableLiveData<Long> = MutableLiveData(-1)
    private val _silenced: MutableLiveData<Boolean> = MutableLiveData(false)
    val silenceUntil: LiveData<Long> = _silenceUntil
    val silenced: LiveData<Boolean> = _silenced
    private val timer = Timer()

    private val _rules = MutableLiveData(ConfigManager().getRules(application.applicationContext))
    val rules = _rules

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, s ->
        if (s == PREF_SILENCE_UNTIL) {
            onSilenceUntilUpdated(sharedPreferences.getLong(PREF_SILENCE_UNTIL, -1))
        }
    }

    init {
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener)
        prefListener.onSharedPreferenceChanged(sharedPrefs, PREF_SILENCE_UNTIL)
    }

    private fun onSilenceUntilUpdated(value: Long) {
        _silenceUntil.value = value
        _silenced.value = computeSilenced()
        scheduleSilencedUpdate()
    }

    private fun scheduleSilencedUpdate() {
        if (_silenced.value!!) {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    Log.d("MainViewModel", "Silenced-update timer fired");
                    _silenced.postValue(computeSilenced())
                    if (_silenced.value!!) {
                        // If we're still silenced, it could be because the clock changed between
                        // when the timer fired and calling computeSilenced(). Schedule another
                        // update to handle that case (though it's more likely that the user simply
                        // changed the silence-until time).
                        scheduleSilencedUpdate()
                    }
                }
            }, Date(_silenceUntil.value!!));
        }
    }

    private fun computeSilenced() = _silenceUntil.value!! > System.currentTimeMillis()

    fun setSilenceUntil(value: Long) {
        Log.d("MainViewModel", "set silenceUntil")
        val editor = sharedPrefs.edit()
        editor.putLong(PREF_SILENCE_UNTIL, value)
        editor.apply()
    }

    override fun onCleared() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(prefListener)
        timer.cancel()
        super.onCleared()
    }
}