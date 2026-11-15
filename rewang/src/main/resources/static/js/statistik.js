const TARGET = 85; // batas persentase pemenuhan untuk status "Sesuai target"
const WARNA = ['#3b82f6', '#60a5fa', '#818cf8', '#38bdf8', '#6366f1'];
const $ = id => document.getElementById(id);
const angka = n => Number(n).toLocaleString('id-ID');
const koma = n => String(n).replace('.', ',');
const persen = (a, b) => b > 0 ? Math.round(a / b * 1000) / 10 : 0;
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const tgl = (d, opsi) => new Date(d).toLocaleDateString('id-ID', opsi);

let grafikTren, grafikJenis, dataTerakhir;

function isoHariIni(offset = 0) {
  const d = new Date(); d.setDate(d.getDate() + offset);
  return d.toISOString().slice(0, 10);
}

async function ambil(url) {
  const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
  if (!res.ok) throw new Error('Gagal memuat data (' + res.status + ')');
  return res.json();
}

async function muatOpsi() {
  const o = await ambil('/api/statistik/opsi');
  o.wilayah.forEach(w => $('lokasi').add(new Option(w, w)));
  o.bencana.forEach(b => $('bencana').add(new Option(b.nama, b.id)));
}

async function muatStatistik() {
  const q = new URLSearchParams({ start: $('start').value, end: $('end').value });
  if ($('lokasi').value) q.set('wilayah', $('lokasi').value);
  if ($('bencana').value) q.set('bencanaId', $('bencana').value);
  try {
    dataTerakhir = await ambil('/api/statistik?' + q);
    tampil(dataTerakhir);
  } catch (e) {
    $('foot').textContent = e.message;
  }
}

function tampil(d) {
  const r = d.ringkasan;
  $('kSetuju').textContent = angka(r.disetujui);
  $('kPenuhi').textContent = koma(persen(r.disetujui, r.diminta)) + '%';
  $('kPenuhiNote').textContent = angka(r.disetujui) + ' dari ' + angka(r.diminta) + ' unit diminta';
  $('kJam').textContent = koma(r.rataVerifikasiJam) + ' jam';
  $('kStok').textContent = angka(r.stokMenipis);

  // tren per pekan
  grafikTren?.destroy();
  grafikTren = new Chart($('cTren'), {
    type: 'line',
    data: { labels: d.tren.map(t => tgl(t.pekan, { day: 'numeric', month: 'short' })), datasets: [
      { label: 'Bantuan disetujui', data: d.tren.map(t => t.disetujui), borderColor: '#3b82f6', backgroundColor: '#3b82f6', tension: .3, pointRadius: 3 },
      { label: 'Permintaan bantuan', data: d.tren.map(t => t.diminta), borderColor: '#6b7a9c', backgroundColor: '#6b7a9c', tension: .3, pointRadius: 3 }
    ]},
    options: {
      maintainAspectRatio: false,
      plugins: { legend: { position: 'bottom', labels: { color: '#8793b3', boxWidth: 10, usePointStyle: true } } },
      scales: {
        x: { ticks: { color: '#8793b3' }, grid: { display: false } },
        y: { ticks: { color: '#8793b3', precision: 0 }, grid: { color: '#1c2745' }, beginAtZero: true }
      }
    }
  });

  // komposisi jenis bencana
  const total = d.komposisi.reduce((s, j) => s + j.jumlah, 0);
  $('totalLaporan').textContent = angka(total);
  $('subJenis').textContent = total + ' laporan pada periode terpilih';
  grafikJenis?.destroy();
  grafikJenis = new Chart($('cJenis'), {
    type: 'doughnut',
    data: { labels: d.komposisi.map(j => j.jenis), datasets: [{ data: d.komposisi.map(j => j.jumlah), backgroundColor: WARNA, borderColor: '#101830', borderWidth: 3 }] },
    options: { maintainAspectRatio: false, cutout: '72%', plugins: { legend: { display: false } } }
  });
  $('legendJenis').innerHTML = d.komposisi.map((j, i) =>
    `<span><i class="dot" style="background:${WARNA[i % WARNA.length]}"></i>${esc(j.jenis)} &bull; ${Math.round(persen(j.jumlah, total))}%</span>`).join('');

  // tabel per wilayah
  $('tbLokasi').innerHTML = d.wilayah.length ? d.wilayah.map(w => {
    const p = persen(w.disetujui, w.diminta);
    const ok = p >= TARGET;
    return `<tr><td>${esc(w.nama)}</td><td class="num">${angka(w.diminta)}</td><td class="num">${angka(w.disetujui)}</td>
      <td class="num">${koma(p)}%</td>
      <td><span class="tag ${ok ? 'ok' : 'warn'}">${ok ? 'Sesuai target' : 'Perlu dorongan'}</span></td></tr>`;
  }).join('') : '<tr><td colspan="5" class="empty">Belum ada data pada periode ini.</td></tr>';

  // pemenuhan per barang
  $('barang').innerHTML = d.barang.length ? d.barang.map(b => {
    const p = persen(b.disetujui, b.diminta);
    return `<div class="bar-row"><div class="info"><span>${esc(b.nama)}</span><span>${Math.round(p)}%</span></div>
      <div class="track"><div class="fill ${p < TARGET - 15 ? 'low' : ''}" style="width:${Math.min(p, 100)}%"></div></div></div>`;
  }).join('') : '<div class="empty">Belum ada data.</div>';

  $('foot').textContent = 'Data periode ' + tgl($('start').value, { day: 'numeric', month: 'long', year: 'numeric' })
    + ' sampai ' + tgl($('end').value, { day: 'numeric', month: 'long', year: 'numeric' }) + '.';
}

// unduh tabel per lokasi sebagai CSV
$('unduh').addEventListener('click', () => {
  if (!dataTerakhir) return;
  const baris = [['Wilayah', 'Permintaan', 'Disetujui', 'Pemenuhan (%)']]
    .concat(dataTerakhir.wilayah.map(w => [w.nama, w.diminta, w.disetujui, persen(w.disetujui, w.diminta)]));
  const csv = baris.map(r => r.map(c => '"' + String(c).replace(/"/g, '""') + '"').join(',')).join('\n');
  const a = document.createElement('a');
  a.href = URL.createObjectURL(new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' }));
  a.download = 'statistik-' + $('start').value + '_' + $('end').value + '.csv';
  a.click();
  URL.revokeObjectURL(a.href);
});

$('filter').addEventListener('change', muatStatistik);

(async function init() {
  $('start').value = isoHariIni(-30);
  $('end').value = isoHariIni();
  try { await muatOpsi(); } catch (e) { /* filter tetap jalan tanpa opsi */ }
  muatStatistik();
})();
