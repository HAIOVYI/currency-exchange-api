DELETE FROM currency_rates
WHERE currency_id IN (
    SELECT id FROM currencies WHERE code IN ('USD', 'UAH', 'EUR')
);

DELETE FROM currencies
WHERE code IN ('USD', 'UAH', 'EUR');
