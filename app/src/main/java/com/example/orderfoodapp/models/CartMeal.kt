package com.example.orderfoodapp.models

class CartMeal(val name:String,val price:Int,val profileImageUrl:String,val qty:Int,val note:String) {
    constructor():this("",0,"",1,"")
}