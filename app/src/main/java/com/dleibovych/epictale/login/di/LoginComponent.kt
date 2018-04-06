package com.dleibovych.epictale.login.di

import com.dleibovych.epictale.login.LoginActivity
import com.dleibovych.epictale.login.steps.chooser.LoginChooserFragment
import com.dleibovych.epictale.login.steps.credentials.LoginCredentialsFragment
import com.dleibovych.epictale.login.steps.status.CheckStatusFragment
import com.dleibovych.epictale.login.steps.thirdparty.LoginThirdPartyFragment
import dagger.Subcomponent

@LoginScope @Subcomponent(modules = [LoginModule::class, LoginNavigationModule::class])
interface LoginComponent {

    fun inject(target: LoginActivity)

    fun inject(target: LoginChooserFragment)

    fun inject(target: LoginThirdPartyFragment)

    fun inject(target: CheckStatusFragment)

    fun inject(target: LoginCredentialsFragment)
}