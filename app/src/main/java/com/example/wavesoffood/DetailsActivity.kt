package com.example.wavesoffood

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.wavesoffood.DataClass.CartItems
import com.example.wavesoffood.databinding.ActivityDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetailsActivity : AppCompatActivity() {

    private val binding: ActivityDetailsBinding by lazy {
        ActivityDetailsBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    private var foodName: String = ""
    private var foodPrice: String = ""
    private var foodDes: String = ""
    private var foodIngredients: String = ""
    private var foodImg: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // ✅ GET DATA SAFELY
        foodName = intent.getStringExtra("menuItemName") ?: ""
        foodPrice = intent.getStringExtra("menuItemPrice") ?: ""
        foodDes = intent.getStringExtra("menuItemDes") ?: ""
        foodIngredients = intent.getStringExtra("menuItemIngredients") ?: ""
        foodImg = intent.getStringExtra("menuItemImg") ?: ""

        // ✅ SET UI
        binding.detailsFoodName.text = foodName
        binding.detailsFoodDescription.text = "$foodName\nPrice: ₹$foodPrice\n$foodDes"
        binding.detailsFoodIngredients.text = foodIngredients

        Glide.with(this)
            .load(Uri.parse(foodImg))
            .into(binding.detailsFoodImg)

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.addToCartBtn.setOnClickListener {
            addItemToCart()
        }
    }

    private fun addItemToCart() {

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        val cartRef = database
            .child("Users")
            .child(userId)
            .child("Cart")

        cartRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                var itemFound = false

                for (cartSnapshot in snapshot.children) {

                    val existingItem = cartSnapshot.getValue(CartItems::class.java)

                    // ✅ IF EXISTS → UPDATE QUANTITY
                    if (existingItem != null && existingItem.foodName == foodName) {

                        val newQuantity = existingItem.quantity + 1

                        cartSnapshot.ref.child("quantity")
                            .setValue(newQuantity)

                        Toast.makeText(
                            this@DetailsActivity,
                            "Quantity Updated",
                            Toast.LENGTH_SHORT
                        ).show()

                        itemFound = true
                        break
                    }
                }

                // ✅ IF NOT EXISTS → ADD NEW
                if (!itemFound) {

                    val key = cartRef.push().key ?: return

                    val item = CartItems(
                        foodName = foodName,
                        foodPrice = foodPrice,
                        foodImg = foodImg,
                        foodDes = foodDes,
                        foodIngredients = foodIngredients,
                        quantity = 1,
                        itemKey = key
                    )

                    cartRef.child(key).setValue(item)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@DetailsActivity,
                                "Added to Cart",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@DetailsActivity,
                                "Failed to Add",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@DetailsActivity,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}