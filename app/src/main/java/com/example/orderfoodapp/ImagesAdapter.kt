package com.example.orderfoodapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.asksira.loopingviewpager.LoopingPagerAdapter

class ImagesAdapter(private val images: MutableList<Int>) : LoopingPagerAdapter<Int>(images, true) {
    override fun bindView(convertView: View, listPosition: Int, viewType: Int) {

        convertView.findViewById<ImageView>(R.id.imageView_lopp_viewpager).setImageResource(images[listPosition])
    }

    override fun inflateView(viewType: Int, container: ViewGroup, listPosition: Int): View {
        return LayoutInflater.from(container.context).inflate(R.layout.layout_viewpager, container, false)
    }
}