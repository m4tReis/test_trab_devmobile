ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
ALTER TABLE users ADD CONSTRAINT uk_users_document UNIQUE (document);

ALTER TABLE soccer_fields ADD CONSTRAINT ck_soccer_fields_active CHECK (active IN (0, 1));

ALTER TABLE reservation_slots ADD CONSTRAINT fk_reservation_slots_field
    FOREIGN KEY (field_id) REFERENCES soccer_fields (id);
ALTER TABLE reservation_slots ADD CONSTRAINT ck_reservation_slots_active CHECK (active IN (0, 1));
ALTER TABLE reservation_slots ADD CONSTRAINT ck_reservation_slots_price CHECK (price >= 0);
ALTER TABLE reservation_slots ADD CONSTRAINT uk_reservation_slots_field_time
    UNIQUE (field_id, start_time, end_time);

ALTER TABLE reservations ADD CONSTRAINT fk_reservations_user
    FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE reservations ADD CONSTRAINT fk_reservations_field
    FOREIGN KEY (field_id) REFERENCES soccer_fields (id);
ALTER TABLE reservations ADD CONSTRAINT fk_reservations_slot
    FOREIGN KEY (slot_id) REFERENCES reservation_slots (id);
ALTER TABLE reservations ADD CONSTRAINT ck_reservations_status CHECK (status IN ('ACTIVE', 'CANCELED'));
ALTER TABLE reservations ADD CONSTRAINT uk_reservations_active_slot
    UNIQUE (active_field_id, active_reservation_date, active_slot_id);
