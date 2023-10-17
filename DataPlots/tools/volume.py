import pymeshlab as pml

mesh = pml.MeshSet()
mesh.load_new_mesh("../DataPlots/shared/temp.off")
print(mesh.get_geometric_measures()["mesh_volume"])
