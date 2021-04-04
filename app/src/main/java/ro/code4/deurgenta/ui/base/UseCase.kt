package ro.code4.deurgenta.ui.base

import android.annotation.SuppressLint
import androidx.core.util.Preconditions
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import ro.code4.deurgenta.helper.Result

abstract class UseCase<in REQUEST, RESPONSE>(
    private val backgroundScheduler: Scheduler = Schedulers.io(),
    private val mainThreadScheduler: Scheduler = AndroidSchedulers.mainThread()
) where RESPONSE : Any {
    private lateinit var disposables: CompositeDisposable

    abstract fun buildUseCaseObservable(params: REQUEST): Observable<Result<RESPONSE>>

    fun execute(result: DisposableObserver<Result<RESPONSE>>, params: REQUEST) {
        val observable: Observable<Result<RESPONSE>> = buildUseCaseObservable(params)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainThreadScheduler)
        addDisposable(observable.subscribeWith(result))
    }

    fun dispose() {
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun addDisposable(disposable: Disposable) {
        Preconditions.checkNotNull(disposable)
        Preconditions.checkNotNull(disposables)
        disposables.add(disposable)
    }
}