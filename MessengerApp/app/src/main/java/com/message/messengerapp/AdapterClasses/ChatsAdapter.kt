package com.message.messengerapp.AdapterClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.message.messengerapp.ModelClasses.Chat
import com.message.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsAdapter(
    private val mContext: Context,
    private val mChatList: List<Chat>,
    private val imageUrl: String
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    private val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == 1) {
            R.layout.message_item_right
        } else {
            R.layout.message_item_left
        }
        val view = LayoutInflater.from(mContext).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = mChatList[position]

        // Load user image if available
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(holder.profile_image)
        }

        // Display message or image based on the chat content
        if (chat.getMessage() == "sent you an image" && !chat.getUrl().isNullOrEmpty()) {
            if (chat.getSender() == firebaseUser.uid) {
                holder.show_text_message?.visibility = View.GONE
                holder.left_image_view?.visibility = View.VISIBLE
                holder.right_image_view?.visibility = View.GONE
                if (holder.left_image_view != null) {
                    Picasso.get().load(chat.getUrl()).into(holder.left_image_view)
                }
            } else {
                holder.show_text_message?.visibility = View.GONE
                holder.left_image_view?.visibility = View.GONE
                holder.right_image_view?.visibility = View.VISIBLE
                if (holder.right_image_view != null) {
                    Picasso.get().load(chat.getUrl()).into(holder.right_image_view)
                }
            }
        } else {
            holder.show_text_message?.visibility = View.VISIBLE
            holder.show_text_message?.text = chat.getMessage()
            holder.left_image_view?.visibility = View.GONE
            holder.right_image_view?.visibility = View.GONE
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
            1
        } else {
            0
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profile_image: CircleImageView? = itemView.findViewById(R.id.profile_image)
        var show_text_message: TextView? = itemView.findViewById(R.id.show_text_message)
        var left_image_view: ImageView? = itemView.findViewById(R.id.left_image_view)
        var text_seen: TextView? = itemView.findViewById(R.id.text_seen)
        var right_image_view: ImageView? = itemView.findViewById(R.id.right_image_view)
    }
}
