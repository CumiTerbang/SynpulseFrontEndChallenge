package com.haryop.synpulsefrontendchallenge.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.haryop.synpulsefrontendchallenge.R
import com.haryop.synpulsefrontendchallenge.databinding.ActivityLandingBinding
import com.haryop.synpulsefrontendchallenge.utils.BaseActivityBinding
import com.haryop.synpulsefrontendchallenge.utils.ConstantsObj
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


interface SignInUpListener {
    fun onReSendVerificationCode()
    fun onSendVerificationCode(phoneNumber: String, navactionid:Int)
    fun onVerifyPhoneNumberWithCode(phoneNumber: String)
}

@AndroidEntryPoint
class LandingActivity : BaseActivityBinding<ActivityLandingBinding>(), SignInUpListener {

    override val bindingInflater: (LayoutInflater) -> ActivityLandingBinding
        get() = ActivityLandingBinding::inflate

    var target_activity = 0
    private lateinit var auth: FirebaseAuth
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var storedVerificationId: String? = ""
    private var phoneNumber: String? = ""
    lateinit var navController: NavController
    override fun setupView(binding: ActivityLandingBinding) {
        auth = FirebaseAuth.getInstance()
        target_activity = intent.getIntExtra(ConstantsObj.KEY_TARGET_ACTIVITY, 0)
        when (target_activity) {
            ConstantsObj.VALUE_TITLESCREEN_ACTIVITY -> setupAction_TitleScreen(binding.root)
            ConstantsObj.VALUE_WELCOMESEARCH_ACTIVITY -> setupAction_WelcomSearch(binding.root)
            ConstantsObj.VALUE_BROWSECOMP_ACTIVITY -> setupAction_InnerPage(
                binding.root,
                R.navigation.browsecomp_nav_graph
            )
            ConstantsObj.VALUE_PROFILE_ACTIVITY -> setupAction_InnerPage(
                binding.root,
                R.navigation.profile_nav_graph
            )
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.e("otp tes", "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.e("otp tes", "onVerificationFailed:${e}")

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.e("otp tes", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
                Log.e("otp tes", "storedVerificationId: $storedVerificationId")
                Log.e("otp tes", "resendToken: $resendToken")
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.e("otp tes", "start sign in")
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = task.result?.user
                    Log.e("otp tes", "signInWithCredential:success | user: "+user)

                    setlogin()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.e(
                        "otp tes",
                        "signInWithCredential:failure ${task.exception}"
                    )
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    fun setupAction_TitleScreen(view: View) = with(binding) {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_landing_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.setGraph(R.navigation.titlescreen_nav_graph)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.titlescreenFragment) {
                landingToolbar.visibility = View.GONE
            } else {
                landingToolbar.visibility = View.VISIBLE
            }
        }

        val appBarConfiguration: AppBarConfiguration = AppBarConfiguration(navController.graph)
        landingToolbar.setupWithNavController(navController, appBarConfiguration)
        landingToolbar.visibility = View.GONE
    }

    fun setupAction_WelcomSearch(view: View) = with(binding) {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_landing_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.setGraph(R.navigation.browsecomp_nav_graph)

        val appBarConfiguration: AppBarConfiguration = AppBarConfiguration(navController.graph)
        landingToolbar.setupWithNavController(navController, appBarConfiguration)
    }

    fun setupAction_InnerPage(view: View, graphid: Int) = with(binding) {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_landing_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.setGraph(graphid)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            landingToolbar.title = destination.label
        }

        setSupportActionBar(landingToolbar);
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);
        landingToolbar.setNavigationOnClickListener {
            if (navController.currentDestination?.id == navController.graph.startDestination) {
                finish()
            } else {
                navController.popBackStack()
            }
        }
    }

    val activityScope = CoroutineScope(Dispatchers.Main)
    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (isTaskRoot && target_activity == ConstantsObj.VALUE_WELCOMESEARCH_ACTIVITY) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }

            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

            activityScope.launch {
                delay(2000)
                doubleBackToExitPressedOnce = false
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onReSendVerificationCode() {
        phoneNumber?.let { resendVerificationCode(it, resendToken) }
    }

    override fun onSendVerificationCode(_phoneNumber: String, navactionid:Int) {
        phoneNumber = "+62" + _phoneNumber
        Log.e("otp tes", "phoneNumber= " + phoneNumber)

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber!!)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        navController.navigate(navactionid)

    }

    override fun onVerifyPhoneNumberWithCode(otp_token: String) {
        var credential = storedVerificationId?.let { PhoneAuthProvider.getCredential(it, otp_token) }
        credential?.let { signInWithPhoneAuthCredential(it) }
    }

    fun setlogin(){
        val sharedPref = getSharedPreferences(packageName, Context.MODE_PRIVATE)
        with (sharedPref?.edit()) {
            this?.putString(com.haryop.synpulsefrontendchallenge.utils.ConstantsObj.KEY_ISLOGIN, "KEY_ISLOGIN")
            this?.commit()
        }

        var islogin = sharedPref?.getString(ConstantsObj.KEY_ISLOGIN, "")
        Log.e("splashscreen", "isLogin: " + islogin)

        var intent = Intent(this, LandingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(ConstantsObj.KEY_TARGET_ACTIVITY, ConstantsObj.VALUE_WELCOMESEARCH_ACTIVITY )
        startActivity(intent)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: ForceResendingToken
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(token) // ForceResendingToken from callbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}