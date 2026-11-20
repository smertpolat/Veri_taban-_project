BEGIN;

DO $$
DECLARE
    tip bigint;
    kisi bigint;
    oda_ucreti numeric;
    hizmet_ucreti numeric;
    toplam numeric;
    dolu bigint;
    kapasite bigint;
    oran numeric;
    gelir numeric;
    onceki_gelir numeric;
    odalar refcursor := 'musait_odalar';
    satir record;
BEGIN
    SELECT id INTO STRICT tip FROM public.oda_tipi WHERE ad = 'Standart';
    SELECT min(id) INTO kisi FROM public.misafir;
    INSERT INTO public.oda (id, numara, kat, oda_tipi_id)
    OVERRIDING SYSTEM VALUE VALUES (-1, 'TEST-901', 9, tip);
    INSERT INTO public.personel (id, ad, soyad, gorev, ise_giris_tarihi)
    OVERRIDING SYSTEM VALUE VALUES (-1, 'Test', 'Personel', 'resepsiyon', CURRENT_DATE);
    INSERT INTO public.rezervasyon
        (id, misafir_id, oda_id, giris_tarihi, cikis_tarihi, kisi_sayisi, durum)
    OVERRIDING SYSTEM VALUE
    VALUES (-1, kisi, -1, DATE '2027-05-31', DATE '2027-06-02', 1, 'onayli');

    CALL public.bos_odalari_listele(DATE '2027-06-01', DATE '2027-06-02', tip, odalar);
    LOOP
        FETCH odalar INTO satir;
        EXIT WHEN NOT FOUND;
        ASSERT satir.id <> -1, 'Reserved room listed as available';
    END LOOP;
    CLOSE odalar;

    CALL public.bos_odalari_listele(DATE '2027-06-02', DATE '2027-06-03', tip, odalar);
    LOOP
        FETCH odalar INTO satir;
        ASSERT FOUND, 'Room missing on departure date';
        EXIT WHEN satir.id = -1;
    END LOOP;
    CLOSE odalar;

    INSERT INTO public.konaklama (id, rezervasyon_id, giris_personel_id)
    OVERRIDING SYSTEM VALUE VALUES (-1, -1, -1);
    INSERT INTO public.konaklama_hizmeti
        (id, konaklama_id, ek_hizmet_id, adet, uygulanan_birim_fiyat)
    OVERRIDING SYSTEM VALUE
    SELECT -1, -1, id, 2, birim_fiyat FROM public.ek_hizmet WHERE ad = 'Kahvalti';

    CALL public.konaklama_tutarini_hesapla(-1, oda_ucreti, hizmet_ucreti, toplam);
    ASSERT oda_ucreti = 4050 AND hizmet_ucreti = 500 AND toplam = 4550,
        'Unexpected seasonal room or service charge';

    BEGIN
        CALL public.konaklamayi_kapat(-1, -1, 'gecersiz');
        RAISE EXCEPTION 'Invalid payment unexpectedly accepted';
    EXCEPTION WHEN check_violation THEN
        NULL;
    END;

    ASSERT EXISTS (SELECT 1 FROM public.konaklama WHERE id = -1 AND cikis_zamani IS NULL)
        AND EXISTS (SELECT 1 FROM public.oda WHERE id = -1 AND durum = 'dolu')
        AND NOT EXISTS (SELECT 1 FROM public.odeme WHERE konaklama_id = -1)
        AND EXISTS (SELECT 1 FROM public.rezervasyon WHERE id = -1 AND durum = 'onayli'),
        'Failed checkout did not roll back';

    CALL public.aylik_doluluk_gelir_raporu(CURRENT_DATE, dolu, kapasite, oran, onceki_gelir);
    CALL public.konaklamayi_kapat(-1, -1, 'kart');
    ASSERT EXISTS (SELECT 1 FROM public.oda_guncel_durum WHERE id = -1 AND durum = 'temizlikte')
        AND EXISTS (SELECT 1 FROM public.konaklama WHERE id = -1 AND cikis_zamani IS NOT NULL)
        AND EXISTS (SELECT 1 FROM public.odeme WHERE konaklama_id = -1 AND tutar = 4550)
        AND EXISTS (SELECT 1 FROM public.rezervasyon WHERE id = -1 AND durum = 'tamamlandi'),
        'Successful checkout did not write all changes';

    ASSERT EXISTS (
        SELECT 1 FROM public.islem_log
        WHERE tablo_adi = 'rezervasyon' AND kayit_id = -1 AND islem = 'UPDATE'
          AND eski_veri->>'durum' = 'onayli' AND yeni_veri->>'durum' = 'tamamlandi'
    ) AND EXISTS (
        SELECT 1 FROM public.islem_log l JOIN public.odeme o ON o.id = l.kayit_id
        WHERE l.tablo_adi = 'odeme' AND o.konaklama_id = -1 AND l.islem = 'INSERT'
    ), 'Missing audit records';

    CALL public.aylik_doluluk_gelir_raporu(CURRENT_DATE, dolu, kapasite, oran, gelir);
    ASSERT gelir = onceki_gelir + 4550, 'Monthly revenue did not include payment';
    CALL public.aylik_doluluk_gelir_raporu(DATE '2027-06-01', dolu, kapasite, oran, gelir);
    ASSERT dolu = 1 AND kapasite = 630 AND oran = 0.16, 'Unexpected monthly occupancy';

    PERFORM 1 FROM public.bugunku_giris_cikis;
END;
$$;

ROLLBACK;
SELECT 'Musaitlik, sezon fiyati, checkout, rollback, log, view ve rapor testleri gecti' AS sonuc;
