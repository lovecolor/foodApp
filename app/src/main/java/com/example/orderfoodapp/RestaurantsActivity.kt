package com.example.orderfoodapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.chatrealtime.models.User
import com.example.orderfoodapp.models.Meal
import com.example.orderfoodapp.models.Restaurant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_restaurants.*
import kotlinx.android.synthetic.main.activity_restaurants.view.*
import kotlinx.android.synthetic.main.loop_viewpager.view.*
import kotlinx.android.synthetic.main.restaurant_row.view.*
import java.sql.DriverPropertyInfo


class RestaurantsActivity : AppCompatActivity() {
    companion object {
        val RESTAURANT_KEY = "RESTAURANT_KEY"
        var currentUser: User? = null
    }

    val adapterRecycler = GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurants)


        verifyUserIsLoggedIn()
//        CartActivity.listMeal.clear()


        adapterRecycler.add(LoopViewItem())
        fetchRestaurants()
        recycler_restaurant.adapter = adapterRecycler
        adapterRecycler.setOnItemClickListener { item, view ->
            val restaurantItem = item as RestaurantItem
            val intent = Intent(view.context, MenuActivity::class.java)
            intent.putExtra(RESTAURANT_KEY, restaurantItem.restaurant)
            CartActivity.listMeal.clear()
            CartActivity.total=0
            CartActivity.qty=0
            startActivity(intent)
        }
        recycler_restaurant.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

    }

    class LoopViewItem : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val adapter = ImagesAdapter(
                mutableListOf(
                    R.drawable.background1,
                    R.drawable.backgounrd2,
                    R.drawable.background3,
                    R.drawable.background4
                )
            )
            viewHolder.itemView.loopviewpager_restaurant.adapter = adapter
        }

        override fun getLayout(): Int {
            return R.layout.loop_viewpager
        }

    }

    private fun fetchRestaurants() {
        val ref = FirebaseDatabase.getInstance().getReference("/restaurants")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val restaurant = it.getValue(Restaurant::class.java)
                    if (restaurant != null) {
                        adapterRecycler.add(RestaurantItem(restaurant))
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        fectchUser()
        if (uid == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun fectchUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {

            R.id.menu_infomation -> {

                val intent = Intent(this, actionsActivity::class.java)

                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}

class RestaurantItem(val restaurant: Restaurant) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val itemView=viewHolder.itemView
        Picasso.get().load(restaurant.profileImageUrl)
            .into(itemView.imageView_restaurant)
        itemView.name_textview_restaurant.text = restaurant.name
        itemView.textView_distance_res_row.text=(Math.ceil((Math.random()*5)*10)/10).toString()+"km"
        val ref=FirebaseDatabase.getInstance().getReference("/menus/${restaurant.id}")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                itemView.textView_first_meal_restaurant_row.text=
                    snapshot.children.elementAt(0).getValue(Meal::class.java)?.name.toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        val qtyRate=restaurant.countRate
        if(qtyRate==0)
        {
            itemView.textView_rate_restaurant_row.isVisible=false
            itemView.textView_qty_rate_res_row.isVisible=false
            itemView.imageView_ic_rate_res_row.isVisible=false

        }
        else
        {
            itemView.textView_rate_restaurant_row.isVisible=true
            itemView.textView_qty_rate_res_row.isVisible=true
            itemView.imageView_ic_rate_res_row.isVisible=true
            val rate=Math.ceil(restaurant.rate*10)/10
            itemView.textView_rate_restaurant_row.text=rate.toString()
            itemView.textView_qty_rate_res_row.text="("+restaurant.countRate.toString()+"+)"
        }

    }

    override fun getLayout(): Int {
        return R.layout.restaurant_row
    }

}