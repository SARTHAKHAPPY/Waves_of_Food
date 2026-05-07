package com.example.wavesoffood.DataClass

data class FoodModel(
    var foodName: String? = null,
    var foodPrice: String? = null,
    var foodDes: String? = null,
    var foodImg: String? = null,
    var foodIngredients: String? = null,

    // ✅ NEW FIELDS
    var quantity: Int = 1,
    var itemKey: String? = null
)