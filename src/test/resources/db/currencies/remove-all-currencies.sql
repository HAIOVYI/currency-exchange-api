DELETE
FROM currency_rates
WHERE currency_id IN (SELECT id FROM currencies);
DELETE
FROM currencies;
