import pymeshlab as pml


mesh = pml.MeshSet()
mesh.load_new_mesh("../DataPlots/shared/temp.off")
mesh.apply_filter('meshing_re_orient_faces_coherentely')

faces = mesh.current_mesh().face_matrix()
for face in faces:
    print(str(face)[1:-1].strip())
