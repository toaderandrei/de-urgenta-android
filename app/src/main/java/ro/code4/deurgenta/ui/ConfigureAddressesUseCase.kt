package ro.code4.deurgenta.ui

import io.reactivex.Observable
import ro.code4.deurgenta.helper.Result
import ro.code4.deurgenta.repositories.Repository
import ro.code4.deurgenta.ui.base.UseCase

class ConfigureAddressesUseCase(private val repository: Repository): UseCase<String, Any>() {

    override fun buildUseCaseObservable(params: String): Observable<Result<Any>> {
        TODO("Not yet implemented")
    }
}