package com.haryop.synpulsefrontendchallenge.ui.titlescreen

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import com.haryop.synpulsefrontendchallenge.R
import com.haryop.synpulsefrontendchallenge.databinding.FragmentOtpBinding
import com.haryop.synpulsefrontendchallenge.ui.LandingActivity
import com.haryop.synpulsefrontendchallenge.utils.BaseFragmentBinding
import com.haryop.synpulsefrontendchallenge.utils.showToast

class OTPFragment : BaseFragmentBinding<FragmentOtpBinding>() {

    var START_MILLI_SECONDS = 60000L //60 seconds

    lateinit var countDown_timer: CountDownTimer
    lateinit var countdown_timer: CountDownTimer
    var isRunning: Boolean = false;
    var time_in_milli_seconds = 0L

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentOtpBinding
        get() = FragmentOtpBinding::inflate

    lateinit var viewbinding: FragmentOtpBinding
    override fun setupView(binding: FragmentOtpBinding) {
        viewbinding = binding

        startTimer(START_MILLI_SECONDS)
        viewbinding.submit.setOnClickListener { onSubmitOTP() }
        viewbinding.resendOtp.setOnClickListener { onResendOTP() }

    }

    fun onResendOTP() = with(viewbinding) {
        startTimer(START_MILLI_SECONDS)
        (activity as LandingActivity).onReSendVerificationCode()

        showToast("Resend OTP")
    }

    fun onSubmitOTP() = with(viewbinding) {
        val otp_code = otpField.text.toString()
        if (otp_code.isNullOrEmpty() || otp_code.equals("")) {
            showToast("Phone number is empty")
            return
        }

        (activity as LandingActivity).onVerifyPhoneNumberWithCode(otp_code)
    }

    fun startTimer(time_in_seconds: Long) = with(viewbinding) {
        countdown_timer = object : CountDownTimer(time_in_seconds, 1000) {
            override fun onFinish() {
                time_in_milli_seconds = 0
                updateTextUI()

                resendOtp.isEnabled = true
                resendOtp.setTextColor(resources.getColor(R.color.orange))
            }

            override fun onTick(p0: Long) {
                time_in_milli_seconds = p0
                updateTextUI()
            }
        }
        countdown_timer.start()

        isRunning = true
        resendOtp.isEnabled = false
        resendOtp.setTextColor(resources.getColor(R.color.light_grey))

    }

    private fun updateTextUI() = with(viewbinding) {
        val minute = (time_in_milli_seconds / 1000) / 60
        val seconds = (time_in_milli_seconds / 1000) % 60

        var _seconds=""+seconds
        var _minute=""+minute

        if(minute<10){
            _minute="0"+minute
        }

        if(seconds<10){
            _seconds="0"+seconds
        }

        timerLabel.text = "$_minute:$_seconds"
    }

}