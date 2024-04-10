package com.message.messengerapp.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.message.messengerapp.ViewFullImageActivity
import com.message.messengerapp.ModelClasses.Chat
import com.message.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsAdapter(
    private val mContext: Context,
    private val mChatList: List<Chat>, // List of chat messages
    private val imageUrl: String // URL of the profile image
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    private val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    // Determine the layout based on the sender
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == 1) {
            R.layout.message_item_right // Outgoing message layout
        } else {
            R.layout.message_item_left // Incoming message layout
        }
        val view = LayoutInflater.from(mContext).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    // Bind data to views
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = mChatList[position]

        // Load user image with placeholder
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_profile).into(holder.profile_image)
        }

        // Determine if the message is an image or text
        if (chat.getMessage() == "sent you an image" && !chat.getUrl().isNullOrEmpty()) {
            // If it's an image, handle the image view visibility based on the sender
            if (chat.getSender() == firebaseUser.uid) {
                // Outgoing image
                holder.show_text_message?.visibility = View.GONE
                holder.left_image_view?.visibility = View.GONE
                holder.right_image_view?.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.right_image_view)
            } else {
                // Incoming image
                holder.show_text_message?.visibility = View.GONE
                holder.left_image_view?.visibility = View.VISIBLE
                holder.right_image_view?.visibility = View.GONE
                Picasso.get().load(chat.getUrl()).into(holder.left_image_view)
            }

            // Set OnClickListener for the image views to open full image
            holder.left_image_view?.setOnClickListener {
                openFullImage(chat.getUrl())
            }
            holder.right_image_view?.setOnClickListener {
                openFullImage(chat.getUrl())
            }
        } else {
            // If it's a text message, handle the text view visibility based on the sender
            holder.left_image_view?.visibility = View.GONE
            holder.right_image_view?.visibility = View.GONE

            if (chat.getSender() == firebaseUser.uid) {
                // Outgoing text message
                holder.show_text_message?.visibility = View.VISIBLE
                holder.show_text_message?.text = chat.getMessage()

                // Set OnClickListener for deleting sent messages
                holder.show_text_message!!.setOnClickListener {
                    showDeleteDialog(position)
                }
            } else {
                // Incoming text message
                holder.show_text_message?.visibility = View.VISIBLE
                holder.show_text_message?.text = chat.getMessage()

                // Set OnClickListener for deleting received messages
                holder.show_text_message!!.setOnClickListener {
                    showDeleteDialog(position)
                }
            }
        }
        // Set "Seen" or "Sent" status
        if (position == mChatList.size - 1) {
            holder.text_seen?.text = if (chat.isSeen()) "Seen" else "Sent"
            val lp = holder.text_seen?.layoutParams as? RelativeLayout.LayoutParams
            lp?.setMargins(0, 245, 10, 0)
            holder.text_seen?.layoutParams = lp
        } else {
            holder.text_seen?.visibility = View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mChatList[position].getSender() == firebaseUser.uid) {
            1 // Outgoing message
        } else {
            0 // Incoming message
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profile_image: CircleImageView? = itemView.findViewById(R.id.profile_image)
        var show_text_message: TextView? = itemView.findViewById(R.id.show_text_message)
        var left_image_view: ImageView? = itemView.findViewById(R.id.left_image_view)
        var text_seen: TextView? = itemView.findViewById(R.id.text_seen)
        var right_image_view: ImageView? = itemView.findViewById(R.id.right_image_view)
    }

    // Function to delete a sent message
    private fun deleteSentMessage(position: Int) {
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(mChatList[position].getMessageId()!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(mContext, "Failed, Not Deleted", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to delete a received message
    private fun deleteReceivedMessage(position: Int) {
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(mChatList[position].getMessageId()!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(mContext, "Failed, Not Deleted", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to open full image
    private fun openFullImage(imageUrl: String) {
        val intent = Intent(mContext, ViewFullImageActivity::class.java)
        intent.putExtra("url", imageUrl)
        mContext.startActivity(intent)
    }

    // Function to show delete message dialog
    private fun showDeleteDialog(position: Int) {
        val options = arrayOf<CharSequence>(
            "Delete Message",
            "Cancel"
        )
        val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
        builder.setTitle("What do you want?")
        builder.setItems(options) { dialog, which ->
            if (which == 0) {
                // Delete message
                if (mChatList[position].getSender() == firebaseUser.uid) {
                    deleteSentMessage(position)
                } else {
                    deleteReceivedMessage(position)
                }
            }
        }
        builder.show()
    }
}
