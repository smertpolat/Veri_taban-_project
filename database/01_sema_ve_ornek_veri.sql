BEGIN;

-- Protect other databases when this script is run from DBeaver.
DO $$
BEGIN
    IF current_database() <> 'otel_db' THEN
        RAISE EXCEPTION 'Run this script in otel_db (current database: %)', current_database();
    END IF;
END;
$$;

SET LOCAL search_path TO public;

CREATE TABLE oda_tipi (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ad text NOT NULL UNIQUE,
    kapasite integer NOT NULL CHECK (kapasite > 0),
    aciklama text,
    temel_fiyat numeric(12, 2) NOT NULL CHECK (temel_fiyat >= 0)
);

CREATE TABLE oda (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numara text NOT NULL UNIQUE,
    kat integer NOT NULL,
    oda_tipi_id bigint NOT NULL REFERENCES oda_tipi (id),
    durum text NOT NULL DEFAULT 'bos'
        CHECK (durum IN ('bos', 'dolu', 'temizlikte', 'bakimda'))
);

CREATE TABLE sezon_fiyati (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    oda_tipi_id bigint NOT NULL REFERENCES oda_tipi (id),
    baslangic_tarihi date NOT NULL,
    bitis_tarihi date NOT NULL,
    gecelik_fiyat numeric(12, 2) NOT NULL CHECK (gecelik_fiyat >= 0),
    CHECK (baslangic_tarihi < bitis_tarihi),
    UNIQUE (oda_tipi_id, baslangic_tarihi, bitis_tarihi)
);

CREATE TABLE misafir (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ad text NOT NULL,
    soyad text NOT NULL,
    kimlik_no text NOT NULL UNIQUE,
    telefon text NOT NULL,
    eposta text NOT NULL UNIQUE,
    dogum_tarihi date NOT NULL
);

CREATE TABLE personel (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ad text NOT NULL,
    soyad text NOT NULL,
    gorev text NOT NULL CHECK (gorev IN ('resepsiyon', 'kat_gorevlisi', 'yonetici')),
    ise_giris_tarihi date NOT NULL
);

CREATE TABLE kullanici (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    kullanici_adi text NOT NULL UNIQUE,
    parola_hash text NOT NULL,
    rol text NOT NULL CHECK (rol IN ('misafir', 'resepsiyon', 'yonetici')),
    misafir_id bigint UNIQUE REFERENCES misafir (id),
    personel_id bigint UNIQUE REFERENCES personel (id),
    CHECK (
        (rol = 'misafir' AND misafir_id IS NOT NULL AND personel_id IS NULL)
        OR (rol IN ('resepsiyon', 'yonetici') AND personel_id IS NOT NULL AND misafir_id IS NULL)
    )
);

CREATE TABLE rezervasyon (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    misafir_id bigint NOT NULL REFERENCES misafir (id),
    oda_id bigint NOT NULL REFERENCES oda (id),
    giris_tarihi date NOT NULL,
    cikis_tarihi date NOT NULL,
    kisi_sayisi integer NOT NULL CHECK (kisi_sayisi > 0),
    durum text NOT NULL DEFAULT 'beklemede'
        CHECK (durum IN ('beklemede', 'onayli', 'iptal', 'tamamlandi')),
    olusturma_zamani timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (giris_tarihi < cikis_tarihi)
);

CREATE TABLE konaklama (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rezervasyon_id bigint NOT NULL UNIQUE REFERENCES rezervasyon (id),
    giris_zamani timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cikis_zamani timestamptz,
    giris_personel_id bigint NOT NULL REFERENCES personel (id),
    cikis_personel_id bigint REFERENCES personel (id),
    CHECK (cikis_zamani >= giris_zamani),
    CHECK ((cikis_zamani IS NULL) = (cikis_personel_id IS NULL))
);

CREATE TABLE ek_hizmet (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ad text NOT NULL UNIQUE,
    birim_fiyat numeric(12, 2) NOT NULL CHECK (birim_fiyat >= 0)
);

CREATE TABLE konaklama_hizmeti (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    konaklama_id bigint NOT NULL REFERENCES konaklama (id),
    ek_hizmet_id bigint NOT NULL REFERENCES ek_hizmet (id),
    adet integer NOT NULL CHECK (adet > 0),
    uygulanan_birim_fiyat numeric(12, 2) NOT NULL CHECK (uygulanan_birim_fiyat >= 0),
    islem_zamani timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE odeme (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    konaklama_id bigint NOT NULL REFERENCES konaklama (id),
    tutar numeric(12, 2) NOT NULL CHECK (tutar >= 0),
    odeme_turu text NOT NULL CHECK (odeme_turu IN ('nakit', 'kart')),
    islem_zamani timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE temizlik_kaydi (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    oda_id bigint NOT NULL REFERENCES oda (id),
    personel_id bigint NOT NULL REFERENCES personel (id),
    islem_zamani timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    durum text NOT NULL CHECK (durum IN ('bekliyor', 'devam_ediyor', 'tamamlandi'))
);

CREATE TABLE islem_log (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tablo_adi text NOT NULL,
    kayit_id bigint NOT NULL,
    islem text NOT NULL CHECK (islem IN ('INSERT', 'UPDATE', 'DELETE')),
    islem_zamani timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    eski_veri jsonb,
    yeni_veri jsonb
);

INSERT INTO oda_tipi (ad, kapasite, temel_fiyat)
VALUES ('Standart', 2, 1800), ('Deluxe', 3, 2600), ('Suit', 4, 3900);

INSERT INTO oda (numara, kat, oda_tipi_id)
SELECT (100 + s.no)::text, 1, t.id
FROM generate_series(1, 20) AS s(no)
JOIN oda_tipi t ON t.ad = CASE
    WHEN s.no <= 10 THEN 'Standart'
    WHEN s.no <= 17 THEN 'Deluxe'
    ELSE 'Suit'
END;

-- These records are fictional and use a domain reserved for examples.
INSERT INTO misafir (ad, soyad, kimlik_no, telefon, eposta, dogum_tarihi)
SELECT 'Misafir ' || s.no, 'Ornek', 'TEST-' || s.no,
       '000' || lpad(s.no::text, 7, '0'),
       'misafir' || s.no || '@example.com', DATE '1980-01-01' + s.no * 120
FROM generate_series(1, 50) AS s(no);

INSERT INTO sezon_fiyati (oda_tipi_id, baslangic_tarihi, bitis_tarihi, gecelik_fiyat)
SELECT id, DATE '2027-06-01', DATE '2027-09-01', temel_fiyat * 1.25
FROM oda_tipi;

INSERT INTO ek_hizmet (ad, birim_fiyat)
VALUES ('Kahvalti', 250), ('Spa', 800), ('Otopark', 120), ('Oda servisi', 350);

-- Ten non-overlapping reservations per room, each separated by one night.
INSERT INTO rezervasyon (misafir_id, oda_id, giris_tarihi, cikis_tarihi, kisi_sayisi, durum)
SELECT m.id, o.id, DATE '2027-06-01' + s.tur * 3,
       DATE '2027-06-03' + s.tur * 3, 2, 'onayli'
FROM generate_series(0, 9) AS s(tur)
CROSS JOIN oda o
JOIN misafir m ON m.kimlik_no = 'TEST-' ||
    (1 + ((s.tur * 20 + o.numara::integer - 101) % 50))::text;

COMMIT;
