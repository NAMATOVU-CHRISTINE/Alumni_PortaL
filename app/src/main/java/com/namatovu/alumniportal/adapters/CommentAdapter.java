package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.Comment;
import java.util.List;

/**
 * Adapter for displaying comments in RecyclerView
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> comments;
    private OnCommentInteractionListener listener;

    public interface OnCommentInteractionListener {
        void onLikeComment(Comment comment, int position);
        void onReplyToComment(Comment comment, int position);
    }

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    public void setOnCommentInteractionListener(OnCommentInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment, position);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void addComment(Comment comment) {
        comments.add(0, comment); // Add to top
        notifyItemInserted(0);
    }

    public void updateComment(int position, Comment comment) {
        if (position >= 0 && position < comments.size()) {
            comments.set(position, comment);
            notifyItemChanged(position);
        }
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAuthorName;
        private TextView tvContent;
        private TextView tvTimestamp;
        private MaterialButton btnLike;
        private MaterialButton btnReply;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnReply = itemView.findViewById(R.id.btnReply);
        }

        public void bind(Comment comment, int position) {
            tvAuthorName.setText(comment.getAuthorName());
            tvContent.setText(comment.getContent());
            tvTimestamp.setText(comment.getFormattedTimestamp());

            // Update like button
            String likeText = comment.isLiked() ? "❤️ " + comment.getLikeCount() : "🤍 " + comment.getLikeCount();
            btnLike.setText(likeText);

            // Set click listeners
            btnLike.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLikeComment(comment, position);
                }
            });

            btnReply.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReplyToComment(comment, position);
                }
            });
        }
    }
}