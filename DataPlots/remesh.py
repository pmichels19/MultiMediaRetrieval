import pymeshlab as pml
from pathlib import Path

res_low = 5000
res_high = 15000


def refineMesh(file_path):
    mesh = pml.MeshSet()
    mesh.load_new_mesh(file_path)
    mesh.apply_filter('meshing_repair_non_manifold_edges')

    ot_count = mesh.current_mesh().face_number()
    if ot_count < res_low or ot_count > res_high:
        iters = 0
        t_count = ot_count
        print(f"Start: {t_count}")

        if t_count < res_low:
            target_edge_length = 0.4
            target_edge_length *= (t_count / res_low)
            max_iters = 40
        elif t_count > res_high:
            target_edge_length = 0.2
            target_edge_length *= (t_count / res_high)
            max_iters = 10
        while True:
            iters += 1
            mesh.meshing_isotropic_explicit_remeshing(targetlen=pml.AbsoluteValue(target_edge_length), iterations=10)
            mesh.apply_filter('meshing_repair_non_manifold_edges')

            t_count = mesh.current_mesh().face_number()
            if iters == max_iters:
                break

            if t_count < res_low:
                mesh.load_new_mesh(file_path)
                target_edge_length *= 0.9
            elif t_count > res_high:
                target_edge_length *= 1.1
                mesh.load_new_mesh(file_path)
            else:
                break

            # print(f"{t_count} -> targetlen {target_edge_length}")

        parts = file_path.rsplit(".", 1)
        file_name = parts[0]
        if not file_name.endswith("_clean"):
            file_name += "_clean"
        save_path = f"{file_name}.{parts[1]}"
        mesh.save_current_mesh(save_path)
        print(f"saved changes -> {t_count}")
    else:
        print(f"Found {ot_count} faces -> no changes")


# paths = Path("..\\ModelQueryTool\\Shapes").rglob("**/*_clean.obj")
# for path in paths:
#     refineMesh(str(path))

# import shutil
#
#
# paths = Path("..\\ModelQueryTool\\Shapes").rglob("**/*_clean.off")
# for path in paths:
#     src = str(path)
#     parts = src.rsplit(".", 1)
#     dst = parts[0] + "_backup." + parts[1]
#     shutil.copy(src, dst)
