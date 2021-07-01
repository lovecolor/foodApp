package com.example.orderfoodapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.orderfoodapp.models.Order
import com.example.orderfoodapp.models.Restaurant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_history_order.*
import kotlinx.android.synthetic.main.history_order_row.view.*

class HistoryOrderActivity : AppCompatActivity() {
    val adapter = GroupAdapter<GroupieViewHolder>()
    companion object{
        val ORDER="ORDER"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_order)
        fetchData()
    }

    private fun fetchData() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/orders/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for(i in snapshot.children.count()-1 downTo 0 )
                {

                    val order =snapshot.children.elementAt(i).getValue(Order::class.java) as Order
                    val refRestaurant=FirebaseDatabase.getInstance().getReference("/restaurants/${order.restaurantId}")
                    refRestaurant.addListenerForSingleValueEvent(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val restaurant=snapshot.getValue(Restaurant::class.java)
                            adapter.add(HistoryOrderRowItem(order,restaurant!!))
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })


                }



            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        adapter.setOnItemClickListener { item, view ->
            val intent= Intent(view.context,OrderDetailActivity::class.java)
            val historyOrderRowItem=item as HistoryOrderRowItem
            intent.putExtra(RestaurantsActivity.RESTAURANT_KEY,historyOrderRowItem.restaurant)
            intent.putExtra(ORDER,historyOrderRowItem.order)
            view.context.startActivity(intent)
        }
        recycler_view_history_order.adapter = adapter
    }
}

class HistoryOrderRowItem(val order: Order,val restaurant: Restaurant) : Item<GroupieViewHolder>() {



    @SuppressLint("ResourceAsColor")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Log.d("Restaurantid",order.restaurantId)
        val itemView = viewHolder.itemView
        itemView.textView_status_history_row.text = order.status
        itemView.textView_date_history_row.text = order.date

        itemView.textView_name_restaurant_history_row.text = restaurant?.name
        itemView.textView_price_history_row.text = order.total.toString() + "đ"
        itemView.textView_type_pay_history_row.text = "(" + order.typePay + ")"
        itemView.textView_qty_history_row.text = order.qty.toString() + " Meal"
        if (order.status == "Cancelled") {

            itemView.imageView_status_history_order.setImageResource(R.drawable.ic_baseline_report_24)
            itemView.imageView_status_history_order.setColorFilter(Color.RED)
            itemView.textView_status_history_row.setTextColor(Color.RED)
            itemView.textView_name_restaurant_history_row.setTextColor(R.color.disable)
        } else {


            itemView.imageView_status_history_order.setColorFilter(Color.GRAY)
            itemView.textView_status_history_row.setTextColor(Color.GRAY)
            itemView.textView_name_restaurant_history_row.setTextColor(Color.BLACK)
        }


    }

    override fun getLayout(): Int {
        return R.layout.history_order_row
    }

}