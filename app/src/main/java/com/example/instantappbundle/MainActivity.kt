package com.example.instantappbundle

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.play.core.splitinstall.*
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity(), View.OnClickListener {

    companion object {
        private const val CONFIRMATION_REQUEST_CODE = 1
        private const val PACKAGE_NAME = "com.example.dynamicfeature"
        private const val ABOUT_CLASSNAME = "$PACKAGE_NAME.AboutUsActivity"
        private const val ANNOUNCEMENT_CLASSNAME = "$PACKAGE_NAME.AnnouncementActivity"
        private const val INSTATAPPURL_CLASSNAME = "$PACKAGE_NAME.InstantAppActivity"
        private const val INSTATAPPSPLIT_CLASSNAME = "$PACKAGE_NAME.InstantSplitActivity"
    }

    private lateinit var manager: SplitInstallManager


    private val moduleAboutUs by lazy { getString(R.string.module_feature_aboutus) }
    private val moduleAnnouncement by lazy { getString(R.string.module_feature_announcement) }
    private val moduleSplitInstant by lazy { getString(R.string.module_feature_splitinstant) }
    private val moduleInstantAppByUrl by lazy { getString(R.string.module_feature_instant_url) }

    private val installableModules by lazy { listOf(moduleAboutUs, moduleAnnouncement) }

    /** Listener used to handle changes in state for install requests. */
    private val listener = SplitInstallStateUpdatedListener { state ->
        val names = state.moduleNames().joinToString(" - ")

        when (state.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                //  In order to see this, the application has to be uploaded to the Play Store.
                displayLoadingState(state, getString(R.string.downloading, names))
            }
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                /*
                  This may occur when attempting to download a sufficiently large module.
                  In order to see this, the application has to be uploaded to the Play Store.
                  Then features can be requested until the confirmation path is triggered.
                 */
                manager.startConfirmationDialogForResult(state, this, CONFIRMATION_REQUEST_CODE)
            }
            SplitInstallSessionStatus.INSTALLED -> {
//                onSuccessfulLoad(names, launch = true)
            }

            SplitInstallSessionStatus.INSTALLING -> displayLoadingState(
                state, getString(R.string.installing, names)
            )
            SplitInstallSessionStatus.FAILED -> {
                toastAndLog(
                    getString(
                        R.string.error_for_module, state.errorCode(),
                        state.moduleNames()
                    )
                )
            }
            SplitInstallSessionStatus.CANCELED -> {

            }
            SplitInstallSessionStatus.CANCELING -> {

            }
            SplitInstallSessionStatus.DOWNLOADED -> {

            }
            SplitInstallSessionStatus.PENDING -> {

            }
            SplitInstallSessionStatus.UNKNOWN -> {

            }
        }
    }


    private fun onSuccessfulLoad(moduleName: String, launch: Boolean) {
        if (launch) {
            when (moduleName) {
                moduleAboutUs -> launchActivity(ABOUT_CLASSNAME)
                moduleAnnouncement -> launchActivity(ANNOUNCEMENT_CLASSNAME)
                moduleInstantAppByUrl -> launchActivity(INSTATAPPURL_CLASSNAME)
                moduleSplitInstant -> launchActivity(INSTATAPPSPLIT_CLASSNAME)
            }
        }
    }

    private fun launchActivity(className: String) {
        val intent = Intent().setClassName(BuildConfig.APPLICATION_ID, className)
        startActivity(intent)
    }

    private fun displayLoadingState(state: SplitInstallSessionState?, message: String) {
        displayProgress()
        updateProgressMessage(message)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        handleIntent(intent)

        setContentView(R.layout.activity_main)
        manager = SplitInstallManagerFactory.create(this)
        initViews()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
//        val appLinkIntent = intent
//        val appLinkAction = appLinkIntent.action
//        val appLinkData = appLinkIntent.data
//        toastAndLog(appLinkIntent.toString())
    }

    private fun initViews() {
        btn_load_aboutus.setOnClickListener(this)
        btn_load_announcement.setOnClickListener(this)
        btn_install_all_now.setOnClickListener(this)
        btn_request_uninstall.setOnClickListener(this)
        btn_instant_dynamic_feature_split_install.setOnClickListener(this)
        btn_instant_dynamic_feature_url_load.setOnClickListener(this)
    }

    override fun onResume() {
        // Listener can be registered even without directly triggering a download.
        manager.registerListener(listener)
        super.onResume()
    }

    override fun onPause() {
        // Make sure to dispose of the listener once it's no longer needed.
        manager.unregisterListener(listener)
        super.onPause()
    }

    /** This is needed to handle the result of the manager.startConfirmationDialogForResult request */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CONFIRMATION_REQUEST_CODE) {
            // Handle the user's decision. For example, if the user selects "Cancel",
            // you may want to disable certain functionality that depends on the module.
            if (resultCode == Activity.RESULT_CANCELED) {
                toastAndLog(getString(R.string.user_cancelled))
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_load_aboutus -> loadAndLaunchModule(moduleAboutUs)
            R.id.btn_load_announcement -> loadAndLaunchModule(moduleAnnouncement)
            R.id.btn_install_all_now -> installAllFeaturesNow()
            R.id.btn_request_uninstall -> requestUninstall()
            R.id.btn_instant_dynamic_feature_split_install -> loadAndLaunchModule(moduleSplitInstant)
            R.id.btn_instant_dynamic_feature_url_load -> openUrl(moduleInstantAppByUrl)

        }
    }

    private fun loadAndLaunchModule(moduleName: String) {
        updateProgressMessage(getString(R.string.loading_module, moduleName))
        // Skip loading if the module already is installed. Perform success action directly.
        if (manager.installedModules.contains(moduleName)) {
            updateProgressMessage(getString(R.string.already_installed))
            onSuccessfulLoad(moduleName, launch = true)
            return
        }

        // Create request to install a feature module by name.
        val request = SplitInstallRequest.newBuilder()
            .addModule(moduleName)
            .build()

        // Load and install the requested feature module.
        manager.startInstall(request)
            .addOnSuccessListener {
                Log.e(TAG, "main: addOnSuccessListener")
            }
            .addOnFailureListener {
                Log.e(TAG, "main: addOnFailureListener")
            }
            .addOnCompleteListener {
                Log.e(TAG, "main: addOnCompleteListener")
            }

        updateProgressMessage(getString(R.string.starting_install_for, moduleName))
    }

    /** Install all features but do not launch any of them. */
    private fun installAllFeaturesNow() {
        // Request all known modules to be downloaded in a single session.
        val requestBuilder = SplitInstallRequest.newBuilder()

        installableModules.forEach { name ->
            if (!manager.installedModules.contains(name)) {
                requestBuilder.addModule(name)
            }
        }

        val request = requestBuilder.build()

        manager.startInstall(request).addOnSuccessListener {
            toastAndLog("Loading ${request.moduleNames}")
        }.addOnFailureListener {
            toastAndLog("Failed loading ${request.moduleNames}")
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .setPackage(BuildConfig.APPLICATION_ID)
            .addCategory(Intent.CATEGORY_BROWSABLE)
        startActivity(intent)
    }


    private fun requestUninstall() {
        toastAndLog(
            "Requesting uninstall of all modules." +
                    "This will happen at some point in the future."
        )

        val installedModules = manager.installedModules.toList()
        manager.deferredUninstall(installedModules).addOnSuccessListener {
            toastAndLog("Uninstalling $installedModules")
        }.addOnFailureListener {
            toastAndLog("Failed installation of $installedModules")
        }
    }

    private fun updateProgressMessage(message: String) {
        if (progress_bar.visibility != View.VISIBLE) displayProgress()
        progress_text.text = message
    }

    /** Display progress bar and text. */
    private fun displayProgress() {
        //TODO Make it visible later
        progress_bar.visibility = View.GONE
    }
}
