INSERT INTO currency_rates (currency_id, rate, timestamp)
VALUES ((SELECT id FROM currencies WHERE code = 'USD'), 1.0000, CURRENT_TIMESTAMP);
INSERT INTO currency_rates (currency_id, rate, timestamp)
VALUES ((SELECT id FROM currencies WHERE code = 'UAH'), 44.5000, CURRENT_TIMESTAMP);
