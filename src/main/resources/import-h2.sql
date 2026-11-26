-- Datos de prueba de CoWorkHub para H2 en modo MySQL (perfil h2 y pruebas).
-- Es el mismo contenido que import.sql; solo cambia la sintaxis de las fechas relativas.
-- Usuarios de prueba (todos con contrasena 'coworkhub123'): admin, recepcion, ana, bruno.

-- Datos de prueba de CoWorkHub para MySQL 8.
-- Se ejecuta solo con spring.jpa.hibernate.ddl-auto=create o create-drop.
-- Las fechas se calculan a partir de CURDATE() para que las reservas futuras sigan siendo futuras.
-- Usuarios de prueba (todos con contrasena 'coworkhub123'): admin, recepcion, ana, bruno.

INSERT INTO locations (id, name, city, address, opening_time, closing_time) VALUES (1, 'Sede Centro', 'Buenos Aires', 'Av. Corrientes 1234', '08:00:00', '20:00:00');
INSERT INTO locations (id, name, city, address, opening_time, closing_time) VALUES (2, 'Sede Palermo', 'Buenos Aires', 'Av. Santa Fe 4321', '09:00:00', '21:00:00');

INSERT INTO equipment (id, name) VALUES (1, 'Proyector');
INSERT INTO equipment (id, name) VALUES (2, 'Pizarra');
INSERT INTO equipment (id, name) VALUES (3, 'Videoconferencia');
INSERT INTO equipment (id, name) VALUES (4, 'Pantalla TV');

INSERT INTO rooms (id, location_id, name, type, capacity, hourly_rate, active) VALUES (1, 1, 'Sala Aurora', 'MEETING_ROOM', 8, 35.00, 1);
INSERT INTO rooms (id, location_id, name, type, capacity, hourly_rate, active) VALUES (2, 1, 'Sala Belgrano', 'MEETING_ROOM', 12, 45.00, 1);
INSERT INTO rooms (id, location_id, name, type, capacity, hourly_rate, active) VALUES (3, 1, 'Oficina San Martin', 'PRIVATE_OFFICE', 4, 25.00, 1);
INSERT INTO rooms (id, location_id, name, type, capacity, hourly_rate, active) VALUES (4, 2, 'Sala Horizonte', 'MEETING_ROOM', 10, 40.00, 1);
INSERT INTO rooms (id, location_id, name, type, capacity, hourly_rate, active) VALUES (5, 2, 'Escritorio Palermo', 'DESK', 2, 12.00, 1);
INSERT INTO rooms (id, location_id, name, type, capacity, hourly_rate, active) VALUES (6, 2, 'Sala Mirador', 'MEETING_ROOM', 6, 30.00, 0);

INSERT INTO room_equipment (room_id, equipment_id) VALUES (1, 1), (1, 2);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (2, 1), (2, 3);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (3, 2);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (4, 3), (4, 4);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (5, 4);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (6, 1);

INSERT INTO additional_services (id, name, unit_price) VALUES (1, 'Catering basico', 50.00);
INSERT INTO additional_services (id, name, unit_price) VALUES (2, 'Equipo de sonido', 30.00);
INSERT INTO additional_services (id, name, unit_price) VALUES (3, 'Servicio de impresion', 15.00);

INSERT INTO membership_plans (id, name, included_hours_per_month, overage_discount, max_active_bookings) VALUES (1, 'Basico', 10, 0, 2);
INSERT INTO membership_plans (id, name, included_hours_per_month, overage_discount, max_active_bookings) VALUES (2, 'Profesional', 40, 10, 5);
INSERT INTO membership_plans (id, name, included_hours_per_month, overage_discount, max_active_bookings) VALUES (3, 'Corporativo', 100, 20, 10);

