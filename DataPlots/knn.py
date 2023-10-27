import scipy as sp
import numpy as np
import csv


def get_features(features_path, mesh_path):
    with open(features_path, newline='') as csvfile:
        csv_reader = csv.reader(csvfile, delimiter=';', quotechar='|')
        for i, row in enumerate(csv_reader):
            if i == 0:
                continue
            filepath = row[0]
            if filepath == mesh_path:
                label = row.pop(1)
                row.pop(0)
                features = [float(feature.replace(" ", "")) for feature in row if feature != " "]
                return features, label
        else:
            raise RuntimeError(f"Mesh path {mesh_path} not found in mini-database.")


def get_all_features(features_path, exclude=None):
    total_features = []
    labels = []
    paths = []
    with open(features_path, newline='') as csvfile:
        csv_reader = csv.reader(csvfile, delimiter=';', quotechar='|')
        
        for i, row in enumerate(csv_reader):
            if i == 0:
                continue # Skip header row
            filepath = row[0]
            if exclude and filepath == exclude:
                continue # Exclude shape from returned features list if specified
            label = row.pop(1)
            row.pop(0)
            features = [float(feature.replace(" ", "")) for feature in row if feature != " "]
            total_features.append(features)
            labels.append(label)
            paths.append(filepath)
    
    return np.array(total_features), labels, paths


def main():
    # Parameters
    query_path = "Humanoid/m129.obj"
    features_path = "miniDB/features.csv"
    k = 5

    # Load feature vector for query shape and db shapes
    query_features, query_label = get_features(features_path, query_path)
    features_total, labels, paths = get_all_features(features_path, exclude=query_path)

    # Build KDTree
    print("Building KDTree... ", end="")
    kdtree = sp.spatial.KDTree(features_total)
    print("Finished.")

    # Do query
    knn_distances, knn_indices = kdtree.query(query_features, k=k)
    print(
        f"{k} nearest neighbors for shape {query_path} (label='{query_label}'):\n",
        "\n".join([("    " + str(paths[i]) + f" (label='{labels[i]}', distance={dist})") for i, dist in zip(knn_indices, knn_distances)]),
        sep=""
    )


if __name__ == "__main__":
    main()
