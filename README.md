# Otel Rezervasyon ve Konaklama Yönetim Sistemi

Projenin veritabanı PostgreSQL, uygulama katmanı Spring Boot olarak planlandı.

## PostgreSQL kurulumu

DBeaver'da `otel_db` veritabanına bağlı bir SQL editörü açıp [şema ve örnek veri betiğini](database/01_sema_ve_ornek_veri.sql) boş veritabanında bir kez çalıştır. Betik 13 tabloyu, 20 odayı, 50 misafiri ve 200 rezervasyonu tek işlemde oluşturur.

Şema kurulduktan sonra aynı veritabanında [rezervasyon çakışma kuralını](database/02_rezervasyon_cakisma_kurali.sql) çalıştır. Bu betik için `btree_gist` eklentisinin PostgreSQL sunucusunda bulunması ve kurulum hesabının eklentiyi oluşturabilmesi gerekir. Çıkış tarihi aralığa dahil edilmez. İptal edilen rezervasyonlar odayı bloke etmez.

## ER diyagramı

```mermaid
erDiagram
    ODA_TIPI ||--o{ ODA : siniflandirir
    ODA_TIPI ||--o{ SEZON_FIYATI : fiyatlandirilir
    ODA ||--o{ REZERVASYON : ayrilir
    MISAFIR ||--o{ REZERVASYON : yapar
    MISAFIR o|--o| KULLANICI : misafir_hesabi
    PERSONEL o|--o| KULLANICI : personel_hesabi
    REZERVASYON ||--o| KONAKLAMA : olusur
    PERSONEL ||--o{ KONAKLAMA : girisi_yapar
    PERSONEL o|--o{ KONAKLAMA : cikisi_yapar
    KONAKLAMA ||--o{ KONAKLAMA_HIZMETI : icerir
    EK_HIZMET ||--o{ KONAKLAMA_HIZMETI : kullanilir
    KONAKLAMA ||--o{ ODEME : odenir
    ODA ||--o{ TEMIZLIK_KAYDI : temizlenir
    PERSONEL ||--o{ TEMIZLIK_KAYDI : kaydeder
    ISLEM_LOG {
        bigint id PK
        text tablo_adi
        bigint kayit_id
        text islem
        timestamptz islem_zamani
        jsonb eski_veri
        jsonb yeni_veri
    }
```

Rezervasyon planlanan tarihleri, konaklama ise gerçekleşen giriş ve çıkış zamanlarını tutar. `konaklama_hizmeti`, kullanılan hizmeti adedi ve işlem anındaki birim fiyatıyla kaydeder. `islem_log`, farklı tablolardan gelen işlem geçmişini saklamak için kaynak tablo adını ve kayıt kimliğini tutar.
