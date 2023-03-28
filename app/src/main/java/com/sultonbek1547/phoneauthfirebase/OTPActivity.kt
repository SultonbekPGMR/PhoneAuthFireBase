package com.sultonbek1547.phoneauthfirebase

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.sultonbek1547.phoneauthfirebase.databinding.ActivityOtpactivityBinding
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {
    private val binding by lazy { ActivityOtpactivityBinding.inflate(layoutInflater) }

    private lateinit var auth: FirebaseAuth
    private lateinit var OTP: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
        phoneNumber = intent.getStringExtra("phoneNumber").toString()

        auth = FirebaseAuth.getInstance()
        binding.progressBar.visibility = View.INVISIBLE
        addTextChangeListener()
        resendOTPtvVisibility()


        binding.btnVerify.setOnClickListener {
//            collect otp from  the editTexts
            binding.apply {
                val typeOTP = (inputOTP1.text.toString()
                        + inputOTP2.text.toString()
                        + inputOTP3.text.toString()
                        + inputOTP4.text.toString()
                        + inputOTP5.text.toString()
                        + inputOTP6.text.toString())

                if (typeOTP.isNotEmpty()) {
                    if (typeOTP.length == 6) {

                        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                            OTP, typeOTP
                        )
                        binding.progressBar.visibility = View.VISIBLE
                        signInWithPhoneAuthCredential(credential)

                    } else {
                        Toast.makeText(
                            this@OTPActivity,
                            "Please enter correct OTP",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@OTPActivity, "Please enter OTP", Toast.LENGTH_SHORT).show()
                }
            }


        }

        binding.tvResend.setOnClickListener {
            resendVerificationCode()
            resendOTPtvVisibility()
        }

    }

    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)// OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendOTPtvVisibility(){
        binding.inputOTP1.setText("")
        binding.inputOTP2.setText("")
        binding.inputOTP3.setText("")
        binding.inputOTP4.setText("")
        binding.inputOTP5.setText("")
        binding.inputOTP6.setText("")
        binding.tvResend.visibility  =View.INVISIBLE
        binding.tvResend.isEnabled = false
        Handler(Looper.myLooper()!!).postDelayed({
            binding.tvResend.visibility  =View.VISIBLE
            binding.tvResend.isEnabled = true

        },60000)
    }
    private fun addTextChangeListener() {
        binding.apply {
            inputOTP1.addTextChangedListener(EditTextWatcher(inputOTP1))
            inputOTP2.addTextChangedListener(EditTextWatcher(inputOTP2))
            inputOTP3.addTextChangedListener(EditTextWatcher(inputOTP3))
            inputOTP4.addTextChangedListener(EditTextWatcher(inputOTP4))
            inputOTP5.addTextChangedListener(EditTextWatcher(inputOTP5))
            inputOTP6.addTextChangedListener(EditTextWatcher(inputOTP6))

        }
    }

    inner class EditTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {
            val text = p0.toString()
            binding.apply {
                when (view.id) {
                    R.id.inputOTP1 -> if (text.length == 1) inputOTP2.requestFocus()
                    R.id.inputOTP2 -> if (text.length == 1) inputOTP3.requestFocus() else if (text.isEmpty()) inputOTP1.requestFocus()
                    R.id.inputOTP3 -> if (text.length == 1) inputOTP4.requestFocus() else if (text.isEmpty()) inputOTP2.requestFocus()
                    R.id.inputOTP4 -> if (text.length == 1) inputOTP5.requestFocus() else if (text.isEmpty()) inputOTP3.requestFocus()
                    R.id.inputOTP5 -> if (text.length == 1) inputOTP6.requestFocus() else if (text.isEmpty()) inputOTP4.requestFocus()
                    R.id.inputOTP6 -> if (text.isEmpty()) inputOTP5.requestFocus()
                }
            }

        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Toast.makeText(this, "Authenticated Successfully", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.INVISIBLE
                startActivity(Intent(this, MainActivity::class.java))
                //  val user = task.result?.user
            } else {
                // Sign in failed, display a message and update the UI
                Toast.makeText(this, "Wrong verification code", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.INVISIBLE
                Log.d("TAG", "onVerificationFailed: ${task.exception.toString()}")

                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                }
                // Update UI
            }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            OTP = verificationId
            resendToken = token
        }
    }


}