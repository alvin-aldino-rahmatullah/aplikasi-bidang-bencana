package com.example.rewang.repository;

import com.example.rewang.dto.StatistikResponse.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Query statistik untuk halaman admin. Tabel mengikuti database bantuan_bencana:
 * laporan, bencana, kebutuhan_bantuan, barang. Laporan berstatus draft tidak dihitung.
 */
@Repository
public class StatistikRepository {

    private static final String KONDISI =
            " WHERE l.status <> :draft AND l.created_at >= :start AND l.created_at < :end"
          + " AND (:wilayah IS NULL OR b.lokasi = :wilayah)"
          + " AND (:bencanaId IS NULL OR l.bencana_id = :bencanaId)";

    private static final String DARI_LAPORAN =
            " FROM laporan l JOIN bencana b ON b.id = l.bencana_id" + KONDISI;

    private static final String DARI_KEBUTUHAN =
            " FROM kebutuhan_bantuan kb"
          + " JOIN laporan l ON l.id = kb.laporan_id"
          + " JOIN bencana b ON b.id = l.bencana_id"
          + " JOIN barang br ON br.id = kb.barang_id" + KONDISI;

    private static final String SUM =
            "COALESCE(SUM(kb.jumlah_diminta),0) AS diminta, COALESCE(SUM(kb.jumlah_disetujui),0) AS setuju";

    private final NamedParameterJdbcTemplate jdbc;

    public StatistikRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private MapSqlParameterSource params(LocalDate start, LocalDate end, String wilayah, Long bencanaId) {
        return new MapSqlParameterSource()
                .addValue("draft", "draft")
                .addValue("start", start.atStartOfDay())
                .addValue("end", end.plusDays(1).atStartOfDay())
                .addValue("wilayah", wilayah == null || wilayah.isBlank() ? null : wilayah)
                .addValue("bencanaId", bencanaId);
    }

    private RowMapper<Capaian> capaian() {
        return (rs, i) -> new Capaian(rs.getString("nama"), rs.getLong("diminta"), rs.getLong("setuju"));
    }

    public Ringkasan ringkasan(LocalDate start, LocalDate end, String wilayah, Long bencanaId) {
        var p = params(start, end, wilayah, bencanaId);

        var total = jdbc.queryForObject("SELECT " + SUM + DARI_KEBUTUHAN, p,
                (rs, i) -> new long[]{rs.getLong("diminta"), rs.getLong("setuju")});

        Double jam = jdbc.queryForObject(
                "SELECT AVG(TIMESTAMPDIFF(MINUTE, l.created_at, l.verified_at)) / 60.0"
              + DARI_LAPORAN + " AND l.verified_at IS NOT NULL", p, Double.class);

        // stok menipis adalah kondisi saat ini, tidak terpengaruh filter periode
        Long menipis = jdbc.getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM barang WHERE stok <= stok_minimum", Long.class);

        return new Ringkasan(total[1], total[0],
                jam == null ? 0 : Math.round(jam * 10) / 10.0,
                menipis == null ? 0 : menipis);
    }

    public List<Tren> tren(LocalDate start, LocalDate end, String wilayah, Long bencanaId) {
        return jdbc.query(
                "SELECT DATE(l.created_at - INTERVAL WEEKDAY(l.created_at) DAY) AS pekan, " + SUM
              + DARI_KEBUTUHAN + " GROUP BY pekan ORDER BY pekan",
                params(start, end, wilayah, bencanaId),
                (rs, i) -> new Tren(rs.getString("pekan"), rs.getLong("diminta"), rs.getLong("setuju")));
    }

    public List<Jenis> komposisi(LocalDate start, LocalDate end, String wilayah, Long bencanaId) {
        return jdbc.query(
                "SELECT b.jenis AS jns, COUNT(*) AS jumlah" + DARI_LAPORAN
              + " GROUP BY b.jenis ORDER BY jumlah DESC",
                params(start, end, wilayah, bencanaId),
                (rs, i) -> new Jenis(rs.getString("jns"), rs.getLong("jumlah")));
    }

    public List<Capaian> perWilayah(LocalDate start, LocalDate end, String wilayah, Long bencanaId) {
        return jdbc.query(
                "SELECT b.lokasi AS nama, " + SUM + DARI_KEBUTUHAN
              + " GROUP BY b.lokasi ORDER BY setuju DESC LIMIT 10",
                params(start, end, wilayah, bencanaId), capaian());
    }

    public List<Capaian> perBarang(LocalDate start, LocalDate end, String wilayah, Long bencanaId) {
        return jdbc.query(
                "SELECT br.nama_barang AS nama, " + SUM + DARI_KEBUTUHAN
              + " GROUP BY br.id, br.nama_barang ORDER BY diminta DESC LIMIT 6",
                params(start, end, wilayah, bencanaId), capaian());
    }

    public Opsi opsi() {
        var wilayah = jdbc.getJdbcTemplate().queryForList(
                "SELECT DISTINCT lokasi FROM bencana WHERE lokasi <> '' ORDER BY lokasi", String.class);
        var bencana = jdbc.getJdbcTemplate().query(
                "SELECT id, nama_bencana FROM bencana ORDER BY nama_bencana",
                (rs, i) -> new Pilihan(rs.getLong("id"), rs.getString("nama_bencana")));
        return new Opsi(wilayah, bencana);
    }
}
