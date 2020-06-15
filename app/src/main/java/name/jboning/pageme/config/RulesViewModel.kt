package name.jboning.pageme.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class RulesViewModel(application: Application) : AndroidViewModel(application) {
    private val _rules = MutableLiveData(ConfigManager().getRules(application.applicationContext))
    val rules = _rules
}