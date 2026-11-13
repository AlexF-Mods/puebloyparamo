# Pueblo y Páramo (`puebloyparamo`)

Mod para Minecraft **26.3** (Fabric Loader + Fabric API, Java **25**) dedicado al páramo altoandino, la arquitectura colonial/de pueblo y el mobiliario típico colombiano.

## Contenido principal

### 1. Páramo y Frailejones (sin biomas nuevos)
- **Frailejón (`frailejon`)**: Planta de 2 bloques de altura que se genera naturalmente en biomas fríos y elevados vanilla (`meadow`, `windswept_hills`, `stony_peaks`, `jagged_peaks`, `snowy_slopes`, `grove` y montañas). Emite partículas tenues de bruma ambiental a su alrededor.
- **Agua pura y fibra**: Clic derecho con **Frasco de vidrio** sobre un frailejón húmedo extrae **Agua pura de páramo (`agua_pura`)** sin destruir la planta (se recarga con lluvia o paso del tiempo; también acepta polvo de hueso). Al cortarlo suelta **Fibra de frailejón (`frailejon_fiber`)**, convertible en **Fardo de fibra (`frailejon_fiber_block`)** aislante y amortiguador de caídas.

### 2. Arquitectura colonial y de pueblo
- **Tejas de barro**: `teja_barro`, `teja_barro_stairs`, `teja_barro_slab`.
- **Paredes tradicionales**: `bahareque`, `bahareque_encalado` y `zocalo_colonial`.
- **Calles y plazas**: `adoquin_colonial`, `adoquin_colonial_stairs`, `adoquin_colonial_slab` y `adoquin_colonial_wall`.
- **Fachada y luz**: Balcones coloniales (`balcon_azul`, `balcon_rojo`, `balcon_amarillo`), **Puerta tallada colonial (`puerta_tallada`)** y **Farol colonial (`farol_colonial`)**.

### 3. Mobiliario típico y música
- **Sillas Rimax (`silla_rimax_blanca`, `silla_rimax_roja`, `silla_rimax_azul`)**: Sentables con clic derecho e indestructibles frente a explosiones (configurable en `config/puebloyparamo.json`).
- **Hamaca tejida (`hamaca`)**: Permite mecerse de día (o agachado) y dormir hasta el amanecer de noche o en tormenta **sin modificar el punto de reaparición** del jugador.
- **Olla de barro (`olla_barro`)**: Estación funcional con interfaz propia (4 ingredientes + salida) que cocina al colocarse sobre fogata, fuego, magma o estufa encendida:
  - **Aguapanela caliente**, **Infusión de frailejón**, **Caldo de costilla**, **Ajiaco santafereño** y **Sancocho trifásico**.
  - Compatibilidad blanda opcional con `gastronomia` y `mitosyfauna`.
- **Picó costeño (`pico`) y discos musicales**: Reproduce a largo alcance (48 bloques por defecto) cuatro discos con audio Ogg Vorbis incluido (**Cumbia**, **Vallenato**, **Salsa** y **Rap local**) además de funcionar con discos vanilla y tocadiscos.

## Compilación y pruebas
```bash
./gradlew test
./gradlew build
```
