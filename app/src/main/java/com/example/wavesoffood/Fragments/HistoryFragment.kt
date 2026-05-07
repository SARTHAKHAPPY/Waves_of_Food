package com.example.wavesoffood.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.wavesoffood.Adapter.BuyAgainAdapter
import com.example.wavesoffood.DataClass.OrderDetails
import com.example.wavesoffood.R
import com.example.wavesoffood.RecentOrderItem
import com.example.wavesoffood.databinding.FragmentHistoryBinding
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.Circle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var buyAgainAdapter: BuyAgainAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private val listOfOrderItem: ArrayList<OrderDetails> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.recentBuyItemCart.setOnClickListener {
            seeItemRecentBuy()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loader2()
        binding.loader2.visibility = View.VISIBLE

        retrieveBuyHistory()

        binding.receivedBtn.setOnClickListener {

            binding.receivedBtn.text = "Confirm Order"

            Toast.makeText(
                requireContext(),
                "Order confirmed, please wait for delivery 🤩",
                Toast.LENGTH_LONG
            ).show()

            updateOrderStatus()

            binding.receivedBtn.setBackgroundResource(R.drawable.un_shape)
            binding.receivedBtn.visibility = View.GONE
        }
    }

    private fun retrieveBuyHistory() {

        val userId = auth.currentUser?.uid ?: return

        // ✅ FIXED PATH
        val ref = database.reference
            .child("Users")
            .child(userId)
            .child("BuyHistory")

        val query = ref.orderByChild("currentTime")

        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                listOfOrderItem.clear() // ✅ IMPORTANT

                for (snap in snapshot.children) {
                    val item = snap.getValue(OrderDetails::class.java)
                    item?.let { listOfOrderItem.add(it) }
                }

                listOfOrderItem.reverse()

                binding.loader2.visibility = View.GONE

                if (listOfOrderItem.isEmpty()) {
                    binding.emptyTxt.visibility = View.VISIBLE
                    binding.emptyImg.visibility = View.VISIBLE
                    binding.recentBuyItemLayout.visibility = View.GONE
                    return
                }

                binding.emptyTxt.visibility = View.GONE
                binding.emptyImg.visibility = View.GONE

                setDataInRecentBuyItem()
                setPreviousBuyItemsRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                binding.loader2.visibility = View.GONE
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setDataInRecentBuyItem() {

        val recentItem = listOfOrderItem.firstOrNull() ?: return

        binding.recentBuyItemLayout.visibility = View.VISIBLE

        binding.rFoodName.text = recentItem.foodNames?.firstOrNull() ?: ""
        binding.rFoodPrice.text = recentItem.foodPrices?.firstOrNull() ?: ""

        val img = recentItem.foodImages?.firstOrNull() ?: ""
        Glide.with(this).load(Uri.parse(img)).into(binding.rFoodImg)

        val isAccepted = recentItem.orderAccepted

        if (isAccepted) {
            binding.orderStatus.setBackgroundResource(R.color.appColor)
            binding.receivedBtn.visibility = View.VISIBLE
        } else {
            binding.orderStatus.setBackgroundResource(R.color.defaultColor)
            binding.receivedBtn.visibility = View.GONE
        }
    }

    private fun setPreviousBuyItemsRecyclerView() {

        val names = mutableListOf<String>()
        val prices = mutableListOf<String>()
        val imgs = mutableListOf<String>()

        for (i in 1 until listOfOrderItem.size) {
            listOfOrderItem[i].foodNames?.firstOrNull()?.let { names.add(it) }
            listOfOrderItem[i].foodPrices?.firstOrNull()?.let { prices.add(it) }
            listOfOrderItem[i].foodImages?.firstOrNull()?.let { imgs.add(it) }
        }

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())

        buyAgainAdapter = BuyAgainAdapter(names, prices, imgs, requireContext())
        binding.rvHistory.adapter = buyAgainAdapter
    }

    private fun updateOrderStatus() {

        val itemKey = listOfOrderItem.firstOrNull()?.itemPushKey ?: return

        database.reference
            .child("CompletedOrder")
            .child(itemKey)
            .child("paymentReceived")
            .setValue(true)
    }

    private fun seeItemRecentBuy() {

        if (listOfOrderItem.isEmpty()) {
            Toast.makeText(requireContext(), "No recent orders", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(requireContext(), RecentOrderItem::class.java)
        intent.putExtra("RecentBuyOrderItem", listOfOrderItem)
        startActivity(intent)
    }

    private fun loader2() {
        val progressBar = binding.loader2 as ProgressBar
        val circle: Sprite = Circle()
        progressBar.indeterminateDrawable = circle
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}