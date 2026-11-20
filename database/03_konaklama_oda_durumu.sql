BEGIN;

DO $$
BEGIN
    IF current_database() <> 'otel_db' THEN
        RAISE EXCEPTION 'Run this script in otel_db (current database: %)', current_database();
    END IF;
END;
$$;

CREATE FUNCTION public.konaklamada_oda_durumunu_guncelle()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.cikis_zamani IS NOT NULL THEN
            RAISE EXCEPTION 'Check-in kaydi acik olmalidir.';
        END IF;

        UPDATE public.oda AS o
        SET durum = 'dolu'
        FROM public.rezervasyon AS r
        WHERE r.id = NEW.rezervasyon_id
          AND r.durum = 'onayli'
          AND o.id = r.oda_id
          AND o.durum = 'bos';

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Check-in icin onayli rezervasyon ve bos oda gerekir.';
        END IF;

        RETURN NEW;
    END IF;

    IF NEW.rezervasyon_id IS DISTINCT FROM OLD.rezervasyon_id THEN
        RAISE EXCEPTION 'Konaklamanin rezervasyonu degistirilemez.';
    END IF;

    IF OLD.cikis_zamani IS NOT NULL AND NEW.cikis_zamani IS NULL THEN
        RAISE EXCEPTION 'Kapatilmis konaklama yeniden acilamaz.';
    END IF;

    IF OLD.cikis_zamani IS NULL AND NEW.cikis_zamani IS NOT NULL THEN
        UPDATE public.oda AS o
        SET durum = 'temizlikte'
        FROM public.rezervasyon AS r
        WHERE r.id = NEW.rezervasyon_id
          AND o.id = r.oda_id
          AND o.durum = 'dolu';

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Check-out icin odanin durumu dolu olmalidir.';
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER konaklama_oda_durumu
AFTER INSERT OR UPDATE OF cikis_zamani, rezervasyon_id
ON public.konaklama
FOR EACH ROW
EXECUTE FUNCTION public.konaklamada_oda_durumunu_guncelle();

COMMIT;
