# Gambar kendaraan (Nano Banana)

Semua gambar memakai gaya yang sama dengan `Gemini_Generated_Image_in4k9ein4k9ein4k.jpg` (Vario): render 3D, abu-abu silver doff, serong depan-kanan ±35°, latar putih polos.

## Cara pakai

1. Salin **prompt dasar** di bawah, lalu ganti `[MODEL]` dengan deskripsi dari tabel.
2. Simpan hasilnya di folder `art/` dengan nama file sesuai kolom **File**, misalnya `art/honda_beat.jpg`.
3. Proses jadi WebP transparan (atau minta Claude melakukannya):

   ```powershell
   python tools/cutout.py art/honda_beat.jpg app/src/main/res/drawable-nodpi/vehicle_honda_beat.webp
   ```

4. Build ulang. Aplikasi otomatis memakai `vehicle_<file>.webp` untuk model itu, tanpa ubah kode.

Gambar per jenis (dipakai bila model belum punya gambar sendiri): `vehicle_kind_matic` (sudah ada, Vario), `vehicle_kind_manual`, `vehicle_kind_sport`, `vehicle_kind_electric`, `vehicle_kind_car`. Mobil tanpa gambar tampil sebagai siluet abu.

## Prompt dasar

```
A 3D product render of [MODEL], three-quarter front-right view (front of the vehicle on the right, rotated about 35 degrees toward the camera), camera slightly above eye level.
Stylized clean 3D look like a premium app illustration: matte silver-grey paint with soft satin reflections, black trim, dark grey wheels, clear headlights.
Soft studio lighting from the upper left, gentle rim light, subtle ambient occlusion, crisp edges.
Isolated on a pure flat white background (#FFFFFF), no floor, no shadow, no reflection on the ground.
No text, no logos, no brand badges, no license plate, no stickers, no watermark.
The whole vehicle fully visible and centered, filling about 85% of the image width, nothing cropped. Aspect ratio 4:3, high resolution.
```

Untuk mobil, tambahkan: `tinted dark windows, the whole car including all four wheels visible`.

## Mode edit foto (hasil paling mirip)

Foto referensi tiap model ada di `art/image/<file>.jpg` (foto resmi pabrikan/dealer, **hanya untuk referensi**, hapus setelah selesai; jangan dimasukkan ke aplikasi).

**Lampirkan SATU gambar saja**: foto model dari `art/image/`. Jangan ikut melampirkan render Vario. Dengan dua lampiran, Nano Banana sering bingung mana yang harus diubah dan malah mengembalikan salah satu gambar lampiran. Gaya Vario sudah dijelaskan lewat teks di prompt.

Ganti `[NAMA MODEL]` dengan nama dari tabel (misalnya `Honda BeAT`), lalu kirim:

```
Use this photo only as a shape reference. Draw a new, original 3D product render of this [NAMA MODEL].
Match the vehicle's body shape, proportions, headlights, body panels, seat and wheels.
Matte silver-grey paint with soft satin reflections, plain unbranded body panels, black trim and seat, dark grey wheels.
Three-quarter front-right view: front of the vehicle on the right side of the picture, turned about 35 degrees toward the camera, camera slightly above eye level.
Soft studio lighting from the upper left, gentle rim light, crisp edges.
Pure flat white background (#FFFFFF), no floor, no shadow, no scenery, no text.
The whole vehicle fully visible and centered, filling about 85% of the image width. Aspect ratio 4:3.
```

Jangan pakai kata "remove logos", "remove watermark" atau "edit this photo": Nano Banana menolaknya ("Sorry, I can't generate unsafe images") karena dianggap menghapus tanda kepemilikan foto orang lain. Kalau masih ditolak, pakai **prompt dasar** tanpa lampiran foto.

Tips kalau hasilnya masih salah:

- **Buka chat baru untuk setiap model.** Di chat yang sama, Nano Banana cenderung mengulang gambar sebelumnya, misalnya Vario lagi.
- Kalau yang keluar foto aslinya tanpa perubahan, balas: `That is the original photo. Now output the edited render: silver-grey, white background, front-right view.`
- Kalau sudutnya susah diputar (foto menghadap kiri), boleh tulis `mirror the vehicle horizontally so the front faces right` di akhir prompt.

