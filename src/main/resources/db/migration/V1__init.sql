-- V1__init.sql: initial schema for settlement application

CREATE TABLE seller (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(80) NOT NULL,
  business_no VARCHAR(40) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE orders (
  id BIGSERIAL PRIMARY KEY,
  seller_id BIGINT NOT NULL REFERENCES seller(id),
  status VARCHAR(20) NOT NULL,
  paid_amount NUMERIC(19,2) NOT NULL,
  paid_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_orders_seller_paidat ON orders(seller_id, paid_at);

CREATE TABLE refund (
  id BIGSERIAL PRIMARY KEY,
  seller_id BIGINT NOT NULL REFERENCES seller(id),
  order_id BIGINT NOT NULL REFERENCES orders(id),
  status VARCHAR(20) NOT NULL,
  refunded_amount NUMERIC(19,2) NOT NULL,
  refunded_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_refund_seller_refundedat ON refund(seller_id, refunded_at);

CREATE TABLE settlement (
  id BIGSERIAL PRIMARY KEY,
  seller_id BIGINT NOT NULL REFERENCES seller(id),
  settlement_date DATE NOT NULL,
  version INTEGER NOT NULL,
  status VARCHAR(30) NOT NULL,
  gross_amount NUMERIC(19,2) NOT NULL,
  refund_amount NUMERIC(19,2) NOT NULL,
  fee_amount NUMERIC(19,2) NOT NULL,
  net_amount NUMERIC(19,2) NOT NULL,
  calculated_at TIMESTAMP WITH TIME ZONE,
  confirmed_at TIMESTAMP WITH TIME ZONE,
  confirmed_by VARCHAR(80)
);
CREATE UNIQUE INDEX uq_settlement_seller_date_version ON settlement(seller_id, settlement_date, version);
CREATE INDEX idx_settlement_date_status ON settlement(settlement_date, status);

CREATE TABLE settlement_item (
  id BIGSERIAL PRIMARY KEY,
  settlement_id BIGINT NOT NULL REFERENCES settlement(id),
  type VARCHAR(255) NOT NULL,
  order_id BIGINT,
  refund_id BIGINT,
  amount NUMERIC(19,2) NOT NULL
);
CREATE INDEX idx_settlement_item_settlement ON settlement_item(settlement_id);

-- Flyway metadata table is handled by flyway

