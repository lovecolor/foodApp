package com.example.orderfoodapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.chrono.ChronoLocalDateTime
@Parcelize
class Order(val id:String, val uid:String, val restaurantId: String, val date:String, val time:String, val total:Int, val qty:Int, val typePay:String,
            var status:String, val address:String) :
    Parcelable {
    constructor():this("","","","","",0,0,"","","")
}