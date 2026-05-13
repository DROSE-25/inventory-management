CREATE TABLE IF NOT EXISTS abc_xyz_results (
    id               BIGSERIAL PRIMARY KEY,
    product_id       BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    abc_class        VARCHAR(1) NOT NULL CHECK (abc_class IN ('A','B','C')),
    xyz_class        VARCHAR(1) NOT NULL CHECK (xyz_class IN ('X','Y','Z')),
    combined_class   VARCHAR(2) NOT NULL,
    revenue          NUMERIC(14,2),
    revenue_share    NUMERIC(6,4),
    cv               NUMERIC(8,2),
    period_from      DATE,
    period_to        DATE,
    calculated_at    TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_abc_xyz_product  ON abc_xyz_results(product_id);
CREATE INDEX IF NOT EXISTS idx_abc_xyz_abc      ON abc_xyz_results(abc_class);
CREATE INDEX IF NOT EXISTS idx_abc_xyz_combined ON abc_xyz_results(combined_class);