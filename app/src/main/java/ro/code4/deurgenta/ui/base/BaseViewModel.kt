package ro.code4.deurgenta.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import ro.code4.deurgenta.helper.SingleLiveEvent

abstract  class BaseViewModel(private val useCase: UseCase<*, *>? = null) : ViewModel(), KoinComponent {
    val messageIdToastLiveData = SingleLiveEvent<String>()

    fun messageToast(): LiveData<String> = messageIdToastLiveData
    open fun onError(throwable: Throwable) = Unit

    override fun onCleared() {
        super.onCleared()
        useCase?.dispose()
    }
}