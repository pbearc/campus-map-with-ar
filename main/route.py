import os, json, math, heapq, copy
from main.db import get_db
from flask import Blueprint, current_app, request, jsonify
ROE_m = 6371000
SOURCE_V = 'S'

bp = Blueprint('route', __name__, url_prefix='/route') 
        
class Graph:
    def __init__(self):
        self.vertices = {}
        self.construct_base_graph()
    
    def construct_base_graph(self):
        path = os.path.join(current_app.root_path, 'route')
        for file in os.listdir(path):
            with open(os.path.join(path, file), 'r') as f:
                route = json.load(f)
                #initialize the vertices of the graph
                for feature in route['features']:
                    id = feature['properties']['id']
                    rep = feature['properties']['rep']
                    lat, long = feature['geometry']['coordinates']
                    #adj_list = [Edge(id, adj_id, ) for adj_id in feature['properties']['adj']]
                    vertex = Vertex(id, rep, lat, long, int(os.path.splitext(file)[0]))
                    self.vertices[id] = vertex
        for file in os.listdir(path):
            with open(os.path.join(path, file), 'r') as f:
                route = json.load(f)
                #initialize the edges of the graph
                for feature in route['features']:
                    u = self.vertices.get(feature['properties']['id'])
                    for adj_id in feature['properties']['adj']:
                        v = self.vertices.get(adj_id)
                        w = self.compute_gcd(u, v)
                        u.add_adjacent_vertices(Edge(u, v, w))
                            
    def compute_gcd(self, v1, v2):
        #convert latitude and longitude of v1 and v2 to radian
        v1_lat = math.radians(v1.lat)
        v1_long = math.radians(v1.long)
        v2_lat = math.radians(v2.lat)
        v2_long = math.radians(v2.long)
        #compute difference between latitude and longitude of v1 and v2
        delta_lat = v2_lat - v1_lat
        delta_long = v2_long - v1_long
        #apply haversine formula
        a = math.sin(delta_lat / 2.0) ** 2 + math.cos(v1_lat) * math.cos(v2_lat) * math.sin(delta_long / 2.0) ** 2
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        d = ROE_m * c
        return d
    
    def add_source_vertex(self, latitude, longitude, floor):
        s_vertex = Vertex(SOURCE_V, None, latitude, longitude, floor)
        distance, nearest_neighbour = self.get_nearest_neighbour(s_vertex, floor)
        self.vertices[SOURCE_V] = s_vertex
        s_vertex.add_adjacent_vertices(Edge(s_vertex, nearest_neighbour, distance))
        for adj in nearest_neighbour.adj:
            s_vertex.add_adjacent_vertices(Edge(s_vertex, adj.v, self.compute_gcd(s_vertex, adj.v)))
        # if nearest_neighbour.rep is not None:
        #     for adj in nearest_neighbour.adj:
        #         s_vertex.add_adjacent_vertices(Edge(s_vertex, adj.v, self.compute_gcd(s_vertex, adj.v)))
    
    def get_nearest_neighbour(self, s_vertex, floor):
        nearest_neighbour = (math.inf, None)
        for vertex in self.vertices.values():
            distance = self.compute_gcd(vertex, s_vertex)
            if vertex.z == floor:
                nearest_neighbour = min(nearest_neighbour, (distance, vertex), key = lambda tuple:tuple[0])
        return nearest_neighbour
    
    def get_route(self, poi_id):
        src_vertex = self.vertices[SOURCE_V]
        discovered = [[src_vertex.distance, src_vertex]]
        src_vertex.discovered = True
        current = None
        while len(discovered) > 0:
            _, current = heapq.heappop(discovered)
            
            #found destination 
            if current.rep == poi_id:
                break
            
            for edge in current.adj:
                if edge.v.discovered is False:
                    edge.v.discovered = True
                    edge.v.distance = edge.w + edge.u.distance
                    edge.v.previous = edge.u
                    heapq.heappush(discovered, [edge.v.distance, edge.v])
                else:
                    for i in range(len(discovered)):
                        _, ex_edge = discovered[i]
                        if ex_edge.id == edge.v.id and edge.u.distance + edge.w < edge.v.distance:
                            edge.v.distance = edge.u.distance + edge.w
                            edge.v.previous = edge.u
                            discovered[i][0] = edge.v.distance
        
        path_list = []
        route = dict()
        distance = current.distance
        while True:
            if route.get(current.z) is None:
                route[current.z] = []
            route.get(current.z).append((current.lat, current.long))
            path_list.append(((current.long, current.lat), current.z))
            if current.id == SOURCE_V:
                break
            current = current.previous
        
        path_list.reverse()
        if path_list[0][1] != path_list[1][1]:
            direction = None
            floor = path_list[0][1] - path_list[1][1]
        else:
            direction = get_forward_azimuth(path_list[0][0], path_list[1][0])
            floor = None
        
        return route, distance, direction, floor

class Edge:
    def __init__(self, u, v, w):
        self.u = u
        self.v = v
        self.w = w
    
class Vertex:
    def __init__(self, id, rep_poi, latitude, longitude, z):
        self.id = id
        self.rep = rep_poi
        self.adj = []
        self.lat = latitude
        self.long = longitude
        self.z = z
        self.discovered = False
        self.distance = 0
        self.previous = None
    
    def add_adjacent_vertices(self, edge):
        self.adj.append(edge)
        
def get_forward_azimuth(user_coordinate, heading_coordinate):
    u_lat, u_long = get_radian(user_coordinate[0]), get_radian(user_coordinate[1])
    h_lat, h_long = get_radian(heading_coordinate[0]), get_radian(heading_coordinate[1])
    
    delta_long = h_long - u_long
    
    x = math.cos(u_lat) * math.sin(h_lat) - math.sin(u_lat) * math.cos(h_lat) * math.cos(delta_long)
    y = math.sin(delta_long) * math.cos(h_lat)
    
    azimuth = math.atan2(y, x)
    
    return (get_degree(azimuth) + 360) % 360

def get_radian(degree):
    return degree * math.pi / 180

def get_degree(radian):
    return radian * 180 / math.pi
    
@bp.get('/get/<int:poi_id>')
def get_route(poi_id):
    route_graph = copy.deepcopy(current_app.base_route)
    user_lat = request.args.get('latitude', type=float)
    user_long = request.args.get('longitude', type=float)
    user_floor = request.args.get('floor', type=int)
    route_graph.add_source_vertex(user_lat, user_long, user_floor)
    route, distance, direction, floor = route_graph.get_route(poi_id)
    # for vertices in route_graph.vertices:
    #     print(f"{vertices}:{route_graph.vertices[vertices].rep}")
    #     for edge in route_graph.vertices[vertices].adj:
    #         print(edge.v.id)
    return jsonify({"route": route, "distance": distance, "direction": direction, "floor": floor})