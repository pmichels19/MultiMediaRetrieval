from matplotlib import pyplot as plt
import matplotlib.image as img
import json
import numpy as np


def vis_rotation(x, y, z):
    chosen_file = "..\\ModelQueryTool\\Shapes\\ShapeDatabase_INFOMR-master\\Jet\\m1253_clean"
    fig, ax = plt.subplots(2, 2, figsize=(8, 8), sharex='col', sharey='row')
    fig.patch.set_facecolor('none')
    plt.tight_layout()
    plt.subplots_adjust(wspace=-0.01, hspace=-0.01)
    # fig.tight_layout(pad=0)
    ax[0][0].axis('off')

    f = open(f"{chosen_file}.json")
    data = json.load(f)
    coords = np.arange(20) * 50
    x_heights = data[f'lightfield_{x}_{y}_{z}_x']
    ax[0][1].bar(coords, x_heights, width=50, color='lime')

    y_heights = data[f'lightfield_{x}_{y}_{z}_y']
    ax[1][0].barh(coords, y_heights, height=50, color='lime')
    ax[1][0].invert_xaxis()
    ax[1][0].axis('off')

    class_img = img.imread(f"figures\\lightfield_{x}_{y}_{z}.png")
    ax[1][1].imshow(class_img)
    ax[1][1].axis('off')
    ax[0][1].axis('off')

    f.close()
    plt.savefig(f"figures\\lightfield_{x}_{y}_{z}_visualized.png")


vis_rotation(0.0, 0.0, 0.0)
vis_rotation(0.4, 0.0, 0.0)
vis_rotation(0.0, 0.4, 0.0)
vis_rotation(0.0, 0.0, 0.4)
