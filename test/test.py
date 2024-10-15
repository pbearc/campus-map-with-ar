import unittest
from route import Graph, get_forward_azimuth

class TestRouting(unittest.TestCase):
    def test_get_route(self):
        route_graph = Graph()
        #input data
        user_lat = 3.06472481008289
        user_long = 101.600554854729
        user_floor = 4
        poi_id = 1000771214
        route_graph.add_source_vertex(user_long, user_lat, user_floor)
        shortest_route, shortest_distance, _, _ = route_graph.get_route(poi_id)
        all_distances = self.get_all_path_distances(poi_id)
        self.assertIn(shortest_distance, all_distances)
        self.assertTrue(all(distance >= shortest_distance for distance in all_distances))
    
    def get_all_path_distances(self, destination_id):
        graph = Graph()
        #input data
        user_lat = 3.06472481008289
        user_long = 101.600554854729
        user_floor = 4
        poi_id = 1000771214
        graph.add_source_vertex(user_long, user_lat, user_floor)
        
        all_distances = []

        def dfs(vertex, current_distance):
            if vertex.rep == destination_id:
                all_distances.append(current_distance)
                return

            vertex.discovered = True
            for edge in vertex.adj:
                if not edge.v.discovered:
                    dfs(edge.v, current_distance + edge.w)
            vertex.discovered = False

        # Start DFS from the source vertex
        source_vertex = graph.vertices.get('S')
        dfs(source_vertex, 0)
        
        return all_distances
    
    def test_construct_base_graph(self):
        supported_poi = [
            1000771230,
            1000771261,
            1000771226,
            1000771214,
            1000771188,
            1000771180,
            1000771248,
            1000771241,
            1000771267,
            1000771223,
            1000771219,
            1000771247,
            1000771227,
            1000771208,
            1000771249,
            1000771264,
            1000771245,
            1000771215,
            1000771253,
            1000771222,
            1000771244,
            1000771269
        ]
        graph = Graph()
        visited_poi = set()
        for vertex in graph.vertices.values():
            if vertex.rep is not None:
                visited_poi.add(vertex.rep)
                self.assertIn(vertex.rep, supported_poi)
        self.assertEqual(len(visited_poi), len(supported_poi))
    
    def test_get_forward_azimuth(self):
        user_coordinate = (101.60048501752658, 3.0647247393682733)
        heading_coordinate = (101.600554854729, 3.06472481008289)
        degree_angle = get_forward_azimuth(user_coordinate, heading_coordinate)
        self.assertGreaterEqual(degree_angle, 0)
        self.assertLessEqual(degree_angle, 360)