INSERT INTO members (id, plan_id, document_number, full_name, email, status) VALUES (1, 2, '30111222', 'Ana Gomez', 'ana.gomez@example.com', 'ACTIVE');
INSERT INTO members (id, plan_id, document_number, full_name, email, status) VALUES (2, 1, '28999888', 'Bruno Diaz', 'bruno.diaz@example.com', 'ACTIVE');
INSERT INTO members (id, plan_id, document_number, full_name, email, status) VALUES (3, 2, '33444555', 'Carla Fernandez', 'carla.fernandez@example.com', 'ACTIVE');
INSERT INTO members (id, plan_id, document_number, full_name, email, status) VALUES (4, 1, '35666777', 'Diego Lopez', 'diego.lopez@example.com', 'ACTIVE');
INSERT INTO members (id, plan_id, document_number, full_name, email, status) VALUES (5, 3, '27123456', 'Elena Ruiz', 'elena.ruiz@example.com', 'SUSPENDED');

INSERT INTO user_accounts (id, username, password, role, member_id) VALUES (1, 'admin', '$2b$10$VtHurbWGP5UkTeaJjYMAe.PPuUPGag6gZXQC0m8OTNs4AsmhOYm..', 'ADMIN', NULL);
INSERT INTO user_accounts (id, username, password, role, member_id) VALUES (2, 'recepcion', '$2b$10$YBtgEbzhmOyfGxzfxYZ.6.YlZFNDsZ85Cdn2V/rZG/2v3OvaDXJWm', 'RECEPTIONIST', NULL);
INSERT INTO user_accounts (id, username, password, role, member_id) VALUES (3, 'ana', '$2b$10$zZ/o40quk6iAs9c13prrau9A7y0aFq7jPNF2DdK5UFKuQjpBNNeSK', 'MEMBER', 1);
INSERT INTO user_accounts (id, username, password, role, member_id) VALUES (4, 'bruno', '$2b$10$eL.lYLgyFfoY4DeapU./3uYCZyDTMqn/ZkNN9.2t0gY7wjM2ENqr6', 'MEMBER', 2);

INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (1, 1, 1, TIMESTAMPADD(HOUR, 10, TIMESTAMPADD(DAY, 1, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 12, TIMESTAMPADD(DAY, 1, CAST(CURRENT_DATE AS TIMESTAMP))), 4, 'PENDING', 70.00, 80.00, 150.00, 0.00, 120);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (2, 2, 2, TIMESTAMPADD(HOUR, 14, TIMESTAMPADD(DAY, 1, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 15, TIMESTAMPADD(DAY, 1, CAST(CURRENT_DATE AS TIMESTAMP))), 3, 'CONFIRMED', 45.00, 30.00, 75.00, 0.00, 60);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (3, 3, 3, TIMESTAMPADD(HOUR, 9, TIMESTAMPADD(DAY, 2, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 11, TIMESTAMPADD(DAY, 2, CAST(CURRENT_DATE AS TIMESTAMP))), 2, 'CONFIRMED', 50.00, 0.00, 50.00, 0.00, 120);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (4, 4, 1, TIMESTAMPADD(HOUR, 16, TIMESTAMPADD(DAY, 2, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 18, TIMESTAMPADD(DAY, 2, CAST(CURRENT_DATE AS TIMESTAMP))), 6, 'PENDING', 80.00, 30.00, 110.00, 0.00, 120);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (5, 2, 4, TIMESTAMPADD(HOUR, 10, TIMESTAMPADD(DAY, 3, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 13, TIMESTAMPADD(DAY, 3, CAST(CURRENT_DATE AS TIMESTAMP))), 8, 'PENDING', 135.00, 115.00, 250.00, 0.00, 180);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (6, 1, 2, TIMESTAMPADD(HOUR, 10, TIMESTAMPADD(DAY, -1, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 12, TIMESTAMPADD(DAY, -1, CAST(CURRENT_DATE AS TIMESTAMP))), 3, 'COMPLETED', 70.00, 30.00, 100.00, 0.00, 120);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (7, 3, 3, TIMESTAMPADD(HOUR, 9, TIMESTAMPADD(DAY, -2, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 10, TIMESTAMPADD(DAY, -2, CAST(CURRENT_DATE AS TIMESTAMP))), 1, 'COMPLETED', 25.00, 0.00, 25.00, 0.00, 60);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (8, 4, 1, TIMESTAMPADD(HOUR, 15, TIMESTAMPADD(DAY, -3, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 17, TIMESTAMPADD(DAY, -3, CAST(CURRENT_DATE AS TIMESTAMP))), 5, 'COMPLETED', 80.00, 0.00, 80.00, 0.00, 120);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (9, 5, 4, TIMESTAMPADD(HOUR, 11, TIMESTAMPADD(DAY, -5, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 12, TIMESTAMPADD(DAY, -5, CAST(CURRENT_DATE AS TIMESTAMP))), 1, 'CANCELLED', 12.00, 0.00, 0.00, 0.00, 0);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (10, 2, 2, TIMESTAMPADD(HOUR, 13, TIMESTAMPADD(DAY, -6, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 15, TIMESTAMPADD(DAY, -6, CAST(CURRENT_DATE AS TIMESTAMP))), 4, 'CANCELLED', 90.00, 50.00, 45.00, 45.00, 120);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (11, 1, 5, TIMESTAMPADD(HOUR, 9, TIMESTAMPADD(DAY, -4, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(MINUTE, 30, TIMESTAMPADD(HOUR, 10, TIMESTAMPADD(DAY, -4, CAST(CURRENT_DATE AS TIMESTAMP)))), 3, 'COMPLETED', 52.50, 0.00, 52.50, 0.00, 90);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (12, 4, 3, TIMESTAMPADD(HOUR, 17, TIMESTAMPADD(DAY, 5, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 19, TIMESTAMPADD(DAY, 5, CAST(CURRENT_DATE AS TIMESTAMP))), 7, 'CONFIRMED', 80.00, 0.00, 80.00, 0.00, 120);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (13, 6, 1, TIMESTAMPADD(HOUR, 10, TIMESTAMPADD(DAY, -7, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 12, TIMESTAMPADD(DAY, -7, CAST(CURRENT_DATE AS TIMESTAMP))), 4, 'COMPLETED', 60.00, 45.00, 105.00, 0.00, 120);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (14, 1, 4, TIMESTAMPADD(HOUR, 10, TIMESTAMPADD(DAY, 2, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 12, TIMESTAMPADD(DAY, 2, CAST(CURRENT_DATE AS TIMESTAMP))), 2, 'CANCELLED', 70.00, 0.00, 0.00, 0.00, 0);
INSERT INTO bookings (id, room_id, member_id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, consumed_minutes) VALUES (15, 5, 2, TIMESTAMPADD(HOUR, 16, TIMESTAMPADD(DAY, -8, CAST(CURRENT_DATE AS TIMESTAMP))), TIMESTAMPADD(HOUR, 17, TIMESTAMPADD(DAY, -8, CAST(CURRENT_DATE AS TIMESTAMP))), 1, 'COMPLETED', 12.00, 0.00, 12.00, 0.00, 60);

INSERT INTO booking_items (id, booking_id, service_id, quantity, applied_unit_price) VALUES (1, 1, 1, 1, 50.00);
INSERT INTO booking_items (id, booking_id, service_id, quantity, applied_unit_price) VALUES (2, 1, 2, 1, 30.00);
INSERT INTO booking_items (id, booking_id, service_id, quantity, applied_unit_price) VALUES (3, 2, 2, 1, 30.00);
INSERT INTO booking_items (id, booking_id, service_id, quantity, applied_unit_price) VALUES (4, 4, 2, 1, 30.00);
INSERT INTO booking_items (id, booking_id, service_id, quantity, applied_unit_price) VALUES (5, 5, 1, 2, 50.00);
INSERT INTO booking_items (id, booking_id, service_id, quantity, applied_unit_price) VALUES (6, 5, 3, 1, 15.00);
INSERT INTO booking_items (id, booking_id, service_id, quantity, applied_unit_price) VALUES (7, 6, 2, 1, 30.00);
INSERT INTO booking_items (id, booking_id, service_id, quantity, applied_unit_price) VALUES (8, 10, 1, 1, 50.00);
INSERT INTO booking_items (id, booking_id, service_id, quantity, applied_unit_price) VALUES (9, 13, 3, 3, 15.00);
