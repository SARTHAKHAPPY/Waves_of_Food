package com.example.wavesoffood.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.wavesoffood.DataClass.UserModel
import com.example.wavesoffood.LoginPage
import com.example.wavesoffood.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    // ✅ FIXED: proper binding pattern (was using wrong layoutInflater)
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUserData()

        binding.apply {

            // ✅ FIXED: logout now goes to LoginPage instead of SignUpPage
            logoutBtn.setOnClickListener {
                auth.signOut()
                val intent = Intent(requireContext(), LoginPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

            saveInfoBtn.setOnClickListener {
                val name = profileEdName.text.toString()
                val email = profileEdEmail.text.toString()
                val address = profileEdAddress.text.toString()
                val phone = profileEdPhone.text.toString()
                updateUserData(name, email, address, phone)
            }

            profileEdName.isEnabled = false
            profileEdEmail.isEnabled = false
            profileEdAddress.isEnabled = false
            profileEdPhone.isEnabled = false

            editProfileBtn.setOnClickListener {
                profileEdName.isEnabled = !profileEdName.isEnabled
                profileEdEmail.isEnabled = !profileEdEmail.isEnabled
                profileEdAddress.isEnabled = !profileEdAddress.isEnabled
                profileEdPhone.isEnabled = !profileEdPhone.isEnabled
                profileEdName.requestFocus()
            }
        }
    }

    private fun updateUserData(name: String, email: String, address: String, phone: String) {
        val userId = auth.currentUser?.uid ?: return

        // ✅ FIXED: "user" → "users"
        val userReference = database.getReference("users").child(userId)
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "address" to address,
            "phone" to phone
        )

        userReference.setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Profile Update Failed!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid ?: return

        // ✅ FIXED: "user" → "users"
        val userReference = database.getReference("users").child(userId)

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                if (snapshot.exists()) {
                    val userProfile = snapshot.getValue(UserModel::class.java)
                    if (userProfile != null) {
                        binding.profileEdName.setText(userProfile.name)
                        binding.profileEdEmail.setText(userProfile.email)
                        binding.profileEdAddress.setText(userProfile.address)
                        binding.profileEdPhone.setText(userProfile.phone)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}