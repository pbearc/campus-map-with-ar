from flask import Blueprint, request, redirect, url_for, jsonify
from main.db import get_db

bp = Blueprint('localization', __name__, url_prefix='/localization')

@bp.get('/poi/get')
def get_pois():
    floor_no = request.args.get('floor_no', type=int)
    pois = get_db().execute("SELECT * FROM poi WHERE z = ?", (floor_no,)).fetchall()
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