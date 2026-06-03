ALTER TABLE bookings
    ADD COLUMN confirmation_token VARCHAR(36) UNIQUE;