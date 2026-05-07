package com.example.wavesoffood.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wavesoffood.Adapter.CartAdapter
import com.example.wavesoffood.DataClass.CartItems
import com.example.wavesoffood.PayOutActivity
import com.example.wavesoffood.databinding.FragmentCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val cartList = ArrayList<CartItems>()
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCartBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        loadCart()

        binding.proceedBtn.setOnClickListener {

            if (cartList.isEmpty()) {
                Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalAmount = cartList.sumOf {
                (it.foodPrice?.toIntOrNull() ?: 0) * it.quantity
            }

            val intent = Intent(requireContext(), PayOutActivity::class.java)
            intent.putExtra("totalAmount", totalAmount)
            startActivity(intent)
        }

        return binding.root
    }

    private fun setupRecyclerView() {

        // ✅ FIXED PARAMETER ORDER
        adapter = CartAdapter(requireContext(), cartList)

        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter
    }

    private fun loadCart() {

        val userId = auth.currentUser?.uid ?: return

        database.child("Users")
            .child(userId)
            .child("Cart")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (_binding == null) return

                    cartList.clear()

                    for (cartSnapshot in snapshot.children) {
                        val item = cartSnapshot.getValue(CartItems::class.java)
                        item?.let { cartList.add(it) }
                    }

                    adapter.notifyDataSetChanged()

                    binding.loader.visibility = View.GONE

                    if (cartList.isEmpty()) {
                        binding.emptyCart.visibility = View.VISIBLE
                        binding.rvCart.visibility = View.GONE
                    } else {
                        binding.emptyCart.visibility = View.GONE
                        binding.rvCart.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (_binding == null) return
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}