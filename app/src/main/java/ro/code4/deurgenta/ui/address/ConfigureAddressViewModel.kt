package ro.code4.deurgenta.ui.address

import androidx.lifecycle.MutableLiveData
import ro.code4.deurgenta.ui.base.BaseViewModel

class ConfigureAddressViewModel : BaseViewModel() {

    val address = MutableLiveData<String>()
}
