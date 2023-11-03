import os.path
from pathlib import Path
from collections import Counter

import matplotlib
from sklearn.manifold import TSNE
from matplotlib import pyplot as plt

import pandas as pd
import numpy as np


matplotlib.use("TkAgg")
supported_file_types = ['.obj', '.off', '.ply']


def get_DB_data(database):
    json_paths = Path(f"..\\ModelQueryTool\\Shapes").rglob("**/*_clean.json")
    json_files = []
    for path in json_paths:
        json_files.append(str(path))

    db_paths = []
    db_labels = []
    start_idx = None
    end_idx = None
    for i in range(len(json_files)):
        split_path = json_files[i].split("\\")

        db = split_path[-3]
        if db == database and start_idx is None:
            start_idx = i
        elif db != database:
            if start_idx is not None and end_idx is None:
                end_idx = i
            continue
        db_labels.append(split_path[-2])

        obj_file = "not found..."
        for file_type in supported_file_types:
            if os.path.exists(json_files[i].replace(".json", file_type)):
                obj_file = split_path[-1].replace("_clean.json", file_type)
                break

        db_paths.append(obj_file)
    db_distances = pd.read_csv("shared\\distances.csv", header=None).to_numpy()[start_idx:end_idx, start_idx:end_idx]
    return db_paths, db_labels, db_distances, Counter(db_labels)


def get_color_map(label_list):
    color_palette = [
        '#a6cee3', '#1f78b4', '#b2df8a',
        '#33a02c', '#fb9a99', '#e31a1c',
        '#fdbf6f', '#ff7f00', '#cab2d6',
        '#6a3d9a', '#ffff99', '#b15928'
    ]

    # Create colors map
    colors_map = {}
    prev_color = 0
    for lbl in label_list:
        if lbl not in colors_map.keys():
            colors_map[lbl] = color_palette[prev_color]

            prev_color = (prev_color + 1) % len(color_palette)
    return colors_map


database = "Labeled_PSB"
# database = "ShapeDatabase_INFOMR-master"
files, labels, distances, occurrences = get_DB_data(database)
c_map = get_color_map(labels)

# Embed feature vectors in lower-dimensional space using T-distributed Stochastic Neighbor Embedding (t-SNE)
tsne_perplexity = round(np.fromiter(occurrences.values(), dtype=int).mean())
print(f"Perplexity: {tsne_perplexity}")
features_embedded = TSNE(n_iter=20000, perplexity=tsne_perplexity).fit_transform(np.array(distances))

colors = []
for label in labels:
    colors.append(c_map[label])
colors = np.array(colors)

# Create plot
fig, ax = plt.subplots(figsize=(15, 15), layout='constrained')
ax.set_title(f"t-SNE for {database} with lightfields")
plot = ax.scatter(features_embedded[:, 0], features_embedded[:, 1], c=colors)

original_edges = plot.get_edgecolors()

annotations = []
for label, file_name, x, y in zip(labels, files, features_embedded[:, 0], features_embedded[:, 1]):
    annotation = ax.annotate(f"{label} | {file_name}",
                             (x, y),
                             xytext=(20, 20),
                             textcoords="offset points",
                             bbox={"boxstyle": "round", "fc": "w"},
                             arrowprops=dict(arrowstyle="->"))
    annotation.set_visible(False)
    annotations.append(annotation)


def hover(event):
    if event.inaxes == ax:
        cont, ind = plot.contains(event)
        for i in range(len(annotations)):
            annotations[i].set_visible(i in ind['ind'])


def toggle_borders(event):
    if event.key == 'b':
        if np.array_equal(plot.get_edgecolors(), original_edges):
            plot.set_edgecolors('black')
        else:
            plot.set_edgecolors(original_edges)


fig.canvas.mpl_connect("motion_notify_event", hover)
fig.canvas.mpl_connect("key_press_event", toggle_borders)

plt.savefig(f"figures\\tsne_{database}_lightfields.png")

plt.ion()
plt.show(block=True)
plt.close()
