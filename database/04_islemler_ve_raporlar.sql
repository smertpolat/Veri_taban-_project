BEGIN;

DO $$
BEGIN
    IF current_database() <> 'otel_db' THEN
        RAISE EXCEPTION 'Run this script in otel_db';
    END IF;
END;
$$;

CREATE FUNCTION public.degisikligi_kaydet() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO public.islem_log (tablo_adi, kayit_id, islem, eski_veri, yeni_veri)
    VALUES (TG_TABLE_NAME, CASE WHEN TG_OP = 'DELETE' THEN OLD.id ELSE NEW.id END,
            TG_OP, CASE WHEN TG_OP <> 'INSERT' THEN to_jsonb(OLD) END,
            CASE WHEN TG_OP <> 'DELETE' THEN to_jsonb(NEW) END);
    RETURN NULL;
END;
$$;

CREATE TRIGGER rezervasyon_islem_log
AFTER INSERT OR UPDATE OR DELETE ON public.rezervasyon
FOR EACH ROW EXECUTE FUNCTION public.degisikligi_kaydet();

CREATE TRIGGER odeme_islem_log
AFTER INSERT OR UPDATE OR DELETE ON public.odeme
FOR EACH ROW EXECUTE FUNCTION public.degisikligi_kaydet();

-- A room type may have at most one price per night.
ALTER TABLE public.sezon_fiyati ADD CONSTRAINT sezon_fiyati_tarih_cakismasi
EXCLUDE USING gist (
    oda_tipi_id WITH =,
    daterange(baslangic_tarihi, bitis_tarihi, '[)') WITH &&
);

-- Room number and guest email already have unique indexes from the schema.
CREATE INDEX rezervasyon_tarih_idx ON public.rezervasyon (giris_tarihi, cikis_tarihi);
CREATE INDEX konaklama_hizmeti_konaklama_idx ON public.konaklama_hizmeti (konaklama_id);
CREATE INDEX odeme_islem_zamani_idx ON public.odeme (islem_zamani);
CREATE INDEX temizlik_kaydi_oda_tarih_idx ON public.temizlik_kaydi (oda_id, islem_zamani DESC)
    WHERE durum = 'tamamlandi';

CREATE VIEW public.bugunku_giris_cikis AS
SELECT r.id AS rezervasyon_id, m.ad, m.soyad, o.numara,
       olay.tur, olay.tarih, k.id AS konaklama_id
FROM public.rezervasyon r
JOIN public.misafir m ON m.id = r.misafir_id
JOIN public.oda o ON o.id = r.oda_id
LEFT JOIN public.konaklama k ON k.rezervasyon_id = r.id
CROSS JOIN LATERAL (VALUES ('giris', r.giris_tarihi), ('cikis', r.cikis_tarihi)) olay(tur, tarih)
WHERE olay.tarih = CURRENT_DATE AND r.durum IN ('onayli', 'tamamlandi');

CREATE VIEW public.oda_guncel_durum AS
SELECT o.id, o.numara, o.kat, t.ad AS oda_tipi, o.durum,
       temizlik.son_temizlik_zamani
FROM public.oda o
JOIN public.oda_tipi t ON t.id = o.oda_tipi_id
LEFT JOIN LATERAL (
    SELECT k.islem_zamani AS son_temizlik_zamani
    FROM public.temizlik_kaydi k
    WHERE k.oda_id = o.id AND k.durum = 'tamamlandi'
    ORDER BY k.islem_zamani DESC LIMIT 1
) temizlik ON true;

-- Fetch the result cursor in the same transaction as CALL.
CREATE PROCEDURE public.bos_odalari_listele(
    IN p_giris date, IN p_cikis date, IN p_oda_tipi_id bigint, INOUT sonuc refcursor
)
LANGUAGE plpgsql AS $$
BEGIN
    IF p_giris IS NULL OR p_cikis IS NULL OR p_giris >= p_cikis THEN
        RAISE EXCEPTION 'Gecerli bir tarih araligi girin.' USING ERRCODE = '22023';
    END IF;

    OPEN sonuc FOR
    SELECT o.id, o.numara, t.ad AS oda_tipi, t.kapasite, t.temel_fiyat
    FROM public.oda o
    JOIN public.oda_tipi t ON t.id = o.oda_tipi_id
    WHERE o.oda_tipi_id = p_oda_tipi_id AND o.durum <> 'bakimda'
      AND (p_giris > CURRENT_DATE OR o.durum = 'bos')
      AND NOT EXISTS (
          SELECT 1 FROM public.rezervasyon r
          WHERE r.oda_id = o.id AND r.durum <> 'iptal'
            AND r.giris_tarihi < p_cikis AND p_giris < r.cikis_tarihi
      )
    ORDER BY o.numara;
