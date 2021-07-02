package com.example.orderfoodapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

class OrderDetailActivity : AppCompatActivity() {
    val adapter=GroupAdapter<GroupieViewHolder>()
    var restaurant:Restaurant?=null
    var order:Order?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

         restaurant=intent.getParcelableExtra<Restaurant>(RestaurantsActivity.RESTAURANT_KEY)
         order=intent.getParcelableExtra<Order>(HistoryOrderActivity.ORDER)

        btn_bought_again_order_detail.setOnClickListener {
            val intent=Intent(this,CartActivity::class.java)
            CartActivity.qty=0
            CartActivity.total=0
            CartActivity.listMeal.clear()
            val ref=FirebaseDatabase.getInstance().getReference("/orderDetails/${order?.id}")
            ref.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach{
                        val meal=it.getValue(CartMeal::class.java)
                        CartActivity.listMeal.add(meal!!)
                        CartActivity.qty+=meal.qty
                        CartActivity.total+=(meal.qty*meal.price)
                    }
                    startActivity(intent)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
        setUpAdapter()

        recycler_view_order_detail.adapter=adapter
        recycler_view_order_detail.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
    }
    private fun setUpAdapter(){
        adapter.add(HeadOrderDetailItem(restaurant!!,order!!))
        fetchOrderDetail()


    }
    private fun fetchOrderDetail()
    {
//        Log.d()
        val ref=FirebaseDatabase.getInstance().getReference("/orderDetails/${order?.id}")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("OrderDetails",snapshot.children.count().toString())
                snapshot.children.forEach{
                    val cartMeal
                    =it.getValue(CartMeal::class.java)
                    adapter.add(CartActivity.CartRowItem(cartMeal!!,false))
                }
                adapter.add(FooterCartItem(order?.qty!!,order?.total!!-15000))
                val refUser=FirebaseDatabase.getInstance().getReference("/users/${order?.uid}")
                refUser.addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user=snapshot.getValue(User::class.java)
                        adapter.add(FootOrderDetailItem(order!!,user!!))
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
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
            itemView.tv_cancelled_head_order_detail.isVisible=true
        }
        else{
            itemView.tv_cancelled_head_order_detail.isVisible=false
        }
        itemView.textView_name_restaurant_head_order_detail.text=restaurant.name
        itemView.textView_name_restaurant_head_order_detail.setOnClickListener {
            val intent =Intent(itemView.context,MenuActivity::class.java)
            intent.putExtra(RestaurantsActivity.RESTAURANT_KEY,restaurant)
            itemView.context.startActivity(intent)
        }
        itemView.textView_date_time_head_order_detail.text=order.date+" "+order.time
    }

    override fun getLayout(): Int {
        return R.layout.header_order_detail
    }

}
class FootOrderDetailItem(val order: Order,val user:User): Item<GroupieViewHolder>()
{


    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
val itemView=viewHolder.itemView

        itemView.textView_total_foot_order_detail.text=order.total.toString()+"đ"
        itemView.textView_type_pay_foot_order_detail.text=order.typePay
        itemView.textView_order_id_foot_order_detail.text=order.id

        itemView.textView_name_foot_order_detail.text=user?.username
        itemView.textView_phone_foot_order_detail.text=user?.phone
        itemView.textView_address_foot_order_detail.text=order.address
    }

    override fun getLayout(): Int {
        return R.layout.footer_order_detail
    }

}
