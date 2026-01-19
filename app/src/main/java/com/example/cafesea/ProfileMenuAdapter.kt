package com.example.cafesea

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfileMenuAdapter(
    private val menuList: List<MenuModel>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<ProfileMenuAdapter.MenuViewHolder>() {

    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.menuIcon)
        val title: TextView = view.findViewById(R.id.menuTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuList[position]
        holder.title.text = item.title
        holder.icon.setImageResource(item.icon)
        holder.itemView.setOnClickListener { onClick(item.title) }
    }

    override fun getItemCount() = menuList.size
}