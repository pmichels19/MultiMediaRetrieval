import pymeshlab as pml


mesh = pml.MeshSet()
mesh.load_new_mesh("..\\DataPlots\\shared\\temp.off")
mesh.apply_filter('generate_convex_hull')

vertices = mesh.current_mesh().vertex_matrix()
print(len(vertices))
for vertex in vertices:
    print(str(vertex)[1:-1].strip())

faces = mesh.current_mesh().face_matrix()
print(len(faces))
for face in faces:
    print(str(face)[1:-1].strip())
