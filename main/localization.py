from flask import Blueprint, request, redirect, url_for, jsonify
from main.db import get_db

bp = Blueprint('localization', __name__, url_prefix='/localization')

@bp.get('/poi/get')
def get_pois():
    pois = get_db().execute("SELECT * FROM poi").fetchall()
    return jsonify([{"id": poi['id'], "name": poi['name'], "lat": poi['latitude'], "long": poi['longitude'], "z": poi['z'], "identifier": poi['identifier']} for poi in pois])

@bp.get('/poi/get/<int:poi_id>')
def get_poi(poi_id):
    poi = get_db().execute(f"SELECT * FROM poi WHERE id = {poi_id}").fetchone()
    return jsonify({"id": poi['id'], "name": poi['name'], "lat": poi['latitude'], "long": poi['longitude'], 'z': poi['z'], "identifier": poi['identifier']})

@bp.delete('/poi/delete/<int:poi_id>')
def delete_poi(poi_id):
    db = get_db()
    db.execute(f"DELETE FROM poi WHERE id = {poi_id}")
    db.commit()
    return redirect(url_for('localization.get_pois'))

@bp.post('/poi/post')
def post_poi():
    db = get_db()
    poi = request.json
    id, name, lat, long, z, identifier = poi['id'], poi['name'], poi['lat'], poi['long'], poi['z'], poi['identifier']
    db.execute(f"INSERT INTO poi (id, name, latitude, longitude, z, identifier) VALUES ({id}, '{name}', {lat}, {long}, {z}, '{identifier}')")
    db.commit()
    return redirect(url_for('localization.get_poi', poi_id=id))

@bp.get('/mac/get')
def get_macs():
    macs = get_db().execute("SELECT * FROM mac").fetchall()
    return jsonify([{"address": mac['address'], "latitude": mac['latitude'], "longitude": mac['longitude'], "z": mac['z']} for mac in macs])

@bp.post('/fp/post')
def add_fp():
    db = get_db()
    fp = request.json
    point, mac1_rssi, mac2_rssi, mac3_rssi = fp['point'], fp['mac1_rssi'], fp['mac2_rssi'], fp['mac3_rssi']
    db.execute(f"INSERT INTO rssi_fingerprint (mac1_rssi, mac2_rssi, mac3_rssi, point) VALUES ({mac1_rssi}, {mac2_rssi}, {mac3_rssi}, '{point}')")
    db.commit()
    return redirect(url_for('localization.get_fp', point=point))

@bp.get('fp/get/<point>')
def get_fp(point):
    fp = get_db().execute(f"SELECT * FROM rssi_fingerprint WHERE point = '{point}'").fetchone()
    return jsonify({"point": fp['point'], "mac1_rssi": fp['mac1_rssi'], "mac2_rssi": fp['mac2_rssi'], "mac3_rssi": fp['mac3_rssi']})
