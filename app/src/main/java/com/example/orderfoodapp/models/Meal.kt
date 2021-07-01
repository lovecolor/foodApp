package com.example.orderfoodapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Meal(val name:String,val price:Int,val profileImageUrl:String) : Parcelable {
    constructor():this("",-1,"")
}