package com.example.wavesoffood.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wavesoffood.DataClass.CartItems
import com.example.wavesoffood.DataClass.FoodModel
import com.example.wavesoffood.DetailsActivity
import com.example.wavesoffood.R
import com.example.wavesoffood.databinding.PopularItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PopularAdapter(
    private val list: ArrayList<FoodModel>,
    private val context: Context
) : RecyclerView.Adapter<PopularAdapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    inner class ViewHolder(val binding: PopularItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PopularItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        holder.binding.apply {

            foodName.text = model.foodName ?: "No Name"
            foodPrice.text = "₹ ${model.foodPrice ?: "0"}"

            // reset button color every time view is bound
            addToCartBtn.setBackgroundResource(R.drawable.shape)

            if (!model.foodImg.isNullOrEmpty()) {
                Glide.with(context)
                    .load(Uri.parse(model.foodImg))
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(foodImg)
            }

            root.setOnClickListener {
                val intent = Intent(context, DetailsActivity::class.java)
                intent.putExtra("menuItemName", model.foodName)
                intent.putExtra("menuItemPrice", model.foodPrice)
                intent.putExtra("menuItemDes", model.foodDes)
                intent.putExtra("menuItemIngredients", model.foodIngredients)
                intent.putExtra("menuItemImg", model.foodImg)
                context.startActivity(intent)
            }

            addToCartBtn.setOnClickListener {
                val userId = auth.currentUser?.uid ?: return@setOnClickListener
                val cartRef = database.child("Users").child(userId).child("Cart")
                val query = cartRef.orderByChild("foodName").equalTo(model.foodName)

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        // ✅ change button color on click in both cases
                        addToCartBtn.setBackgroundResource(R.drawable.un_shape)

                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val item = child.getValue(CartItems::class.java)
                                val key = child.key ?: continue
                                val newQty = (item?.quantity ?: 0) + 1
                                cartRef.child(key).child("quantity").setValue(newQty)
                            }
                            // ✅ quantity update message kept
                            Toast.makeText(context, "Quantity Updated", Toast.LENGTH_SHORT).show()
                        } else {
                            val key = cartRef.push().key ?: return
                            val cartItem = CartItems(
                                foodName = model.foodName,
                                foodPrice = model.foodPrice,
                                foodImg = model.foodImg,
                                foodDes = model.foodDes,
                                foodIngredients = model.foodIngredients,
                                quantity = 1,
                                itemKey = key
                            )
                            cartRef.child(key).setValue(cartItem)
                            Toast.makeText(context, "Added to Cart", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }

    override fun getItemCount(): Int = list.size
}