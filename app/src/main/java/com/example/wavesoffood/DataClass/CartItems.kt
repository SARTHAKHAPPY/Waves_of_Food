package com.example.wavesoffood.DataClass

data class CartItems(
    var foodName: String? = null,
    var foodPrice: String? = null,
    var foodImg: String? = null,
    var foodDes: String? = null,
    var foodIngredients: String? = null,
    var quantity: Int = 1,   // ✅ NOT nullable
    var itemKey: String? = null
)