package com.example.wavesoffood.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wavesoffood.DataClass.CartItems
import com.example.wavesoffood.databinding.CartItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CartAdapter(
    private var context: Context,
    private var cartList: ArrayList<CartItems>
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    inner class ViewHolder(val binding: CartItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CartItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = cartList[position]

        holder.binding.apply {

            // ✅ MATCHED WITH YOUR XML IDS
            cartFoodName.text = item.foodName ?: ""
            cartFoodPrice.text = "₹ ${item.foodPrice ?: "0"}"
            itemQuantity.text = item.quantity.toString()

            Glide.with(context)
                .load(item.foodImg)
                .into(cartFoodImg)

            // ➕ Increase
            plusBtn.setOnClickListener {
                val newQty = item.quantity + 1
                item.quantity = newQty
                itemQuantity.text = newQty.toString()
                updateQuantity(item)
            }

            // ➖ Decrease
            minusBtn.setOnClickListener {
                if (item.quantity > 1) {
                    val newQty = item.quantity - 1
                    item.quantity = newQty
                    itemQuantity.text = newQty.toString()
                    updateQuantity(item)
                }
            }

            // 🗑 Delete
            deleteBtn.setOnClickListener {

                val pos = holder.adapterPosition   // ✅ THIS WILL WORK

                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                val userId = auth.currentUser?.uid ?: return@setOnClickListener
                val key = item.itemKey ?: return@setOnClickListener

                database.child("Users")
                    .child(userId)
                    .child("Cart")
                    .child(key)
                    .removeValue()
                    .addOnSuccessListener {

                        if (pos < cartList.size) {
                            cartList.removeAt(pos)
                            notifyItemRemoved(pos)
                        }

                        Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun getItemCount(): Int = cartList.size

    private fun updateQuantity(item: CartItems) {

        val userId = auth.currentUser?.uid ?: return
        val key = item.itemKey ?: return

        database.child("Users")
            .child(userId)
            .child("Cart")
            .child(key)
            .child("quantity")
            .setValue(item.quantity)
    }
}