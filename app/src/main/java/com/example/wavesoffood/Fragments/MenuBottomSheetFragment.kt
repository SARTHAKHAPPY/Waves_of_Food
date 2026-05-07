package com.example.wavesoffood.Fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wavesoffood.Adapter.MenuAdapter
import com.example.wavesoffood.DataClass.FoodModel
import com.example.wavesoffood.databinding.FragmentMenuBottomSheetBinding
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.Circle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.*

class MenuBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentMenuBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: MenuAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            ) ?: return@setOnShowListener

            // get screen height and force the sheet to that height
            val screenHeight = resources.displayMetrics.heightPixels
            bottomSheet.layoutParams.height = screenHeight
            bottomSheet.requestLayout()

            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.peekHeight = screenHeight
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isDraggable = false
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBottomSheetBinding.inflate(inflater, container, false)

        binding.backBtn.setOnClickListener { dismiss() }

        setupLoader()
        setupRecycler()
        loadMenu()

        return binding.root
    }

    private fun setupRecycler() {
        adapter = MenuAdapter(mutableListOf(), requireContext())
        binding.rvMenu.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMenu.setHasFixedSize(false)
        binding.rvMenu.adapter = adapter
    }

    private fun setupLoader() {
        val progressBar = binding.loader2 as ProgressBar
        val circle: Sprite = Circle()
        progressBar.indeterminateDrawable = circle
        binding.loader2.visibility = View.VISIBLE
    }

    private fun loadMenu() {
        database = FirebaseDatabase.getInstance()
        val foodRef = database.reference.child("Menu")

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return

                val fetchedList = ArrayList<FoodModel>()
                for (foodSnapshot in snapshot.children) {
                    val item = foodSnapshot.getValue(FoodModel::class.java)
                    item?.let { fetchedList.add(it) }
                }

                Log.d("MENU_DEBUG", "Items fetched: ${fetchedList.size}")

                binding.loader2.visibility = View.GONE
                adapter.updateList(fetchedList)

                Log.d("MENU_DEBUG", "Adapter count after update: ${adapter.itemCount}")
                Log.d("MENU_DEBUG", "RV height after update: ${binding.rvMenu.height}")
            }

            override fun onCancelled(error: DatabaseError) {
                if (_binding == null) return
                binding.loader2.visibility = View.GONE
                Log.e("MENU_DEBUG", "Error: ${error.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}