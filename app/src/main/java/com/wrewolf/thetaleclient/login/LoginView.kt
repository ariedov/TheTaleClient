package com.wrewolf.thetaleclient.login

interface LoginView {

    fun showLoading()

    fun showChooser()

    fun showEmailPassword()

    fun showEmailPassword(email: String, password: String)

    fun showInitError()

    fun showLoginError(email: String, password: String, emailError: String?, passwordError: String?)

    fun showThirdParty()

    fun showThirdPartyError()

    fun showThirdPartyStatusError()
}