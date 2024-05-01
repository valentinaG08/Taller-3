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
import com.bumptech.glide.Glide
import com.example.taller3.utils.schemas.User
import com.google.firebase.Firebase
import com.google.firebase.storage.storage

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

        val imageReference = Firebase.storage.reference.child("images").child(currentUser.id)

        Glide
            .with(holder.imageUser)
            .load(imageReference)
            .into(holder.imageUser)
        //holder.imageUser.setImageResource(currentUser.imagenId!!.toInt())
        holder.textName.text = "${currentUser.firstName}"
        holder.btnViewLocation.setOnClickListener { onClickListener(currentUser) }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}