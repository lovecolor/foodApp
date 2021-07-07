package com.example.orderfoodapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.orderfoodapp.CartActivity.Companion.qty
import com.example.orderfoodapp.models.Meal
import com.example.orderfoodapp.models.Restaurant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_edit_infomation.*
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.info_restaurant.view.*
import kotlinx.android.synthetic.main.menu_row.view.*
import org.w3c.dom.Text

class MenuActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()

    companion object {
        val MEAL_KEY = "MEAL_KEY"
        var restaurant: Restaurant? = null
        var btn_cart:LinearLayout?=null
        var textView_qty:TextView?=null
        var textView_total:TextView?=null

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        supportActionBar?.title="Menu"
        btn_cart=btn_cart_menu
        textView_qty=textView_qty_meal_menu
        textView_total=textView_total_meal_menu
        restaurant = intent.getParcelableExtra<Restaurant>(RestaurantsActivity.RESTAURANT_KEY)



        setUpAdapter()

        btn_cart_menu.setOnClickListener {
            val intent=Intent(this,CartActivity::class.java)

            startActivity(intent)
        }
        if (CartActivity.listMeal.size == 0) {
            btn_cart_menu.isVisible = false


        } else {

            textView_qty_meal_menu.text=qty.toString()+" Meals"
            textView_total_meal_menu.text=CartActivity.total.toString()+"đ"
            btn_cart_menu.isVisible=true

        }



        recycler_menu.adapter = adapter

    }

    private fun setUpAdapter(){
        adapter.add(InfoRestaurantItem(restaurant!!))
        fetchMenu()
        adapter.setOnItemClickListener { item, view ->
            if (item != adapter.getItem(0)) {
                val mealItem = item as MealItem
                val intent = Intent(this, MealActivity::class.java)
                intent.putExtra(MEAL_KEY, mealItem.meal)
                startActivity(intent)
            }

        }
        recycler_menu.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }
    private fun fetchMenu() {
        val id = restaurant?.id
        val ref = FirebaseDatabase.getInstance().getReference("/menus/$id")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val meal = it.getValue(Meal::class.java)
                    if (meal != null) {
                        adapter.add(MealItem(meal))
                    }


                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}


class InfoRestaurantItem(val restaurant: Restaurant) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Picasso.get().load(restaurant?.profileImageUrl)
            .into(viewHolder.itemView.imageView_restaurant_menu)
        viewHolder.itemView.textView_name_restaurant_menu.text = restaurant?.name
        viewHolder.itemView.textView_address_restaurant_menu.text = restaurant?.address
    }

    override fun getLayout(): Int {
        return R.layout.info_restaurant
    }

}

class MealItem(val meal: Meal) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Picasso.get().load(meal.profileImageUrl).into(viewHolder.itemView.imageView_menu_row)
        viewHolder.itemView.textView_name_menu_row.text = meal.name

        viewHolder.itemView.textView_price_menu_row.text = meal.price.toString() + "đ"
    }

    override fun getLayout(): Int {
        return R.layout.menu_row
    }

}