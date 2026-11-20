BEGIN;

CREATE TEMP TABLE rezervasyon_olcum ON COMMIT DROP AS
SELECT giris_tarihi + (n * 31) AS giris_tarihi,
       cikis_tarihi + (n * 31) AS cikis_tarihi
FROM public.rezervasyon CROSS JOIN generate_series(0, 499) AS seri(n);

CREATE TEMP TABLE indeks_sonucu (senaryo text, tarama text, sure_ms numeric) ON COMMIT DROP;

ANALYZE rezervasyon_olcum;

DO $$
DECLARE
    plan json;
BEGIN
    EXECUTE $s$EXPLAIN (ANALYZE, FORMAT JSON)
        SELECT * FROM rezervasyon_olcum
        WHERE giris_tarihi = DATE '2027-06-01' AND cikis_tarihi = DATE '2027-06-03'$s$
    INTO plan;
    INSERT INTO indeks_sonucu VALUES (
        'indekssiz', plan->0->'Plan'->>'Node Type',
        (plan->0->>'Execution Time')::numeric
    );
END;
$$;

CREATE INDEX ON rezervasyon_olcum (giris_tarihi, cikis_tarihi);
ANALYZE rezervasyon_olcum;

DO $$
DECLARE
    plan json;
BEGIN
    EXECUTE $s$EXPLAIN (ANALYZE, FORMAT JSON)
        SELECT * FROM rezervasyon_olcum
        WHERE giris_tarihi = DATE '2027-06-01' AND cikis_tarihi = DATE '2027-06-03'$s$
    INTO plan;
    INSERT INTO indeks_sonucu VALUES (
        'indeksli', plan->0->'Plan'->>'Node Type',
        (plan->0->>'Execution Time')::numeric
    );
END;
$$;

SELECT (SELECT count(*) FROM rezervasyon_olcum) AS kayit, senaryo, tarama, sure_ms
FROM indeks_sonucu;
ROLLBACK;
