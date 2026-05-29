-- V7__add_registration_support.sql
-- Додає підтримку самостійної реєстрації першого адміна

-- Таблиця компаній (тенанти)
CREATE TABLE companies (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200)  NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_companies_name UNIQUE (name)
);

-- Додаємо company_id до users (nullable для зворотньої сумісності)
ALTER TABLE users
    ADD COLUMN company_id BIGINT REFERENCES companies(id) ON DELETE SET NULL,
    ADD COLUMN full_name  VARCHAR(100);

CREATE INDEX idx_users_company ON users(company_id);

-- Прапорець: чи вже є хоча б один адмін (для першого запуску)
-- Використовується в /api/auth/register щоб перший юзер ставав ADMIN
