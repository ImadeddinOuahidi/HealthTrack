package com.example.healthtrack;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private final List<Achievement> achievementList;
    private final Context context;

    public AchievementAdapter(Context context, List<Achievement> achievementList) {
        this.context = context;
        this.achievementList = achievementList;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievementList.get(position);
        holder.title.setText(achievement.getTitle());
        holder.description.setText(achievement.getDescription());
        holder.icon.setImageResource(achievement.getIconResId());

        // Set badge based on tier
        Achievement.BadgeTier tier = achievement.getBadgeTier();
        int badgeResId = getBadgeResource(tier);
        holder.badge.setImageResource(badgeResId);
        
        // Set badge label text and background
        String tierName = getTierName(tier);
        holder.badgeLabel.setText(tierName);
        holder.badgeLabel.setBackgroundResource(badgeResId);

        if (achievement.isUnlocked()) {
            holder.layout.setAlpha(1.0f);
            holder.icon.setImageTintList(null);
            holder.badge.setAlpha(1.0f);
            holder.badgeLabel.setAlpha(1.0f);
        } else {
            holder.layout.setAlpha(0.5f);
            holder.icon.setImageTintList(ColorStateList.valueOf(Color.GRAY));
            holder.badge.setAlpha(0.3f);
            holder.badgeLabel.setAlpha(0.5f);
        }
    }

    private int getBadgeResource(Achievement.BadgeTier tier) {
        switch (tier) {
            case BRONZE:
                return R.drawable.badge_bronze;
            case SILVER:
                return R.drawable.badge_silver;
            case GOLD:
                return R.drawable.badge_gold;
            case PLATINUM:
                return R.drawable.badge_platinum;
            case DIAMOND:
                return R.drawable.badge_diamond;
            default:
                return R.drawable.badge_bronze;
        }
    }

    private String getTierName(Achievement.BadgeTier tier) {
        switch (tier) {
            case BRONZE:
                return "BRONZE";
            case SILVER:
                return "SILVER";
            case GOLD:
                return "GOLD";
            case PLATINUM:
                return "PLATINUM";
            case DIAMOND:
                return "DIAMOND";
            default:
                return "BRONZE";
        }
    }

    @Override
    public int getItemCount() {
        return achievementList.size();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        ImageView icon, badge;
        TextView title, description, badgeLabel;
        LinearLayout layout;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.achievement_icon);
            badge = itemView.findViewById(R.id.achievement_badge);
            title = itemView.findViewById(R.id.achievement_title);
            description = itemView.findViewById(R.id.achievement_description);
            badgeLabel = itemView.findViewById(R.id.achievement_badge_label);
            layout = itemView.findViewById(R.id.achievement_layout);
        }
    }
}
