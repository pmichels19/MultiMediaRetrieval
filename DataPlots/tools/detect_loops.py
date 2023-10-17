import math
import networkx as nx

graph_file = open("../Dataplots/shared/graph.txt", "r")
edge_nums = graph_file.readline().split(",")
graph_file.close()

edges = []
for i in range(math.floor(len(edge_nums) / 2)):
    idx = i * 2
    edges.append((int(edge_nums[idx + 0]), int(edge_nums[idx + 1])))

g = nx.Graph(edges)
cycles = list(nx.cycle_basis(g))
print(len(cycles))
for cycle in cycles:
    print(len(cycle))
    for v in cycle:
        print(v)
