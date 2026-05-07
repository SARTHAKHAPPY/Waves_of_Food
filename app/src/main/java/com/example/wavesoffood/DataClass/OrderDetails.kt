package com.example.wavesoffood.DataClass

import java.io.Serializable

data class OrderDetails(
    var userUid: String? = null,
    var userName: String? = null,
    var foodNames: ArrayList<String>? = null,
    var foodPrices: ArrayList<String>? = null,
    var foodImages: ArrayList<String>? = null,
    var foodQty: ArrayList<Int>? = null,
    var address: String? = null,
    var totalPrice: String? = null,
    var phoneNumber: String? = null,
    var currentTime: Long = 0,
    var itemPushKey: String? = null,
    var orderAccepted: Boolean = false,
    var paymentReceived: Boolean = false
) : Serializable