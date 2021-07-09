package com.example.orderfoodapp

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_cart.view.*
import kotlinx.android.synthetic.main.activity_type_pay.*
import kotlinx.android.synthetic.main.type_pay_row.view.*
import java.util.HashMap


class TypePayActivity : AppCompatActivity() {
    var listIc: MutableList<Int> = mutableListOf(R.drawable.ic_cash, R.drawable.ic_atm, R.drawable.ic_visa, R.drawable.ic_momo, R.drawable.ic_zlpay, R.drawable.ic_vnpay, R.drawable.ic_vtpay)
    var listText: MutableList<String> = mutableListOf("Cash", "ATM", "VISA", "MoMo", "ZaloPay", "VNPAY", "ViettelPay")
    var icMap = HashMap<String, Int>()
    val adapter=GroupAdapter<GroupieViewHolder>()
    companion object{
        var currentTypePay:String?=null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_type_pay)
        supportActionBar?.title = "Type Pay"

        currentTypePay=intent.getStringExtra(CartActivity.TEXT_TYPE_PAY)
        recycler_view_type_pay.adapter=adapter
        recycler_view_type_pay.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        setUp()



    }
    private fun setUp()
    {

        setUpAdapter()
        textView_confirm_type_pay.setOnClickListener {


            val intent= Intent(this,CartActivity::class.java)
            intent.putExtra(CartActivity.TEXT_TYPE_PAY, currentTypePay)
            intent.putExtra(CartActivity.IC_TYPE_PAY,icMap[currentTypePay])
            startActivity(intent)
        }
    }
    private fun setUpAdapter()
    {
        for (i in 0..listText.size-1){
            adapter.add(TypePayRowItem(listIc[i], listText[i]))
            icMap[listText[i]]=listIc[i]
        }
        adapter.setOnItemClickListener { item, view ->
            val typePayRowItem=item as TypePayRowItem
            currentTypePay=typePayRowItem.text

            adapter.notifyDataSetChanged()
        }
    }
}
class TypePayRowItem(val img: Int, val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val itemView=viewHolder.itemView
        itemView.textView_type_pay_row.text=text;
        var icCheck:Int=0
        if(text==TypePayActivity.currentTypePay)
        {
            icCheck=R.drawable.ic_baseline_check_24
            val spanString = SpannableString(text)

            spanString.setSpan(StyleSpan(Typeface.BOLD), 0, spanString.length, 0)
            itemView.textView_type_pay_row.text=spanString
        }
        itemView.textView_type_pay_row.setCompoundDrawablesWithIntrinsicBounds(img, 0, icCheck, 0)
    }

    override fun getLayout(): Int {
        return R.layout.type_pay_row
    }

}