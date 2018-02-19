package com.wrewolf.thetaleclient.login.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.view.RxView
import com.wrewolf.thetaleclient.R
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_login_password.view.*

class LoginPasswordView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL

        LayoutInflater.from(context).inflate(R.layout.layout_login_password, this)
    }

    fun loginClicks(): Observable<View> {
        return RxView.clicks(actionLogin)
                .map { it as View }
    }

    fun remindPasswordClicks(): Observable<View> {
        return RxView.clicks(passwordRemind)
                .map { it as View }
    }
}