Foto yang menghadap kiri (Nano Banana perlu memutarnya): `honda_genio`, `honda_cb150r_streetfire`, `vespa_sprint`, `alva_one`, `mitsubishi_xpander`, `daihatsu_sigra`, `toyota_rush`, `suzuki_ertiga`, `honda_hr_v`, `wuling_air_ev`. Foto Yamaha sudah berlatar putih dengan sudut yang sama seperti Vario, jadi hasilnya biasanya paling rapi.

Gambar per jenis memakai referensi model wakilnya: `kind_manual` ← `honda_supra_x_125`, `kind_sport` ← `yamaha_r15`, `kind_electric` ← `alva_one`, `kind_car` ← `toyota_avanza`.

## Prioritas 1: paling banyak di jalan

| File | [MODEL] |
|---|---|
| `kind_manual` | a Honda Supra X 125 style underbone motorcycle (motor bebek) |
| `kind_sport` | a Yamaha R15 style faired sport motorcycle |
| `kind_electric` | a modern Indonesian electric scooter like an Alva One |
| `kind_car` | a Toyota Avanza style 7-seat MPV |
| `honda_beat` | a Honda BeAT style small automatic scooter |
| `honda_scoopy` | a Honda Scoopy style retro round-body automatic scooter |
| `honda_vario_125` | a Honda Vario 125 style sporty automatic scooter |
| `honda_pcx_160` | a Honda PCX 160 style maxi scooter with flat floorboard |
| `yamaha_nmax_155` | a Yamaha NMAX 155 style maxi scooter |
| `yamaha_aerox_155` | a Yamaha Aerox 155 style sporty scooter with high tail |
| `yamaha_fazzio` | a Yamaha Fazzio style boxy retro automatic scooter |
| `toyota_avanza` | a Toyota Avanza style 7-seat MPV |
| `mitsubishi_xpander` | a Mitsubishi Xpander style crossover MPV with split headlights |
| `honda_brio` | a Honda Brio style small city hatchback |
| `daihatsu_sigra` | a Daihatsu Sigra style compact 7-seat MPV |

## Prioritas 2

| File | [MODEL] |
|---|---|
| `honda_genio` | a Honda Genio style compact automatic scooter |
| `honda_stylo_160` | a Honda Stylo 160 style neo-retro scooter with round headlight |
| `honda_adv_160` | a Honda ADV 160 style adventure scooter with tall windscreen |
| `yamaha_mio_m3` | a Yamaha Mio M3 style small automatic scooter |
| `yamaha_gear_125` | a Yamaha Gear 125 style practical scooter with front basket hook |
| `yamaha_xmax` | a Yamaha XMAX style large maxi scooter |
| `suzuki_nex_ii` | a Suzuki Nex II style small automatic scooter |
| `vespa_sprint` | a Vespa Sprint style classic steel-body scooter |
| `honda_supra_x_125` | a Honda Supra X 125 style underbone motorcycle |
| `yamaha_mx_king_150` | a Yamaha MX King 150 style sporty underbone motorcycle |
| `honda_cb150r_streetfire` | a Honda CB150R style naked streetfighter motorcycle |
| `kawasaki_ninja_250` | a Kawasaki Ninja 250 style twin-cylinder sport bike |
| `toyota_kijang_innova_zenix` | a Toyota Kijang Innova Zenix style large MPV |
| `toyota_rush` | a Toyota Rush style compact 7-seat SUV |
| `toyota_fortuner` | a Toyota Fortuner style ladder-frame SUV |
| `suzuki_ertiga` | a Suzuki Ertiga style compact MPV |
| `honda_hr_v` | a Honda HR-V style compact crossover SUV |
| `wuling_air_ev` | a Wuling Air ev style tiny two-door electric city car |
| `suzuki_carry_pick_up` | a Suzuki Carry style small pickup truck with open bed |
| `yamaha_r15` | a Yamaha R15 style faired sport motorcycle |
| `alva_one` | an Alva One style modern electric scooter |
| `toyota_agya` | a Toyota Agya style small city hatchback |
| `daihatsu_xenia` | a Daihatsu Xenia style 7-seat MPV |

Nama file selalu `merek_model` dalam huruf kecil, spasi dan tanda baca jadi `_` (contoh: "Honda EM1 e:" → `honda_em1_e`). Daftar lengkap merek dan model ada di `app/src/main/java/id/cekendaraan/app/data/Catalog.kt`.
