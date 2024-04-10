package com.message.messengerapp.AdapterClasses

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.message.messengerapp.MessageChatActivity
import com.message.messengerapp.ModelClasses.Chat
import com.message.messengerapp.ModelClasses.Users
import com.message.messengerapp.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private val mContext: Context, // Context of the adapter
    private val mUsers: List<Users>, // List of users to display
    private val isChatCheck: Boolean, // Boolean indicating if the adapter is used for displaying chats
    private var lastMsg: String = "" // Last message in the conversation
): RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private val firebaseUser = FirebaseAuth.getInstance().currentUser // Firebase user

    // Create view holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, parent, false)
        return ViewHolder(view)
    }

    // Get item count
    override fun getItemCount(): Int {
        return mUsers.size
    }

    // Bind data to views
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUsers[position]
        holder.userNameTxt.text = user.getUsername()
        val profileImageUrl = user.getProfile()

        // Load profile image using Picasso
        if (!profileImageUrl.isNullOrEmpty()) {
            Picasso.get().load(profileImageUrl).placeholder(R.drawable.ic_profile).into(holder.profileImageView)
        } else {
            holder.profileImageView.setImageResource(R.drawable.ic_profile)
        }

        // Retrieve and display last message if isChatCheck is true
        if (isChatCheck) {
            retrieveLastMessage(user.getUid(), holder.lastMessageTxt)
        } else {
            holder.lastMessageTxt.visibility = View.GONE
        }

        // Show online/offline status
        if (isChatCheck) {
            if (user.getStatus() == "online") {
                holder.onlineImageView.visibility = View.VISIBLE
                holder.offlineImageView.visibility = View.GONE
            } else {
                holder.onlineImageView.visibility = View.GONE
                holder.offlineImageView.visibility = View.VISIBLE
            }
        } else {
            holder.onlineImageView.visibility = View.GONE
            holder.offlineImageView.visibility = View.GONE
        }

        // Handle item click listener
        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            )

            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("What do you want")
            builder.setItems(options) { dialog, position ->
                if (position == 0) {
                    // Send message action
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.putExtra("visit_id", user.getUid())
                    mContext.startActivity(intent)
                } else if (position == 1) {
                    // Visit profile action
                    // TODO: Handle "Visit Profile" action
                }
            }
            builder.show()
        }
    }

    // ViewHolder class
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userNameTxt: TextView = itemView.findViewById(R.id.username)
        val profileImageView: CircleImageView = itemView.findViewById(R.id.profileimage)
        val onlineImageView: CircleImageView = itemView.findViewById(R.id.imageonline)
        val offlineImageView: CircleImageView = itemView.findViewById(R.id.imageoffline)
        val lastMessageTxt: TextView = itemView.findViewById(R.id.messagelast)
    }

    // Retrieve last message from the database
    private fun retrieveLastMessage(chatUserId: String, lastMessageTxt: TextView) {
        lastMsg = "defaultMsg"
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val chat: Chat? = snapshot.getValue(Chat::class.java)
                    if (firebaseUser != null && chat != null) {
                        if ((chat.getReceiver() == firebaseUser.uid && chat.getSender() == chatUserId) ||
                            (chat.getReceiver() == chatUserId && chat.getSender() == firebaseUser.uid)) {
                            lastMsg = chat.getMessage() ?: "No Message"
                        }
                    }
                }
                when (lastMsg) {
                    "defaultMsg" -> lastMessageTxt.text = "No Message"
                    "sent you an image." -> lastMessageTxt.text = "Image sent"
                    else -> lastMessageTxt.text = lastMsg
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
}
