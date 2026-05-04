-- ============================================================
-- V1__init_schema.sql
-- Inventory Management System — Initial Database Schema
-- PostgreSQL 15+
-- ============================================================

-- ─── ENUM TYPES ─────────────────────────────────────────────
CREATE TYPE user_role_enum AS ENUM ('ADMIN', 'MANAGER', 'ANALYST');

CREATE TYPE po_status_enum AS ENUM (
    'DRAFT', 'SUBMITTED', 'CONFIRMED', 'RECEIVED', 'CANCELLED'
);

CREATE TYPE forecast_method_enum AS ENUM (
    'SMA', 'WMA', 'SES', 'HOLT', 'HOLT_WINTERS',
    'LINEAR_REGRESSION', 'ARIMA'
);

-- ─── TABLE: users ────────────────────────────────────────────
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)      NOT NULL,
    password_hash VARCHAR(255)     NOT NULL,
    email         VARCHAR(100)     NOT NULL,
    role          user_role_enum   NOT NULL,
    is_active     BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email)
);

-- ─── TABLE: categories ───────────────────────────────────────
CREATE TABLE categories (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    parent_id BIGINT       REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT uq_categories_name UNIQUE (name)
);

-- ─── TABLE: suppliers ────────────────────────────────────────
CREATE TABLE suppliers (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(150)    NOT NULL,
    contact_person   VARCHAR(100),
    phone            VARCHAR(30),
    email            VARCHAR(100),
    lead_time_days   SMALLINT        NOT NULL CHECK (lead_time_days > 0),
    min_order_amount NUMERIC(12,2)   CHECK (min_order_amount >= 0),
    is_active        BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_suppliers_name UNIQUE (name)
);

-- ─── TABLE: warehouses ───────────────────────────────────────
CREATE TABLE warehouses (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(100)  NOT NULL,
    address   VARCHAR(255),
    capacity  NUMERIC(12,2) CHECK (capacity > 0),
    is_active BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_warehouses_name UNIQUE (name)
);

-- ─── TABLE: products ─────────────────────────────────────────
CREATE TABLE products (
    id                 BIGSERIAL PRIMARY KEY,
    sku                VARCHAR(50)    NOT NULL,
    name               VARCHAR(200)   NOT NULL,
    category_id        BIGINT         NOT NULL REFERENCES categories(id)  ON DELETE RESTRICT,
    supplier_id        BIGINT         NOT NULL REFERENCES suppliers(id)   ON DELETE RESTRICT,
    unit_price         NUMERIC(12,2)  NOT NULL CHECK (unit_price > 0),
    unit_of_measure    VARCHAR(20)    NOT NULL DEFAULT 'шт',
    ordering_cost      NUMERIC(10,2)  NOT NULL DEFAULT 100.00,
    holding_cost_rate  NUMERIC(5,4)   NOT NULL DEFAULT 0.2000
                                      CHECK (holding_cost_rate BETWEEN 0 AND 1),
    service_level      NUMERIC(4,3)   NOT NULL DEFAULT 0.950
                                      CHECK (service_level BETWEEN 0 AND 1),
    is_active          BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_products_sku UNIQUE (sku)
);

-- ─── TABLE: stock_levels ─────────────────────────────────────
CREATE TABLE stock_levels (
    id           BIGSERIAL PRIMARY KEY,
    product_id   BIGINT         NOT NULL REFERENCES products(id)   ON DELETE CASCADE,
    warehouse_id BIGINT         NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    quantity     NUMERIC(12,3)  NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    reorder_point NUMERIC(12,3) CHECK (reorder_point >= 0),
    safety_stock  NUMERIC(12,3) CHECK (safety_stock >= 0),
    eoq           NUMERIC(12,3) CHECK (eoq > 0),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_stock_product_warehouse UNIQUE (product_id, warehouse_id)
);

