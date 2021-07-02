package com.example.orderfoodapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_find_restaurant.*

class FindRestaurantActivity : AppCompatActivity() {
    val adapter=GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_restaurant)



        setUp()
        adapter.setOnItemClickListener { item, view ->
            val restaurantItem=item as RestaurantItem
            val intent=Intent(view.context,MenuActivity::class.java)
            intent.putExtra(RestaurantsActivity.RESTAURANT_KEY,restaurantItem.restaurant)
            view.context.startActivity(intent)
        }
        recycler_view_find.adapter=adapter
    }
    private fun setUp(){
        editText_text_find_restaurant.requestFocus()
        editText_text_find_restaurant.addTextChangedListener {
            adapter.clear()
            val text=editText_text_find_restaurant.text.toString().trim().toLowerCase()
            if(text.length>0)
            {
                RestaurantsActivity.listRestaurant.forEach {
                    if(it.name.toLowerCase().contains(text)){
                        adapter.add(RestaurantItem(it))
                    }
                }
            }

        }
        imageButton_back_find.setOnClickListener {
            val intent=Intent(this,RestaurantsActivity::class.java)
            startActivity(intent)
        }
        imageButton_reset_text_find.setOnClickListener {
            editText_text_find_restaurant.setText("")
        }
    }
}