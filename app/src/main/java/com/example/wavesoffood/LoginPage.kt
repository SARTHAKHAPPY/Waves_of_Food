package com.example.wavesoffood

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.wavesoffood.DataClass.UserModel
import com.example.wavesoffood.databinding.ActivityLoginPageBinding
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.Circle
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase

class LoginPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient

    private val binding: ActivityLoginPageBinding by lazy {
        ActivityLoginPageBinding.inflate(layoutInflater)
    }

    private fun loader() {
        val loader = binding.loader as ProgressBar
        val circle: Sprite = Circle()
        loader.indeterminateDrawable = circle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loader()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.loginBtn.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        binding.googleBtn.setOnClickListener {
            googleSignInClient.signOut()
            launcher.launch(googleSignInClient.signInIntent)
        }

        binding.goSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpPage::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {

        binding.loader.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                binding.loader.visibility = View.GONE

                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    val error = task.exception?.message ?: ""
                    when {
                        error.contains("no user record", true) ->
                            Toast.makeText(this, "User not found. Please sign up.", Toast.LENGTH_LONG).show()
                        error.contains("password is invalid", true) ->
                            Toast.makeText(this, "Wrong password", Toast.LENGTH_LONG).show()
                        else ->
                            Toast.makeText(this, "Login failed: $error", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "Google Sign-In Cancelled", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Error: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->

                val user = result.user!!

                // ✅ FIXED: "user" → "users"
                val userRef = database.reference.child("users").child(user.uid)

                userRef.get().addOnSuccessListener {
                    if (!it.exists()) {
                        val newUser = UserModel(
                            user.displayName,
                            user.email,
                            ""
                        )
                        userRef.setValue(newUser)
                    }

                    Toast.makeText(this, "Google Login Success", Toast.LENGTH_SHORT).show()
                    goToHome()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Google Auth Failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            goToHome()
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomePage::class.java))
        finish()
    }
}