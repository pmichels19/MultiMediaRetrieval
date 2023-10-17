import math

import matplotlib.pyplot as plt
import pandas as pd
import numpy as np


def clean_name(base_name, csv_file):
    if "_clean" in csv_file:
        return f"{base_name}_clean"
    return base_name


def hist_vertices_faces(csv_file):
    data = pd.read_csv(csv_file)

    def plot_metric(metric):
        metric_name = metric.capitalize()
        fig, ax = plt.subplots(figsize=(6, 4))
        _, bins, _ = ax.hist(data[metric], bins=50, color='lime', rwidth=0.8, log=True)
        ax.set_xlabel(f'Number of {metric_name}')
        ax.set_ylabel('Number of Shapes')
        ax.set_title(f'Distribution of Shapes by Number of {metric_name}')
        ax.grid()
        ax.set_axisbelow(True)
        fig.autofmt_xdate(rotation=45)
        plt.xticks([round(x / 2000) * 2000 for x in bins[::5]])
        base_name = f"{metric}_distribution"
        plt.savefig(f'figures/{clean_name(base_name, csv_file)}.png')

    plot_metric('vertices')
    plot_metric('faces')


def class_distribution(csv_file):
    data = pd.read_csv(csv_file)
    counts = data['class'].value_counts(ascending=True).rename_axis('class').reset_index(name='counts')
    fig, axis = plt.subplots(figsize=(16, 8))
    fig.tight_layout()
    axis.bar(counts['class'], counts['counts'], color='lime')
    fig.autofmt_xdate(rotation=50)
    axis.set_ylabel('Number of Shapes')
    axis.set_title('Number of Shapes by Class')
    plt.grid()
    plt.savefig('figures/average_class_histogram.png')


def plot_AABB(csv_file):
    data = pd.read_csv(csv_file)
    data['dx'] = data['xmax'] - data['xmin']
    data['dy'] = data['ymax'] - data['ymin']
    data['dz'] = data['zmax'] - data['zmin']
    data['dxyz'] = data['dx'] * data['dx'] + data['dy'] * data['dy'] + data['dz'] * data['dz']
    data['dxyz'] = np.sqrt(data['dxyz'])

    colors = ['lime', 'orange']

    fig, ax = plt.subplots(figsize=(6, 4))
    differences = ax.violinplot([data['dx'], data['dy'], data['dz'], data['dxyz']], showextrema=True)
    ax.set_xticks(np.arange(4) + 1, labels=['X', 'Y', 'Z', 'AABB'])
    ax.set_title('Distribution of Differences between the Maximum and Minimum')
    ax.set_xlabel('Axis')
    ax.set_ylabel('Difference (max - min)')
    plt.grid(axis='y')

    differences['cbars'].set_color(colors[0])
    differences['cmins'].set_color(colors[0])
    differences['cmaxes'].set_color(colors[0])
    for b in differences['bodies']:
        b.set_color(colors[0])

    plt.savefig(f'figures/{clean_name("differences_distribution", csv_file)}.png')


def plot_areas(csv_file, bins):
    area_data = pd.read_csv(csv_file).to_numpy()
    densities, edges = np.histogram(area_data, bins=bins)
    densities = densities / densities.sum()
    labels = []
    for i in range(len(edges) - 1):
        if i == len(edges) - 2:
            labels.append(f"[{edges[i] * 1e05:.1f}, {edges[i + 1] * 1e05:.1f}]")
        else:
            labels.append(f"[{edges[i] * 1e05:.1f}, {edges[i + 1] * 1e05:.1f})")

    fig, ax = plt.subplots(figsize=(6, 5))
    ax.bar(labels, densities, color='lime')
    ax.set_xlabel('Face Area (* 1e-5)')
    ax.set_ylabel('% of Faces')
    ax.set_title('Distribution of Areas over All Faces')
    fig.autofmt_xdate(rotation=45)
    plt.grid(axis='y')
    plt.savefig(f'figures/{clean_name("area_distribution", csv_file)}.png')


def print_min_max_avg(csv_file):
    data = pd.read_csv(csv_file)
    print(clean_name("vertices", csv_file))
    print(data['vertices'].min())
    print(data['vertices'].max())
    print(data['vertices'].mean())
    print(clean_name("faces", csv_file))
    print(data['faces'].min())
    print(data['faces'].max())
    print(data['faces'].mean())
    print()


######################
#   Plotting calls
######################
# hist_vertices_faces("../ModelQueryTool/src/Preprocessing/Analysis/CSV/analysis.csv")
# plot_areas("../ModelQueryTool/src/Preprocessing/Analysis/CSV/area_analysis.csv", np.arange(11) * 5 * 1e-05)
# print_min_max_avg("../ModelQueryTool/src/Preprocessing/Analysis/CSV/analysis.csv")

# hist_vertices_faces("../ModelQueryTool/src/Preprocessing/Analysis/CSV/analysis_clean.csv")
# class_distribution("../ModelQueryTool/src/Preprocessing/Analysis/CSV/analysis_clean.csv")
# plot_AABB("../ModelQueryTool/src/Preprocessing/Analysis/CSV/analysis_clean.csv")
# plot_areas("../ModelQueryTool/src/Preprocessing/Analysis/CSV/area_analysis_clean.csv", np.arange(11) * 0.1 * 1e-05)
# print_min_max_avg("../ModelQueryTool/src/Preprocessing/Analysis/CSV/analysis_clean.csv")
