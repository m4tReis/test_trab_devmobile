INSERT INTO reservation_slots (field_id, start_time, end_time, price, active, created_at, updated_at)
SELECT sf.id, slots.start_time, slots.end_time, slots.price, 1, SYSTIMESTAMP, SYSTIMESTAMP
FROM soccer_fields sf
CROSS JOIN (
    SELECT '08:00' AS start_time, '09:00' AS end_time, 120.00 AS price FROM dual
    UNION ALL SELECT '09:00', '10:00', 120.00 FROM dual
    UNION ALL SELECT '10:00', '11:00', 120.00 FROM dual
    UNION ALL SELECT '11:00', '12:00', 120.00 FROM dual
    UNION ALL SELECT '14:00', '15:00', 140.00 FROM dual
    UNION ALL SELECT '15:00', '16:00', 140.00 FROM dual
    UNION ALL SELECT '16:00', '17:00', 140.00 FROM dual
    UNION ALL SELECT '17:00', '18:00', 160.00 FROM dual
    UNION ALL SELECT '18:00', '19:00', 180.00 FROM dual
    UNION ALL SELECT '19:00', '20:00', 180.00 FROM dual
    UNION ALL SELECT '20:00', '21:00', 180.00 FROM dual
    UNION ALL SELECT '21:00', '22:00', 160.00 FROM dual
) slots
WHERE sf.name IN ('Campo 1', 'Campo 2', 'Campo Society');
