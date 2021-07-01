package com.example.orderfoodapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Restaurant(val id:String,val name:String,val address:String,val profileImageUrl:String,val rate:Double,val countRate:Int): Parcelable {
    constructor():this("","","","", 5.0,0)
}