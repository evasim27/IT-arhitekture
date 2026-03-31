CREATE TABLE IF NOT EXISTS stores (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS prices (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    value NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_prices_store FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE,
    CONSTRAINT uq_product_store UNIQUE (product_id, store_id)
);