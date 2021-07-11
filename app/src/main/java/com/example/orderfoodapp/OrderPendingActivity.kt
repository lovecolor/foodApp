package com.example.orderfoodapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.orderfoodapp.models.Order
import com.example.orderfoodapp.models.Restaurant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_order_pending.*

class OrderPendingActivity : AppCompatActivity() {
    companion object{
        val listOrder: MutableList<Order> = mutableListOf()
    }
    val adapter=GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_pending)
        recycler_view_order_pending.adapter=adapter
        setUpAdapter()
    }
    private fun setUpAdapter(){
        listOrder.forEach {
            val ref=FirebaseDatabase.getInstance().getReference("/restaurants/${it.restaurantId}")
            ref.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val restaurant=snapshot.getValue(Restaurant::class.java)
                    adapter.add(HistoryOrderRowItem(it,restaurant!!))
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
        adapter.setOnItemClickListener { item, view ->
            val intent=Intent(view.context,StatusOrderActivity::class.java)
            val orderItem=item as HistoryOrderRowItem
            intent.putExtra(CartActivity.ORDER_KEY,orderItem.order)
            view.context.startActivity(intent)
        }
    }
}