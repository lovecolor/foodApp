package com.example.orderfoodapp.models

class Coupon(val code:String,val title:String,val hsd:String,val type:Int,val typePay:String,val minPrice:Int,val sale:Int,val maxSale:Int) {
    constructor():this("","","",0,"",0,0,0)
}