-- ─── TABLE: sales ────────────────────────────────────────────
CREATE TABLE sales (
    id           BIGSERIAL PRIMARY KEY,
    product_id   BIGINT          NOT NULL REFERENCES products(id)   ON DELETE RESTRICT,
    warehouse_id BIGINT          NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    sale_date    DATE            NOT NULL,
    quantity     NUMERIC(12,3)   NOT NULL CHECK (quantity > 0),
    unit_price   NUMERIC(12,2)   NOT NULL CHECK (unit_price > 0),
    created_by   BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    created_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ─── TABLE: purchase_orders ──────────────────────────────────
CREATE TABLE purchase_orders (
    id                BIGSERIAL PRIMARY KEY,
    supplier_id       BIGINT         NOT NULL REFERENCES suppliers(id)  ON DELETE RESTRICT,
    warehouse_id      BIGINT         NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    status            po_status_enum NOT NULL DEFAULT 'DRAFT',
    order_date        DATE           NOT NULL DEFAULT CURRENT_DATE,
    expected_delivery DATE,
    actual_delivery   DATE,
    total_amount      NUMERIC(14,2)  CHECK (total_amount >= 0),
    created_by        BIGINT         REFERENCES users(id) ON DELETE SET NULL,
    notes             TEXT
);

-- ─── TABLE: purchase_order_items ─────────────────────────────
CREATE TABLE purchase_order_items (
    id                 BIGSERIAL PRIMARY KEY,
    purchase_order_id  BIGINT         NOT NULL
                       REFERENCES purchase_orders(id) ON DELETE CASCADE,
    product_id         BIGINT         NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity_ordered   NUMERIC(12,3)  NOT NULL CHECK (quantity_ordered > 0),
    quantity_received  NUMERIC(12,3)  CHECK (quantity_received >= 0),
    unit_price         NUMERIC(12,2)  NOT NULL CHECK (unit_price > 0)
);

-- ─── TABLE: forecasts ────────────────────────────────────────
CREATE TABLE forecasts (
    id               BIGSERIAL PRIMARY KEY,
    product_id       BIGINT                NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    method           forecast_method_enum  NOT NULL,
    forecast_date    DATE                  NOT NULL,
    horizon_months   SMALLINT              NOT NULL CHECK (horizon_months > 0),
    forecast_value   NUMERIC(14,3)         NOT NULL,
    mae              NUMERIC(10,4),
    mape             NUMERIC(8,4),
    rmse             NUMERIC(10,4),
    parameters       JSONB,
    created_by       BIGINT                REFERENCES users(id) ON DELETE SET NULL,
    created_at       TIMESTAMPTZ           NOT NULL DEFAULT NOW()
);

-- ─── TABLE: abc_xyz_results ──────────────────────────────────
CREATE TABLE abc_xyz_results (
    id             BIGSERIAL PRIMARY KEY,
    product_id     BIGINT         NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    analysis_date  DATE           NOT NULL DEFAULT CURRENT_DATE,
    abc_class      CHAR(1)        NOT NULL CHECK (abc_class IN ('A','B','C')),
    xyz_class      CHAR(1)        NOT NULL CHECK (xyz_class IN ('X','Y','Z')),
    revenue_share  NUMERIC(6,4)   NOT NULL CHECK (revenue_share BETWEEN 0 AND 1),
    cv             NUMERIC(8,4)   NOT NULL CHECK (cv >= 0),
    combined_class VARCHAR(2)     NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_abc_xyz_product_date UNIQUE (product_id, analysis_date)
);

-- ─── INDEXES ─────────────────────────────────────────────────
CREATE INDEX idx_products_category  ON products(category_id);
CREATE INDEX idx_products_supplier  ON products(supplier_id);
CREATE INDEX idx_stock_warehouse    ON stock_levels(warehouse_id);
CREATE INDEX idx_sales_product      ON sales(product_id);
CREATE INDEX idx_sales_date         ON sales(sale_date);
CREATE INDEX idx_sales_product_date ON sales(product_id, sale_date);
CREATE INDEX idx_po_supplier        ON purchase_orders(supplier_id);
CREATE INDEX idx_po_status          ON purchase_orders(status);
CREATE INDEX idx_poi_order          ON purchase_order_items(purchase_order_id);
CREATE INDEX idx_forecast_product        ON forecasts(product_id);
CREATE INDEX idx_forecast_product_method ON forecasts(product_id, method);
CREATE INDEX idx_forecast_product_date   ON forecasts(product_id, forecast_date);
CREATE INDEX idx_axr_class          ON abc_xyz_results(combined_class);

-- ─── SEED: DEFAULT ADMIN USER ────────────────────────────────
INSERT INTO users (username, password_hash, email, role)
VALUES ('admin',
        '$2a$12$xkX1z5Icj7/AkB3hOt0KUORRExSFYNkGMYxhJlFd9nLTEZkFu6kH2',
        'admin@inventory.local',
        'ADMIN');

-- ─── SEED: DEFAULT CATEGORIES ────────────────────────────────
INSERT INTO categories (name, parent_id) VALUES ('Продовольчі товари', NULL);
INSERT INTO categories (name, parent_id) VALUES ('Непродовольчі товари', NULL);
INSERT INTO categories (name, parent_id) VALUES ('Молочна продукція', 1);
INSERT INTO categories (name, parent_id) VALUES ('Хлібобулочні вироби', 1);
INSERT INTO categories (name, parent_id) VALUES ('Побутова хімія', 2);

-- ─── SEED: DEFAULT WAREHOUSE ─────────────────────────────────
INSERT INTO warehouses (name, address, capacity)
VALUES ('Головний склад', 'м. Київ, вул. Складська, 1', 1000.00);

-- ============================================================
-- END OF V1__init_schema.sql
-- ============================================================