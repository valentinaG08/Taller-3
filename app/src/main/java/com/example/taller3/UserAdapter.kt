package com.example.taller3

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private var users: List<User>, private val context: Context, private val onClickListener: (User) -> Unit) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    fun setUsers(users: List<User>) {
        this.users = users
        notifyDataSetChanged()
    }
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageUser: ImageView = itemView.findViewById(R.id.imageUser)
        val textName: TextView = itemView.findViewById(R.id.textName)
        val btnViewLocation: Button = itemView.findViewById(R.id.btnViewLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = users[position]
        holder.imageUser.setImageResource(currentUser.imagenId)
        holder.textName.text = "${currentUser.nombre}"
        holder.btnViewLocation.setOnClickListener { onClickListener(currentUser) }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}