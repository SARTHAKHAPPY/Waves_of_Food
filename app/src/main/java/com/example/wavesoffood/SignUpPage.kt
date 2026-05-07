package com.example.wavesoffood

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wavesoffood.DataClass.UserModel
import com.example.wavesoffood.databinding.ActivitySignUpPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val binding: ActivitySignUpPageBinding by lazy {
        ActivitySignUpPageBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.createAccountBtn.setOnClickListener {

            val name = binding.inputName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createAccount(name, email, password)
        }

        binding.alreadyHaveBtn.setOnClickListener {
            startActivity(Intent(this, LoginPage::class.java))
        }
    }

    private fun createAccount(name: String, email: String, password: String) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                val userId = auth.currentUser!!.uid

                // ✅ FIXED: "user" → "users"
                val user = UserModel(name, email, password)
                database.reference.child("users").child(userId).setValue(user)

                Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, LoginPage::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Signup Failed", Toast.LENGTH_SHORT).show()
                Log.e("Signup", "Error", it)
            }
    }
}