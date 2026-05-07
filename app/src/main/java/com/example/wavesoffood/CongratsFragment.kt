package com.example.wavesoffood

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wavesoffood.databinding.FragmentCongratsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CongratsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCongratsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCongratsBinding.inflate(inflater, container, false)

        binding.goToHome.setOnClickListener {

            // ✅ Close bottom sheet
            dismiss()

            // ✅ Go to home
            val intent = Intent(requireContext(), HomePage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}