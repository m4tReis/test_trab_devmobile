package com.santosschein.app.presentation.reservation.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.santosschein.app.R;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.presentation.common.PresentationFormatters;

import java.util.ArrayList;
import java.util.List;

public final class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    public interface Listener {
        void onCancelClick(Reservation reservation);
    }

    private boolean showCancelButton;
    private final Listener listener;
    private final List<Reservation> reservations = new ArrayList<>();

    public ReservationAdapter(boolean showCancelButton, Listener listener) {
        this.showCancelButton = showCancelButton;
        this.listener = listener;
    }

    public void submitList(List<Reservation> newReservations) {
        reservations.clear();
        reservations.addAll(newReservations);
        notifyDataSetChanged();
    }

    public void setShowCancelButton(boolean showCancelButton) {
        this.showCancelButton = showCancelButton;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        holder.fieldNameTextView.setText(reservation.getFieldName());
        holder.dateTextView.setText("Data: " + PresentationFormatters.date(reservation.getDate()));
        holder.timeTextView.setText("Horario: "
                + PresentationFormatters.time(reservation.getStartTime())
                + " - "
                + PresentationFormatters.time(reservation.getEndTime()));
        holder.amountTextView.setText("Valor: " + PresentationFormatters.money(reservation.getAmount()));
        holder.statusTextView.setText(PresentationFormatters.status(reservation.getStatus()));
        holder.cancelButton.setVisibility(showCancelButton ? View.VISIBLE : View.GONE);
        holder.cancelButton.setOnClickListener(v -> listener.onCancelClick(reservation));
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView fieldNameTextView;
        private final TextView dateTextView;
        private final TextView timeTextView;
        private final TextView amountTextView;
        private final TextView statusTextView;
        private final MaterialButton cancelButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            fieldNameTextView = itemView.findViewById(R.id.fieldNameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
}
