BEGIN;

DO $$
BEGIN
    IF current_database() <> 'otel_db' THEN
        RAISE EXCEPTION 'Run this script in otel_db (current database: %)', current_database();
    END IF;
END;
$$;

CREATE EXTENSION IF NOT EXISTS btree_gist;

-- The upper bound is exclusive: a departure date can be the next arrival date.
-- The exclusion constraint also protects against concurrent inserts and updates.
ALTER TABLE public.rezervasyon
    ADD CONSTRAINT rezervasyon_oda_tarih_cakismasi
    EXCLUDE USING gist (
        oda_id WITH =,
        daterange(giris_tarihi, cikis_tarihi, '[)') WITH &&
    ) WHERE (durum <> 'iptal');

CREATE FUNCTION public.rezervasyon_cakismasini_kontrol_et()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.durum <> 'iptal' AND EXISTS (
        SELECT 1
        FROM public.rezervasyon AS r
        WHERE r.oda_id = NEW.oda_id
          AND r.id IS DISTINCT FROM NEW.id
          AND r.durum <> 'iptal'
          AND r.giris_tarihi < NEW.cikis_tarihi
          AND NEW.giris_tarihi < r.cikis_tarihi
    ) THEN
        RAISE EXCEPTION 'Oda % icin % - % tarihleri arasinda rezervasyon cakismasi var.',
            NEW.oda_id, NEW.giris_tarihi, NEW.cikis_tarihi
            USING ERRCODE = '23P01', CONSTRAINT = 'rezervasyon_oda_tarih_cakismasi';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER rezervasyon_cakismasini_engelle
BEFORE INSERT OR UPDATE OF oda_id, giris_tarihi, cikis_tarihi, durum
ON public.rezervasyon
FOR EACH ROW
EXECUTE FUNCTION public.rezervasyon_cakismasini_kontrol_et();

COMMIT;
