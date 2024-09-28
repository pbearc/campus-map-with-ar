DROP TABLE IF EXISTS poi;
DROP TABLE IF EXISTS mac;

CREATE TABLE poi(
    id INTEGER PRIMARY KEY,
    name TEXT,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    z INTEGER NOT NULL,
    identifier TEXT NOT NULL,
    UNIQUE ( latitude, longitude, z )
);

CREATE TABLE mac(
    address TEXT PRIMARY KEY,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    z INTEGER NOT NULL,
    UNIQUE ( latitude, longitude, z)
);

