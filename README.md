# TrackGBFS

A [GBFS](https://www.gbfs.org/) v1 feed poller that continuously archives bike-share station data and exposes it through a REST API and a web dashboard.

Designed for citizens, researchers, journalists, and advocates who need historical bike-share data.

## Features

- Polls a GBFS v1 feed on a configurable interval
- Stores only changes (delta-only persistence) to a local SQLite database
- REST API for current and historical station status and information
- Web dashboard listing all stations with live status

## API

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/station/status` | Latest status for all stations |
| `GET` | `/station/status/{id}` | Latest status for a station |
| `GET` | `/station/status/{id}/history` | Full status history for a station |
| `GET` | `/station/information` | Information for all stations |
| `GET` | `/station/information/{id}` | Information for a station |
| `GET` | `/` | Web dashboard |

## Configuration

All settings can be overridden via environment variables (Spring relaxed binding: `TRACKGBFS_FEED_BASE_URL` → `trackgbfs.feed.base-url`).

| Variable | Default | Description |
|----------|---------|-------------|
| `TRACKGBFS_FEED_BASE_URL` | Québec PBSC feed | GBFS v1 feed base URL |
| `TRACKGBFS_FEED_LANGUAGE` | `en` | Feed language code |
| `TRACKGBFS_DB_URL` | `jdbc:sqlite:trackgbfs.db` | JDBC URL for the SQLite database |
| `TRACKGBFS_POLL_MIN_INTERVAL_STATION_STATUS` | `5s` | Minimum polling interval for station status |
| `TRACKGBFS_POLL_MIN_INTERVAL_STATION_INFORMATION` | `1h` | Minimum polling interval for station information |
| `TRACKGBFS_API_CACHE_TTL` | `2m` | API response cache TTL |

## Building

### Requirements

- [Nix](https://nixos.org/) with flakes enabled (recommended — fully hermetic)
- **or** GraalVM CE 25+ with `JAVA_HOME` set

### Native binary (Nix)

```sh
nix build
./result/bin/trackgbfs
```

### Native binary (Gradle)

```sh
./gradlew nativeCompile
./build/native/nativeCompile/trackgbfs
```

### JVM (development)

```sh
./gradlew bootRun
```

### Update Nix dependency lock

Run this after adding or removing Gradle dependencies:

```sh
$(nix build .#trackgbfs.mitmCache.updateScript --print-out-paths)
```
