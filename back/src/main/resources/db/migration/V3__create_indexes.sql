CREATE INDEX idx_reservations_user_status_date
    ON reservations (user_id, status, reservation_date);

CREATE INDEX idx_reservations_user_date
    ON reservations (user_id, reservation_date);

CREATE INDEX idx_reservations_date_status
    ON reservations (reservation_date, status);

CREATE INDEX idx_reservations_field_date_slot_status
    ON reservations (field_id, reservation_date, slot_id, status);

CREATE INDEX idx_reservations_slot_date
    ON reservations (slot_id, reservation_date);

CREATE INDEX idx_reservation_slots_field_active
    ON reservation_slots (field_id, active);

CREATE INDEX idx_reservation_slots_active_start
    ON reservation_slots (active, start_time);
