CREATE TABLE fx_rate (
    id SERIAL PRIMARY KEY,
    date DATE NOT NULL,
    source_currency VARCHAR(3) NOT NULL,
    target_currency VARCHAR(3) NOT NULL,
    rate DECIMAL(18, 6) NOT NULL,
    UNIQUE(date, source_currency, target_currency)
);
