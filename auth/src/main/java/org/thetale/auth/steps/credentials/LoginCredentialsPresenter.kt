package org.thetale.auth.steps.credentials

import org.thetale.auth.LoginNavigationProvider
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.thetale.core.PresenterState
import org.thetale.api.TheTaleService
import org.thetale.api.call
import org.thetale.api.error.ResponseException

class LoginCredentialsPresenter(private val service: TheTaleService,
                                private val navigationProvider: LoginNavigationProvider) {

    private val state = PresenterState { view?.hideProgress() }
    private var loginJob: Job? = null

    var view: LoginCredentialsView? = null

    fun start() {
        state.start()
    }

    fun stop() {
        state.stop()
    }

    fun loginWithCredentials(email: String, password: String) {
        loginJob = launch(UI) {
            try {
                state.apply { view?.showProgress() }
                service.login(email, password).call()

                navigationProvider.navigation?.openApp()

                state.apply { view?.hideProgress() }
            } catch (e: ResponseException) {
                processResponseError(e)
            } catch (e: Exception) {
                state.apply { view?.showError() }
            }
        }
    }

    private fun processResponseError(e: ResponseException) {
        if (e.errors != null) {
            state.apply { view?.showLoginError(e.getEmailError(), e.getPasswordError()) }
        } else if (e.error != null) {
            state.apply { view?.showLoginError(e.getGeneralError(), null) }
        }
    }

    fun navigateToThirdParty() {
        navigationProvider.navigation?.showThirdParty()
    }

    fun dispose() {
        loginJob?.cancel()
    }
}

fun ResponseException.getEmailError(): String? {
    return errors?.get("email")?.get(0)
}

fun ResponseException.getPasswordError(): String? {
    return errors?.get("password")?.get(0)
}

fun ResponseException.getGeneralError(): String? {
    return error
}