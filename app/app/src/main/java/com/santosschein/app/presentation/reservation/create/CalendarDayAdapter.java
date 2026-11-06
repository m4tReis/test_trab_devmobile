package com.santosschein.app.presentation.reservation.create;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.santosschein.app.R;

import java.util.ArrayList;
import java.util.List;

public final class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.ViewHolder> {

    public interface Listener {
        void onDayClick(CalendarDay day);
    }

    private final Listener listener;
    private final List<CalendarDay> days = new ArrayList<>();

    public CalendarDayAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<CalendarDay> newDays) {
        days.clear();
        days.addAll(newDays);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarDay day = days.get(position);
        holder.dayTextView.setText(String.valueOf(day.getDate().getDayOfMonth()));
        holder.rootCard.setEnabled(day.isAvailable());
        holder.itemView.setEnabled(day.isAvailable());
        holder.itemView.setAlpha(day.isInVisibleMonth() ? 1f : 0.28f);

        int background = R.color.white;
        int stroke = R.color.santos_blue_gray;
        int text = R.color.santos_graphite;
        if (day.isAvailable()) {
            background = R.color.santos_ball_blue;
            stroke = R.color.santos_ball_blue;
            text = R.color.white;
        }
        if (day.isSelected()) {
            background = R.color.santos_energy_orange;
            stroke = R.color.santos_energy_orange;
            text = R.color.white;
        }

        holder.rootCard.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), background));
        holder.rootCard.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), stroke));
        holder.dayTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), text));
        holder.itemView.setOnClickListener(v -> {
            if (day.isAvailable()) {
                listener.onDayClick(day);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView rootCard;
        private final TextView dayTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootCard = itemView.findViewById(R.id.rootCard);
            dayTextView = itemView.findViewById(R.id.dayTextView);
        }
    }
}
