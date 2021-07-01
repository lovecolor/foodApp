package com.example.orderfoodapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.chatrealtime.models.User
import com.example.orderfoodapp.models.CartMeal
import com.example.orderfoodapp.models.Order
import com.example.orderfoodapp.models.Restaurant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.footer_order_detail.view.*
import kotlinx.android.synthetic.main.header_order_detail.view.*
import kotlinx.android.synthetic.main.header_status_order.view.*

class OrderDetailActivity : AppCompatActivity() {
    val adapter=GroupAdapter<GroupieViewHolder>()
    var restaurant:Restaurant?=null
    var order:Order?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

         restaurant=intent.getParcelableExtra<Restaurant>(RestaurantsActivity.RESTAURANT_KEY)
         order=intent.getParcelableExtra<Order>(HistoryOrderActivity.ORDER)
//        setUpAdapter()

        recycler_view_order_detail.adapter=adapter
        recycler_view_order_detail.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
    }
    private fun setUpAdapter(){
        adapter.add(HeadOrderDetailItem(restaurant!!,order!!))
        fetchOrderDetail()
        adapter.add(FootOrderDetailItem(order!!))
    }
    private fun fetchOrderDetail()
    {
        val ref=FirebaseDatabase.getInstance().getReference("/orderDetails/${order?.id}")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    val cartMeal
                    =it.getValue(CartMeal::class.java)
                    adapter.add(CartActivity.CartRowItem(cartMeal!!,false))
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}
class HeadOrderDetailItem(val restaurant: Restaurant,val order: Order): Item<GroupieViewHolder>()
{
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val itemView=viewHolder.itemView
        if(order.status=="Cancelled"){
            itemView.textView_status_header_status_order.isVisible=false
        }
        else{
            itemView.textView_status_header_status_order.isVisible=true
        }
        itemView.textView_name_restaurant_head_order_detail.text=restaurant.name
        itemView.textView_date_time_head_order_detail.text=order.date+" "+order.time
    }

    override fun getLayout(): Int {
        return R.layout.header_order_detail
    }

}
class FootOrderDetailItem(val order: Order): Item<GroupieViewHolder>()
{
    var user:User?=null
    private fun fetchUser()
    {
        val ref=FirebaseDatabase.getInstance().getReference("/users/${order.uid}")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                user=snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
val itemView=viewHolder.itemView
        itemView.textView_temporary_expense_foot_order_detail.text=(order.total-15000).toString()+"đ"
        itemView.textView_total_qty_footer_order_detail.text="("+order.qty+" meals)"
        itemView.textView_total_foot_order_detail.text=order.total.toString()+"đ"
        itemView.textView_type_pay_foot_order_detail.text=order.typePay
        itemView.textView_order_id_foot_order_detail.text=order.id
        fetchUser()
        itemView.textView_name_foot_order_detail.text=user?.username
        itemView.textView_phone_foot_order_detail.text=user?.phone
        itemView.textView_address_foot_order_detail.text=order.address
    }

    override fun getLayout(): Int {
        return R.layout.footer_order_detail
    }

}
