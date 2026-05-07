package com.example.wavesoffood.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wavesoffood.Adapter.MenuAdapter
import com.example.wavesoffood.DataClass.FoodModel
import com.example.wavesoffood.databinding.FragmentSearchBinding
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.Circle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: MenuAdapter
    private lateinit var database: FirebaseDatabase

    private var originalMenuList = mutableListOf<FoodModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        // ✅ create adapter once with empty list and attach immediately
        // so RecyclerView always has an adapter from the start
        adapter = MenuAdapter(mutableListOf(), requireContext())
        binding.rvSearchMenu.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchMenu.adapter = adapter

        loader2()
        binding.loader2.visibility = View.VISIBLE

        retrieveMenuItem()
        setUpSearchView()

        return binding.root
    }

    private fun retrieveMenuItem() {
        database = FirebaseDatabase.getInstance()
        val foodReference: DatabaseReference = database.reference.child("Menu")

        foodReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                originalMenuList.clear()

                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(FoodModel::class.java)
                    menuItem?.let { originalMenuList.add(it) }
                }

                binding.loader2.visibility = View.GONE

                // ✅ use updateList() to push data into the existing adapter
                adapter.updateList(originalMenuList)
            }

            override fun onCancelled(error: DatabaseError) {
                binding.loader2.visibility = View.GONE
            }
        })
    }

    private fun setUpSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterMenuItems(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMenuItems(newText)
                return true
            }
        })
    }

    private fun filterMenuItems(query: String?) {
        // ✅ use updateList() instead of creating a new adapter every keypress
        val filtered = if (query.isNullOrEmpty()) {
            originalMenuList
        } else {
            originalMenuList.filter {
                it.foodName?.contains(query, ignoreCase = true) == true
            }
        }
        adapter.updateList(filtered)
    }

    private fun loader2() {
        val progressBar = binding.loader2 as ProgressBar
        val circle: Sprite = Circle()
        progressBar.indeterminateDrawable = circle
    }
}