DROP TABLE IF EXISTS poi;
DROP TABLE IF EXISTS rp;
DROP TABLE IF EXISTS ap;
DROP TABLE IF EXISTS fp;

CREATE TABLE poi(
    id INTEGER PRIMARY KEY,
    name TEXT,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    z INTEGER NOT NULL,
    identifier TEXT NOT NULL,
    UNIQUE ( latitude, longitude )
);

CREATE TABLE ap(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mac TEXT UNIQUE NOT NULL,
    title TEXT
);

CREATE TABLE rp(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    UNIQUE ( latitude, longitude )
);

CREATE TABLE fp(
    rp_id INTEGER,
    ap_id INTEGER,
    rss INTEGER NOT NULL,
    FOREIGN KEY ( rp_id ) REFERENCES rp ( id ),
    FOREIGN KEY ( ap_id ) REFERENCES ap ( id ),
    PRIMARY KEY ( rp_id, ap_id )
);