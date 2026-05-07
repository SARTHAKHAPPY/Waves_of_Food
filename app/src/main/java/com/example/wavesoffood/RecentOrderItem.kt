package com.example.wavesoffood

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wavesoffood.Adapter.RecentBuyAdapter
import com.example.wavesoffood.DataClass.OrderDetails
import com.example.wavesoffood.databinding.ActivityRecentOrderItemBinding

class RecentOrderItem : AppCompatActivity() {

    private val binding: ActivityRecentOrderItemBinding by lazy {
        ActivityRecentOrderItemBinding.inflate(layoutInflater)
    }

    private val allFoodNames = ArrayList<String>()
    private val allFoodImgs = ArrayList<String>()
    private val allFoodPrices = ArrayList<String>()
    private val allFoodQty = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // ✅ SAFE CAST
        val orders =
            intent.getSerializableExtra("RecentBuyOrderItem") as? ArrayList<OrderDetails>

        if (orders.isNullOrEmpty()) {
            Toast.makeText(this, "No Order History Found", Toast.LENGTH_SHORT).show()
            binding.rvRecentBuy.visibility = View.GONE
            return
        }

        // ✅ GET LATEST ORDER SAFELY
        val latestOrder = orders.lastOrNull()

        if (latestOrder == null) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ SAFE DATA ADD
        allFoodNames.addAll(latestOrder.foodNames ?: arrayListOf())
        allFoodPrices.addAll(latestOrder.foodPrices ?: arrayListOf())
        allFoodImgs.addAll(latestOrder.foodImages ?: arrayListOf())
        allFoodQty.addAll(latestOrder.foodQty ?: arrayListOf())

        if (allFoodNames.isEmpty()) {
            Toast.makeText(this, "No items in this order", Toast.LENGTH_SHORT).show()
            binding.rvRecentBuy.visibility = View.GONE
            return
        }

        setAdapter()

        binding.backBtn.setOnClickListener { finish() }
    }

    private fun setAdapter() {

        binding.rvRecentBuy.layoutManager = LinearLayoutManager(this)

        val adapter = RecentBuyAdapter(
            this,
            allFoodNames,
            allFoodPrices,
            allFoodImgs,
            allFoodQty
        )

        binding.rvRecentBuy.adapter = adapter
    }
}