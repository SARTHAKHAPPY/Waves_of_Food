package com.example.wavesoffood.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wavesoffood.DataClass.CartItems
import com.example.wavesoffood.DataClass.FoodModel
import com.example.wavesoffood.DetailsActivity
import com.example.wavesoffood.R
import com.example.wavesoffood.databinding.MenuItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MenuAdapter(
    private var menuList: MutableList<FoodModel>,
    private var context: Context
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    inner class ViewHolder(val binding: MenuItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MenuItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        Log.d("MENU_DEBUG", "onCreateViewHolder called")
        return ViewHolder(binding)
    }

    fun updateList(newList: List<FoodModel>) {
        menuList.clear()
        menuList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = menuList[position]
        Log.d("MENU_DEBUG", "onBindViewHolder position=$position name=${model.foodName}")

        holder.binding.apply {

            menuFoodName.text = model.foodName ?: "No Name"
            menuFoodPrice.text = "₹ ${model.foodPrice ?: "0"}"

            // reset button color every time view is bound
            menuAddToCartBtn.setBackgroundResource(R.drawable.shape)

            Glide.with(context)
                .load(Uri.parse(model.foodImg))
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(menuFoodImg)

            root.setOnClickListener {
                val intent = Intent(context, DetailsActivity::class.java)
                intent.putExtra("menuItemName", model.foodName)
                intent.putExtra("menuItemPrice", model.foodPrice)
                intent.putExtra("menuItemDes", model.foodDes)
                intent.putExtra("menuItemIngredients", model.foodIngredients)
                intent.putExtra("menuItemImg", model.foodImg)
                context.startActivity(intent)
            }

            menuAddToCartBtn.setOnClickListener {
                val userId = auth.currentUser?.uid ?: return@setOnClickListener
                val cartRef = database.child("Users").child(userId).child("Cart")
                val query = cartRef.orderByChild("foodName").equalTo(model.foodName)

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        // ✅ change button color on click in both cases
                        menuAddToCartBtn.setBackgroundResource(R.drawable.un_shape)

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
                                model.foodName, model.foodPrice, model.foodImg,
                                model.foodDes, model.foodIngredients, 1, key
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
        }
    }

    override fun getItemCount(): Int {
        Log.d("MENU_DEBUG", "getItemCount: ${menuList.size}")
        return menuList.size
    }
}