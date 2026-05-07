package com.example.wavesoffood.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wavesoffood.DataClass.CartItems
import com.example.wavesoffood.R
import com.example.wavesoffood.databinding.BuyAgainItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BuyAgainAdapter(
    private var againFoodName: MutableList<String>,
    private var againFoodPrice: MutableList<String>,
    private var againFoodImg: MutableList<String>,
    var context: Context
) : RecyclerView.Adapter<BuyAgainAdapter.viewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    inner class viewHolder(val binding: BuyAgainItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(foodName: String, foodPrice: String, foodImg: String) {
            binding.apply {
                buyAgainFoodName.text = foodName
                buyAgainFoodPrice.text = foodPrice

                Glide.with(context)
                    .load(Uri.parse(foodImg))
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(buyAgainFoodImg)

                // reset button color
                buyAgainFoodBtn.setBackgroundResource(R.drawable.addtocartshape)

                buyAgainFoodBtn.setOnClickListener {
                    addToCart(foodName, foodPrice, foodImg)
                    // ✅ change button color on click like other add to cart buttons
                    buyAgainFoodBtn.setBackgroundResource(R.drawable.un_shape)
                }
            }
        }
    }

    private fun addToCart(foodName: String, foodPrice: String, foodImg: String) {
        val userId = auth.currentUser?.uid ?: return
        val cartRef = database.child("Users").child(userId).child("Cart")

        // check if item already exists in cart
        cartRef.orderByChild("foodName").equalTo(foodName)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // item exists → update quantity
                        for (child in snapshot.children) {
                            val item = child.getValue(CartItems::class.java)
                            val key = child.key ?: continue
                            val newQty = (item?.quantity ?: 0) + 1
                            cartRef.child(key).child("quantity").setValue(newQty)
                        }
                        Toast.makeText(context, "Quantity Updated", Toast.LENGTH_SHORT).show()
                    } else {
                        // item doesn't exist → add new
                        val key = cartRef.push().key ?: return
                        val cartItem = CartItems(
                            foodName = foodName,
                            foodPrice = foodPrice,
                            foodImg = foodImg,
                            foodDes = "",
                            foodIngredients = "",
                            quantity = 1,
                            itemKey = key
                        )
                        cartRef.child(key).setValue(cartItem)
                        Toast.makeText(context, "Added to Cart", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding = BuyAgainItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(
            againFoodName[position],
            againFoodPrice[position],
            againFoodImg[position]
        )
    }

    override fun getItemCount(): Int = againFoodName.size
}