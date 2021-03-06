package org.thetale.auth.steps.thirdparty

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_login_third_party.*
import org.thetale.auth.R
import org.thetale.auth.di.LoginComponentProvider
import org.thetale.core.openUrl
import javax.inject.Inject

class LoginThirdPartyFragment : Fragment(), LoginThirdPartyView {

    @Inject
    internal lateinit var presenter: LoginThirdPartyPresenter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity?.application as LoginComponentProvider)
                .provideLoginComponent()
                ?.inject(this)

        presenter.view = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_third_party, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        confirmThirdParty.onConfirmClick(View.OnClickListener {
            presenter.checkAuthorisation()
        })

        confirmThirdParty.onTryAgainClick(View.OnClickListener {
            presenter.openLink()
        })
    }

    override fun onStart() {
        super.onStart()

        presenter.start()
    }

    override fun onStop() {
        super.onStop()

        presenter.stop()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (activity!!.isFinishing) {
            presenter.dismiss()
        }
    }

    override fun showProgress() {
        progress.visibility = VISIBLE
        confirmThirdParty.visibility = GONE
    }

    override fun hideProgress() {
        progress.visibility = GONE
        confirmThirdParty.visibility = VISIBLE
    }

    override fun showError() {
        confirmThirdParty.setError(getString(R.string.error_third_party_login))
    }

    override fun hideError() {
        confirmThirdParty.setError(null)
    }

    override fun openThirdPartyLink(url: String) {
        openUrl(activity!!, url)
    }

    companion object {
        fun create() = LoginThirdPartyFragment()
    }
}