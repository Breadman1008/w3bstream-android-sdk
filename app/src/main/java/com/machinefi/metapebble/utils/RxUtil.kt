package com.machinefi.metapebble.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import com.fasterxml.jackson.databind.util.JSONPObject
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Observer
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object RxUtil {

    fun clicks(view: View?): Observable<String> {
        return ViewClickObservable(view)
    }

    fun textChange(view: TextView?): Observable<String> {
        return TextChangeObservable(view)
    }

    fun <T> observableSchedulers(): ObservableTransformer<T, T> {
        return ObservableTransformer<T, T> {
            it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun <T> singleSchedulers(): SingleTransformer<T, T> {
        return SingleTransformer<T, T> {
            it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

}

class ViewClickObservable(val view: View?) : Observable<String>() {

    override fun subscribeActual(observer: Observer<in String>?) {
        view?.setOnClickListener {
            observer?.onNext("onClick")
        }
    }

}

class TextChangeObservable(val view: TextView?) : Observable<String>() {


    override fun subscribeActual(observer: Observer<in String>?) {
        view?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                observer?.onNext(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }


}