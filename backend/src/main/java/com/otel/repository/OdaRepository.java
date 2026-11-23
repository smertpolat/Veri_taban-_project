package com.otel.repository;

import com.otel.entity.MusaitOda;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OdaRepository {
    private final JdbcTemplate jdbc;

    public OdaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MusaitOda> musaitOdalariBul(LocalDate giris, LocalDate cikis, long odaTipiId) {
        jdbc.queryForObject(
                "CALL public.bos_odalari_listele(?, ?, ?, 'musait_odalar'::refcursor)",
                String.class, giris, cikis, odaTipiId);
        List<MusaitOda> odalar = jdbc.query("FETCH ALL FROM musait_odalar", (satir, satirNo) ->
                new MusaitOda(satir.getLong("id"), satir.getString("numara"), satir.getString("oda_tipi"),
                        satir.getInt("kapasite"), satir.getBigDecimal("temel_fiyat")));
        jdbc.execute("CLOSE musait_odalar");
        return odalar;
    }
}
