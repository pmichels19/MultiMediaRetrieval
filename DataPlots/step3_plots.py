from pathlib import Path
import json
import matplotlib.pyplot as plt
import matplotlib.image as img


grouped_classes = {"Labeled_PSB": ["Mech", "Hand"], "ShapeDatabase_INFOMR-master": ["Jet", "Fish"]}


def plot_histogram(ax, histograms):
    ys = list(range(20))
    for i in range(20):
        ax.plot(ys, histograms[i], '-')


def plot_histograms(descriptors, labels, name, labelpad=10, height=8):
    path_data = {}
    for database in grouped_classes:
        for class_name in grouped_classes[database]:
            key = f"{database}\\{class_name}"

            path_data[class_name] = {}
            for descriptor in descriptors:
                path_data[class_name][descriptor] = []

            json_files = Path(f"..\\ModelQueryTool\\Shapes\\{key}").rglob("**/*_clean.json")
            for json_file in json_files:
                f = open(json_file)
                data = json.load(f)
                for descriptor in descriptors:
                    path_data[class_name][descriptor].append(data[descriptor])

    fig, ax = plt.subplots(len(descriptors) + 1, 4, sharey='row', figsize=(10, 8))
    for i in range(4):
        key = list(path_data.keys())[i]
        ax[0][i].set_title(key, fontweight='bold')

        for j in range(len(descriptors) + 1):
            ax[j][i].tick_params(axis='x', which='both', top=False, bottom=False, labelbottom=False)
            if j == 0:
                class_img = img.imread(f"..\\ModelQueryTool\\figures\\screenshot_{key}.png")
                ax[j][i].imshow(class_img)
                ax[j][i].axis('off')
            else:
                descriptor = descriptors[j - 1]
                hists = path_data[key][descriptor]
                plot_histogram(ax[j][i], hists)
                ax[j][0].set_ylabel(labels[j - 1], rotation='horizontal', verticalalignment='center_baseline', fontweight='bold', labelpad=labelpad)

    plt.savefig(f"figures\\{name}.png")


mandatory_descriptors = ["a3", "d1", "d2", "d3", "d4"]
mandatory_labels = ["a3", "d1", "d2", "d3", "d4"]
plot_histograms(mandatory_descriptors, mandatory_labels, "descriptors")

lightfield_descriptors = [
    "lightfield_0.0_0.0_0.0_x",
    "lightfield_0.2_0.0_0.0_x",
    "lightfield_0.4_0.0_0.0_x",
    "lightfield_0.6_0.0_0.0_x",
    "lightfield_0.8_0.0_0.0_x",
]

lightfield_labels = [
    "0.0",
    "0.2",
    "0.4",
    "0.6",
    "0.8",
]
plot_histograms(lightfield_descriptors, lightfield_labels, "lightfields", labelpad=30)
