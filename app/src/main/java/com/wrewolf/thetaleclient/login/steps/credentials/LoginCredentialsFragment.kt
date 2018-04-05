package com.wrewolf.thetaleclient.login.steps.credentials

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.wrewolf.thetaleclient.R
import com.wrewolf.thetaleclient.TheTaleClientApplication
import kotlinx.android.synthetic.main.fragment_login_credentials.*
import javax.inject.Inject

class LoginCredentialsFragment : Fragment(), LoginCredentialsView {

    @Inject
    lateinit var presenter: LoginCredentialsPresenter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        TheTaleClientApplication.getComponentProvider()
                .loginComponent!!
                .inject(this)

        presenter.view = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_credentials, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginPasswordView.onLoginClick {
            presenter.loginWithCredentials(it.email, it.password)
        }
    }

    override fun onStart() {
        super.onStart()

        presenter.start()
    }

    override fun onStop() {
        super.onStop()

        presenter.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        presenter.view = null
    }

    override fun onDestroy() {
        super.onDestroy()

        if (activity!!.isFinishing) {
            presenter.dispose()
        }
    }

    override fun showProgress() {
        progress.visibility = VISIBLE
        loginPasswordView.visibility = GONE
    }

    override fun hideProgress() {
        progress.visibility = GONE
        loginPasswordView.visibility = VISIBLE
    }

    override fun showError() {
        progress.visibility = GONE
        loginPasswordView.visibility = VISIBLE

        loginPasswordView.setPasswordError(getString(R.string.common_error))
    }

    override fun showLoginError(loginError: String?, passwordError: String?) {
        progress.visibility = GONE
        loginPasswordView.visibility = VISIBLE

        loginPasswordView.setEmailError(loginError)
        loginPasswordView.setPasswordError(passwordError)
    }

    companion object {

        fun create() = LoginCredentialsFragment()
    }
}