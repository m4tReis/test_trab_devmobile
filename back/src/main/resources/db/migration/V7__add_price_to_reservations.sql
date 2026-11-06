-- Modelo "reserva": a reserva guarda uma copia do valor no momento da confirmacao,
-- para que uma alteracao posterior de preco nao modifique o historico.

ALTER TABLE reservations ADD (price NUMBER(10, 2));

UPDATE reservations r
   SET r.price = (SELECT rs.price FROM reservation_slots rs WHERE rs.id = r.slot_id);

ALTER TABLE reservations MODIFY (price NOT NULL);

ALTER TABLE reservations ADD CONSTRAINT ck_reservations_price CHECK (price >= 0);