END;
$$;

CREATE PROCEDURE public.konaklama_tutarini_hesapla(
    IN p_konaklama_id bigint, OUT oda_tutari numeric, OUT hizmet_tutari numeric, OUT toplam numeric
)
LANGUAGE plpgsql AS $$
DECLARE
    kayit record;
BEGIN
    SELECT r.giris_tarihi, r.cikis_tarihi, t.id AS oda_tipi_id, t.temel_fiyat
    INTO STRICT kayit
    FROM public.konaklama k
    JOIN public.rezervasyon r ON r.id = k.rezervasyon_id
    JOIN public.oda o ON o.id = r.oda_id
    JOIN public.oda_tipi t ON t.id = o.oda_tipi_id
    WHERE k.id = p_konaklama_id;

    SELECT sum(coalesce(f.gecelik_fiyat, kayit.temel_fiyat))
    INTO oda_tutari
    FROM generate_series(0, kayit.cikis_tarihi - kayit.giris_tarihi - 1) AS gece(n)
    LEFT JOIN public.sezon_fiyati f ON f.oda_tipi_id = kayit.oda_tipi_id
        AND kayit.giris_tarihi + gece.n >= f.baslangic_tarihi
        AND kayit.giris_tarihi + gece.n < f.bitis_tarihi;

    SELECT coalesce(sum(adet * uygulanan_birim_fiyat), 0)
    INTO hizmet_tutari FROM public.konaklama_hizmeti WHERE konaklama_id = p_konaklama_id;
    toplam := oda_tutari + hizmet_tutari;
END;
$$;

CREATE PROCEDURE public.aylik_doluluk_gelir_raporu(
    IN p_ay date, OUT dolu_oda_gecesi bigint, OUT toplam_oda_gecesi bigint,
    OUT doluluk_yuzdesi numeric, OUT gelir numeric
)
LANGUAGE plpgsql AS $$
DECLARE
    baslangic date := date_trunc('month', p_ay::timestamp)::date;
    bitis date := (baslangic + interval '1 month')::date;
BEGIN
    IF p_ay IS NULL THEN
        RAISE EXCEPTION 'Rapor ayi zorunludur.' USING ERRCODE = '22023';
    END IF;

    -- Occupancy uses reserved nights for stays that actually started.
    SELECT coalesce(sum(least(r.cikis_tarihi, bitis) - greatest(r.giris_tarihi, baslangic)), 0)
    INTO dolu_oda_gecesi
    FROM public.konaklama k JOIN public.rezervasyon r ON r.id = k.rezervasyon_id
    WHERE r.durum <> 'iptal' AND r.giris_tarihi < bitis AND r.cikis_tarihi > baslangic;

    SELECT count(*) * (bitis - baslangic) INTO toplam_oda_gecesi FROM public.oda;
    doluluk_yuzdesi := coalesce(round(100.0 * dolu_oda_gecesi / nullif(toplam_oda_gecesi, 0), 2), 0);

    SELECT coalesce(sum(tutar), 0) INTO gelir FROM public.odeme
    WHERE islem_zamani >= baslangic AND islem_zamani < bitis;
END;
$$;

-- CALL is atomic. The room state trigger runs in the same transaction.
CREATE PROCEDURE public.konaklamayi_kapat(
    IN p_konaklama_id bigint, IN p_personel_id bigint, IN p_odeme_turu text
)
LANGUAGE plpgsql AS $$
DECLARE
    v_rezervasyon_id bigint;
    v_oda_tutari numeric;
    v_hizmet_tutari numeric;
    v_toplam numeric;
BEGIN
    SELECT rezervasyon_id INTO v_rezervasyon_id
    FROM public.konaklama WHERE id = p_konaklama_id AND cikis_zamani IS NULL
    FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Acik konaklama bulunamadi.';
    END IF;

    CALL public.konaklama_tutarini_hesapla(p_konaklama_id, v_oda_tutari, v_hizmet_tutari, v_toplam);

    UPDATE public.konaklama
    SET cikis_zamani = clock_timestamp(), cikis_personel_id = p_personel_id
    WHERE id = p_konaklama_id;

    INSERT INTO public.odeme (konaklama_id, tutar, odeme_turu)
    VALUES (p_konaklama_id, v_toplam, p_odeme_turu);

    UPDATE public.rezervasyon SET durum = 'tamamlandi' WHERE id = v_rezervasyon_id;
END;
$$;

COMMIT;
