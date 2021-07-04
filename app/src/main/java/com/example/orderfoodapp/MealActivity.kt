package com.example.orderfoodapp

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.orderfoodapp.models.CartMeal
import com.example.orderfoodapp.models.Meal
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_meal.*

class MealActivity : AppCompatActivity() {
    var meal:Meal?=null
    var qty:Int=1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal)
        supportActionBar?.title="Meal Information"
        meal=intent.getParcelableExtra<Meal>(MenuActivity.MEAL_KEY)
        loadData()
        imageButton_sub_meal.setOnClickListener {
            if(qty==1){
                return@setOnClickListener
            }
            qty--;
            if(qty==1){
                imageButton_sub_meal.setBackgroundResource(R.drawable.btn_disable)
                imageButton_sub_meal.setColorFilter(Color.parseColor("#EEEEEE"));
            }
            textView_qty_meal.text=qty.toString()

            textView_total_meal.text=(qty*meal?.price!!).toString()+"đ"
        }
        imageButton_add_meal.setOnClickListener {
            if(qty==1){
                imageButton_sub_meal.setBackgroundResource(R.drawable.btn_disable)
                imageButton_sub_meal.setColorFilter(Color.parseColor("#4AC3AB"));
            }
            qty++;

            textView_qty_meal.text=qty.toString()

            textView_total_meal.text=(qty*meal?.price!!).toString()+"đ"
        }
        btn_add_meal.setOnClickListener {
val cartMeal=CartMeal(meal?.name!!,meal?.price!!,meal?.profileImageUrl!!,qty,editText_note_meal.text.toString())

            CartActivity.listMeal.add(cartMeal)
            CartActivity.total+=(meal?.price!!*qty)
            CartActivity.qty+=qty
            val intent=Intent(this,MenuActivity::class.java)
            intent.putExtra(RestaurantsActivity.RESTAURANT_KEY,MenuActivity.restaurant)
            startActivity(intent)


        }
    }
    private fun loadData(){
        Picasso.get().load(meal?.profileImageUrl).into(imageView_meal)
        textView_name_meal.text=meal?.name
        textView_price_meal.text=meal?.price.toString()+"đ"
        textView_total_meal.text=meal?.price.toString()+"đ"


    }
}