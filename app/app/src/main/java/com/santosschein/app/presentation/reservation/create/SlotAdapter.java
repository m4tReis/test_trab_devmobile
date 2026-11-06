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
import com.santosschein.app.domain.model.ReservationSlot;
import com.santosschein.app.presentation.common.PresentationFormatters;

import java.util.ArrayList;
import java.util.List;

public final class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.ViewHolder> {

    public interface Listener {
        void onSlotClick(ReservationSlot slot);
    }

    private final Listener listener;
    private final List<ReservationSlot> slots = new ArrayList<>();
    private String selectedSlotId;

    public SlotAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<ReservationSlot> newSlots, ReservationSlot selectedSlot) {
        slots.clear();
        slots.addAll(newSlots);
        selectedSlotId = selectedSlot != null ? selectedSlot.getId() : null;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReservationSlot slot = slots.get(position);
        holder.slotTextView.setText(PresentationFormatters.time(slot.getStartTime())
                + " - "
                + PresentationFormatters.time(slot.getEndTime())
                + "  •  "
                + PresentationFormatters.money(slot.getAmount()));

        boolean selected = slot.getId().equals(selectedSlotId);
        holder.rootCard.setCardBackgroundColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                selected ? R.color.santos_royal_blue : R.color.white
        ));
        holder.rootCard.setStrokeColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                selected ? R.color.santos_royal_blue : R.color.santos_blue_gray
        ));
        holder.slotTextView.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                selected ? R.color.white : R.color.santos_graphite
        ));
        holder.itemView.setOnClickListener(v -> listener.onSlotClick(slot));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView rootCard;
        private final TextView slotTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootCard = itemView.findViewById(R.id.rootCard);
            slotTextView = itemView.findViewById(R.id.slotTextView);
        }
    }
}
