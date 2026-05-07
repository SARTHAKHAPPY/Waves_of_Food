package com.example.wavesoffood

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wavesoffood.DataClass.CartItems
import com.example.wavesoffood.DataClass.OrderDetails
import com.example.wavesoffood.databinding.ActivityPayOutActitvityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PayOutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPayOutActitvityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val cartList = ArrayList<CartItems>()
    private lateinit var userId: String
    private var totalAmount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPayOutActitvityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        userId = auth.currentUser?.uid ?: ""

        setUserData()
        loadCartData()

        binding.placeOrderBtn.setOnClickListener {
            val name = binding.payOutEdName.text.toString().trim()
            val address = binding.payOutEdAddress.text.toString().trim()
            val phone = binding.payOutEdPhone.text.toString().trim()

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please Enter All Details", Toast.LENGTH_SHORT).show()
            } else {
                // ✅ save whatever user typed back to users/ before placing order
                // so Profile page always has the latest data
                saveUserData(name, address, phone)
                placeOrder(name, address, phone)
            }
        }

        binding.backBtn.setOnClickListener { finish() }
    }

    // ✅ NEW: save name/address/phone back to users/ node
    // so Profile page reflects latest info entered on PayOut page
    private fun saveUserData(name: String, address: String, phone: String) {
        if (userId.isEmpty()) return

        val userRef = database.child("users").child(userId)
        userRef.child("name").setValue(name)
        userRef.child("address").setValue(address)
        userRef.child("phone").setValue(phone)
    }

    private fun loadCartData() {
        if (userId.isEmpty()) return

        database.child("Users")
            .child(userId)
            .child("Cart")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cartList.clear()
                    totalAmount = 0

                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(CartItems::class.java)
                        item?.let {
                            cartList.add(it)
                            val price = it.foodPrice?.toIntOrNull() ?: 0
                            totalAmount += price * it.quantity
                        }
                    }

                    binding.payOutTotalPrice.text = "₹ $totalAmount"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PayOutActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun placeOrder(name: String, address: String, phone: String) {
        if (userId.isEmpty()) return

        val orderKey = database.child("OrderDetails").push().key ?: return

        val orderDetails = OrderDetails(
            userUid = userId,
            userName = name,
            foodNames = ArrayList(cartList.map { it.foodName ?: "" }),
            foodPrices = ArrayList(cartList.map { it.foodPrice ?: "0" }),
            foodImages = ArrayList(cartList.map { it.foodImg ?: "" }),
            foodQty = ArrayList(cartList.map { it.quantity }),
            address = address,
            totalPrice = "₹ $totalAmount",
            phoneNumber = phone,
            currentTime = System.currentTimeMillis(),
            itemPushKey = orderKey,
            orderAccepted = false,
            paymentReceived = false
        )

        database.child("OrderDetails")
            .child(orderKey)
            .setValue(orderDetails)
            .addOnSuccessListener {
                saveToHistory(orderDetails)
                removeCart()
                val bottomSheet = CongratsFragment()
                bottomSheet.show(supportFragmentManager, "Congrats")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Order Failed ❌", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeCart() {
        database.child("Users").child(userId).child("Cart").removeValue()
    }

    private fun saveToHistory(orderDetails: OrderDetails) {
        database.child("Users")
            .child(userId)
            .child("BuyHistory")
            .child(orderDetails.itemPushKey!!)
            .setValue(orderDetails)
    }

    // ✅ auto-fill from users/ node
    private fun setUserData() {
        if (userId.isEmpty()) return

        database.child("users")
            .child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val address = snapshot.child("address").getValue(String::class.java) ?: ""
                    val phone = snapshot.child("phone").getValue(String::class.java) ?: ""

                    binding.payOutEdName.setText(name)
                    binding.payOutEdAddress.setText(address)
                    binding.payOutEdPhone.setText(phone)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}