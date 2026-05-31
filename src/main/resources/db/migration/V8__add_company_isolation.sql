-- ============================================================
-- V8__add_company_isolation.sql
-- Додає company_id до всіх сутностей для ізоляції даних між компаніями
-- ============================================================

-- ─── Додаємо company_id до abc_xyz_results ──────────────────
ALTER TABLE abc_xyz_results
    ADD COLUMN company_id BIGINT REFERENCES companies(id) ON DELETE CASCADE;

-- ─── Додаємо company_id до categories ───────────────────────
ALTER TABLE categories
    ADD COLUMN company_id BIGINT REFERENCES companies(id) ON DELETE CASCADE;

-- ─── Додаємо company_id до suppliers ────────────────────────
ALTER TABLE suppliers
    ADD COLUMN company_id BIGINT REFERENCES companies(id) ON DELETE CASCADE;

-- ─── Додаємо company_id до warehouses ───────────────────────
ALTER TABLE warehouses
    ADD COLUMN company_id BIGINT REFERENCES companies(id) ON DELETE CASCADE;

-- ─── Додаємо company_id до products ─────────────────────────
ALTER TABLE products
    ADD COLUMN company_id BIGINT REFERENCES companies(id) ON DELETE CASCADE;

-- ─── Додаємо company_id до sales ────────────────────────────
ALTER TABLE sales
    ADD COLUMN company_id BIGINT REFERENCES companies(id) ON DELETE CASCADE;

-- ─── Переконуємось що компанія з id=1 існує для seed-даних ──
-- Якщо адмін (з V1) не має company_id — створюємо компанію і прив'язуємо
DO $$
DECLARE
    v_company_id BIGINT;
BEGIN
    -- Перевіряємо чи є вже компанія
    SELECT id INTO v_company_id FROM companies LIMIT 1;

    -- Якщо компаній взагалі немає — створюємо дефолтну
    IF v_company_id IS NULL THEN
        INSERT INTO companies (name) VALUES ('Default Company')
        RETURNING id INTO v_company_id;
    END IF;

    -- Прив'язуємо адміна до цієї компанії (якщо ще не прив'язаний)
    UPDATE users SET company_id = v_company_id WHERE company_id IS NULL;

    -- Прив'язуємо всі наявні дані до цієї компанії
    UPDATE abc_xyz_results SET company_id = v_company_id WHERE company_id IS NULL;
    UPDATE categories  SET company_id = v_company_id WHERE company_id IS NULL;
    UPDATE suppliers   SET company_id = v_company_id WHERE company_id IS NULL;
    UPDATE warehouses  SET company_id = v_company_id WHERE company_id IS NULL;
    UPDATE products    SET company_id = v_company_id WHERE company_id IS NULL;
    UPDATE sales       SET company_id = v_company_id WHERE company_id IS NULL;
END $$;

-- ─── Тепер робимо NOT NULL ───────────────────────────────────
ALTER TABLE categories  ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE suppliers   ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE warehouses  ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE products    ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE sales       ALTER COLUMN company_id SET NOT NULL;

-- ─── Прибираємо старі UNIQUE constraints на name (вони заважають
--     різним компаніям мати однакові назви) ───────────────────
ALTER TABLE categories DROP CONSTRAINT IF EXISTS uq_categories_name;
ALTER TABLE suppliers  DROP CONSTRAINT IF EXISTS uq_suppliers_name;
ALTER TABLE warehouses DROP CONSTRAINT IF EXISTS uq_warehouses_name;
ALTER TABLE products   DROP CONSTRAINT IF EXISTS uq_products_sku;

-- ─── Нові UNIQUE constraints в межах однієї компанії ────────
ALTER TABLE categories ADD CONSTRAINT uq_categories_name_company
    UNIQUE (name, company_id);

ALTER TABLE suppliers ADD CONSTRAINT uq_suppliers_name_company
    UNIQUE (name, company_id);

ALTER TABLE warehouses ADD CONSTRAINT uq_warehouses_name_company
    UNIQUE (name, company_id);

ALTER TABLE products ADD CONSTRAINT uq_products_sku_company
    UNIQUE (sku, company_id);

-- ─── Індекси для продуктивності ─────────────────────────────
CREATE INDEX idx_abc_xyz_company   ON abc_xyz_results(company_id);
CREATE INDEX idx_categories_company ON categories(company_id);
CREATE INDEX idx_suppliers_company  ON suppliers(company_id);
CREATE INDEX idx_warehouses_company ON warehouses(company_id);
CREATE INDEX idx_products_company   ON products(company_id);
CREATE INDEX idx_sales_company      ON sales(company_id);

-- ============================================================
-- END OF V8__add_company_isolation.sql
-- ============================================================