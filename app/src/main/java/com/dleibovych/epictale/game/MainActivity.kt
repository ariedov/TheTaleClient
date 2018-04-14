package com.dleibovych.epictale.game

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView

import com.dleibovych.epictale.DrawerItem
import com.dleibovych.epictale.R
import com.dleibovych.epictale.TheTaleClientApplication
import com.dleibovych.epictale.api.cache.RequestCacheManager
import com.dleibovych.epictale.fragment.GameFragment
import com.dleibovych.epictale.fragment.Refreshable
import com.dleibovych.epictale.login.LoginActivity
import com.dleibovych.epictale.util.HistoryStack
import com.dleibovych.epictale.util.PreferencesManager
import com.dleibovych.epictale.util.TextToSpeechUtils
import com.dleibovych.epictale.util.UiUtils
import com.dleibovych.epictale.util.onscreen.OnscreenPart
import org.thetale.api.models.GameInfo

import javax.inject.Inject


class MainActivity : AppCompatActivity(),
        GameNavigation,
        GameView {

    @Inject
    lateinit var navigationProvider: GameNavigationProvider
    @Inject
    lateinit var presenter: GamePresenter


    /**
     * Used to store the last screen title. For use in [.restoreActionBar].
     */
    private var mTitle: CharSequence? = null
    private var currentItem: DrawerItem? = null
    private var history: HistoryStack<DrawerItem>? = null
    var isPaused: Boolean = false
        private set

    var menu: Menu? = null
        private set

    private var accountNameTextView: TextView? = null
    private var timeTextView: TextView? = null
    private var drawerItemInfoView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        TheTaleClientApplication
                .getComponentProvider()
                .gameComponent!!
                .inject(this)

        navigationProvider.navigation = this

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTitle = title

        drawerItemInfoView = findViewById(DrawerItem.PROFILE.viewResId)

        history = HistoryStack(DrawerItem.values().size)
//        onNavigationDrawerItemSelected(DrawerItem.values()[tabIndex])
    }

    override fun onStart() {
        super.onStart()

        if (PreferencesManager.isReadAloudConfirmed()) {
            TextToSpeechUtils.init(TheTaleClientApplication.getContext(), null)
        }
    }

    override fun onResume() {
        super.onResume()
        isPaused = false

//        if (PreferencesManager.shouldExit()) {
//            PreferencesManager.setShouldExit(false)
//            finish()
//        }
//
//        var tabIndex = -1
//        if (intent != null) {
//            if (intent.hasExtra(KEY_GAME_TAB_INDEX)) {
////                onNavigationDrawerItemSelected(DrawerItem.GAME)
//                tabIndex = intent.getIntExtra(KEY_GAME_TAB_INDEX, GameFragment.GamePage.GAME_INFO.ordinal)
//                intent.removeExtra(KEY_GAME_TAB_INDEX)
//            }
//
//            if (intent.getBooleanExtra(KEY_SHOULD_RESET_WATCHING_ACCOUNT, false)) {
//                PreferencesManager.setWatchingAccount(0, null)
//                intent.removeExtra(KEY_SHOULD_RESET_WATCHING_ACCOUNT)
//            }
//        }

//        val fragment = supportFragmentManager.findFragmentByTag(currentItem!!.fragmentTag)
//        if (tabIndex != -1) {
//            val gamePage = GameFragment.GamePage.values()[tabIndex]
//            if (fragment is GameFragment) {
//                fragment.setCurrentPage(gamePage)
//            } else {
//                PreferencesManager.setDesiredGamePage(gamePage)
//            }
//        }
//        UiUtils.callOnscreenStateChange(fragment, true)
//
//        TheTaleClientApplication.getOnscreenStateWatcher().onscreenStateChange(OnscreenPart.MAIN, true)
    }

    override fun onPause() {
        isPaused = true

        TheTaleClientApplication.getOnscreenStateWatcher().onscreenStateChange(OnscreenPart.MAIN, false)
        TextToSpeechUtils.pause()
        RequestCacheManager.invalidate()
        UiUtils.callOnscreenStateChange(supportFragmentManager.findFragmentByTag(currentItem!!.fragmentTag), false)

        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        isPaused = true
        super.onSaveInstanceState(outState)

        outState.putInt(KEY_DRAWER_TAB_INDEX, currentItem!!.ordinal)
    }

    override fun onDestroy() {
        super.onDestroy()

        TextToSpeechUtils.destroy()

        navigationProvider.navigation = null
        if (isFinishing) {
            presenter.dispose()
            TheTaleClientApplication.getComponentProvider().gameComponent = null
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

//    override fun onNavigationDrawerItemSelected(item: DrawerItem) {
//        if (item != currentItem) {
//            when (item) {
//                DrawerItem.PROFILE -> DialogUtils.showChoiceDialog(supportFragmentManager, getString(R.string.drawer_title_site),
//                        arrayOf(getString(R.string.drawer_dialog_profile_item_keeper), getString(R.string.drawer_dialog_profile_item_hero)))
//                { position ->
//                    val accountId = PreferencesManager.getAccountId()
//
//                    when (position) {
//                        0 -> startActivity(UiUtils.getOpenLinkIntent(String.format(WebsiteUtils.URL_PROFILE_KEEPER, accountId)))
//
//                        1 -> startActivity(UiUtils.getOpenLinkIntent(String.format(WebsiteUtils.URL_PROFILE_HERO, accountId)))
//
//                        else -> if (!isPaused) {
//                            DialogUtils.showCommonErrorDialog(supportFragmentManager, this@MainActivity)
//                        }
//                    }
//                }
//
//                DrawerItem.SITE -> startActivity(UiUtils.getOpenLinkIntent(WebsiteUtils.URL_GAME))
//
//                DrawerItem.LOGOUT -> {
//                    PreferencesManager.setSession("")
//
//                    val fragment = supportFragmentManager.findFragmentByTag(currentItem!!.fragmentTag)
//                    if (fragment is WrapperFragment) {
//                        fragment.setMode(DataViewMode.LOADING)
//                    }
//
//                    presenter.logout()
//                }
//
//                DrawerItem.ABOUT -> DialogUtils.showAboutDialog(supportFragmentManager)
//
//                else -> {
//                    val fragmentManager = supportFragmentManager
//                    var oldFragment: Fragment? = fragmentManager.findFragmentByTag(item.fragmentTag)
//                    if (oldFragment == null) {
//                        oldFragment = item.fragment
//                        fragmentManager.beginTransaction()
//                                .replace(R.id.container, oldFragment, item.fragmentTag)
//                                .commit()
//                    } else if (oldFragment.isDetached) {
//                        fragmentManager.beginTransaction()
//                                .attach(oldFragment)
//                                .commit()
//                    }
//
//                    if (currentItem != null) {
//                        UiUtils.callOnscreenStateChange(supportFragmentManager.findFragmentByTag(currentItem!!.fragmentTag), false)
//                    }
//                    UiUtils.callOnscreenStateChange(oldFragment, true)
//
//                    currentItem = item
//                    mTitle = getString(currentItem!!.titleResId)
//                    history!!.set(currentItem)
//                    invalidateOptionsMenu()
//                }
//            }
//        }
//    }

    private fun restoreActionBar() {
        val actionBar = supportActionBar
        actionBar!!.setDisplayShowTitleEnabled(true)
        actionBar.title = mTitle
    }

    private fun refresh() {
        onRefreshStarted()
        val fragment = supportFragmentManager.findFragmentByTag(currentItem!!.fragmentTag)
        if (fragment is Refreshable) {
            (fragment as Refreshable).refresh(true)
        }
    }

    fun refreshGameAdjacentFragments() {
        if (currentItem == DrawerItem.GAME) {
            val fragment = supportFragmentManager.findFragmentByTag(currentItem!!.fragmentTag)
            if (fragment is GameFragment) {
                fragment.refreshAdjacentFragments()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                refresh()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("InflateParams")
    fun onRefreshStarted() {
//        if (menu != null) {
//            val itemRefresh = menu!!.findItem(R.id.action_refresh)
//            if (itemRefresh != null) {
//                itemRefresh.actionView = layoutInflater.inflate(R.layout.menu_progress, null)
//            }
//        }
    }

    fun onRefreshFinished() {
//        if (menu != null) {
//            val itemRefresh = menu!!.findItem(R.id.action_refresh)
//            if (itemRefresh != null) {
//                itemRefresh.actionView = null
//            }
//        }
    }

    fun onDataRefresh() {
        drawerItemInfoView!!.visibility = View.VISIBLE
        UiUtils.setText(accountNameTextView, PreferencesManager.getAccountName())

        presenter.reload()
    }

    override fun setGameInfo(info: GameInfo) {
        UiUtils.setText(timeTextView, String.format("%s %s", info.turn.verboseDate, info.turn.verboseTime))
    }

    override fun showError() {
        UiUtils.setText(timeTextView, null)
    }

    override fun showLogin() {
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }

    companion object {

        const val KEY_GAME_TAB_INDEX = "KEY_GAME_TAB_INDEX"
        const val KEY_SHOULD_RESET_WATCHING_ACCOUNT = "KEY_SHOULD_RESET_WATCHING_ACCOUNT"

        private const val KEY_DRAWER_TAB_INDEX = "KEY_DRAWER_TAB_INDEX"
    }

}