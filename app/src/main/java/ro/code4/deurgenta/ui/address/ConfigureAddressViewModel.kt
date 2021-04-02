package ro.code4.deurgenta.ui.address

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.schedulers.Schedulers
import ro.code4.deurgenta.data.model.MapAddress
import ro.code4.deurgenta.repositories.Repository
import ro.code4.deurgenta.ui.base.BaseViewModel
import ro.code4.deurgenta.helper.Result
import javax.inject.Inject

class ConfigureAddressViewModel @Inject constructor(val repository: Repository) : BaseViewModel() {

    private val saveAddressResult = MutableLiveData<Result<MapAddress>>()

    @SuppressLint("LongLogTag")
    fun saveAddress(currentAddress: MapAddress) {
        val disposable = repository.saveAddress(mapAddress = currentAddress)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                Log.d(TAG, "loading")
                saveAddressResult.postValue(Result.Loading)
            }
            .subscribe({
                Log.d(TAG, "success.")
                saveAddressResult.postValue(Result.Success(currentAddress))
            }, {
                Log.e(TAG, "error:", it)
                saveAddressResult.postValue(Result.Failure(it))
            })
        disposables.add(disposable)
    }

    fun saveResult(): LiveData<Result<MapAddress>> {
        return saveAddressResult
    }

    companion object {
        const val TAG = "ConfigureAddressViewModel"
    }
}
