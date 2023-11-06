import math

import pandas as pd
import numpy as np


def get_data(distance_function):
    data = pd.read_csv(f"..\\ModelQueryTool\\src\\Analysis\\CSV\\evaluation_metrics_{distance_function}.csv")
    data = data.drop('name', axis=1)
    return data


euclidean_data = get_data("euclidean")
cosine_data = get_data("cosine")
emd_data = get_data("emd")


def counts_to_ranks(occurrences):
    rank = 1
    for key in occurrences:
        count = occurrences[key]
        occurrences[key] = rank
        rank += count


def rank_dataframe(data):
    means = data.groupby('label').mean()
    for col in data.columns:
        if col == 'label':
            continue
        unique_values, counts = np.unique(means[[col]].values.flatten(), return_counts=True)
        if col == 'specificity' or col == 'sensitivity':
            unique_values = np.flip(unique_values)
            counts = np.flip(counts)
        ranks = dict(zip(unique_values, counts))
        counts_to_ranks(ranks)
        means[f"rank_{col}"] = means[col].map(ranks)
    means['mean_rank'] = means.mean(axis=1)
    return means.sort_values('mean_rank')


def find_best_worst(data, label, k, latex=False):
    ranked_data = rank_dataframe(data)
    ranked_data = ranked_data[[col for col in ranked_data if not col.startswith("rank")]]
    ranked_head = ranked_data.head(k).style.format(decimal='.', precision=3)
    ranked_tail = ranked_data.tail(k).style.format(decimal='.', precision=3)
    print(f"===== {label} =====")
    if latex:
        print(ranked_head.to_latex())
        print(ranked_tail.to_latex())
    else:
        print(ranked_head.to_string())
        print(ranked_tail.to_string())
    print()


# Latex output
find_best_worst(euclidean_data, "Euclidean", 5, True)
find_best_worst(cosine_data, "Cosine", 5, True)
find_best_worst(emd_data, "EMD", 5, True)
# Raw output
# find_best_worst(euclidean_data, "Euclidean", 5)
# find_best_worst(cosine_data, "Cosine", 5)
# find_best_worst(emd_data, "EMD", 5)


def sum_rows(totals, row):
    for key in totals:
        totals[key] += row[key] * row['class_size']


def sum_mean_squares(stdevs, averages, row):
    for key in stdevs:
        stdevs[key] += (row[key] - averages[key]) ** 2


def function_data(latex=False):
    dicts = []
    indices = []
    dfs = [euclidean_data, cosine_data, emd_data]
    labels = ["euclidean", "cosine", "EMD"]
    for i in range(len(dfs)):
        df = dfs[i]
        df['class_size'] = df.groupby('label')['label'].transform('size')
        df = df.groupby('label').mean()
        averages = {}
        for col in df.columns:
            if col == 'class_size':
                continue
            averages[col] = 0.0
        df.apply(lambda row: sum_rows(averages, row), axis=1)

        n = df['class_size'].sum()
        for col in df.columns:
            if col == 'class_size':
                continue
            averages[col] /= n

        stdevs = {}
        for col in df.columns:
            if col == 'class_size':
                continue
            stdevs[col] = 0.0
        df.apply(lambda row: sum_mean_squares(stdevs, averages, row), axis=1)
        for col in df.columns:
            if col == 'class_size':
                continue
            stdevs[col] = math.sqrt(stdevs[col] / (n - 1))
        indices.append(f"mean_{labels[i]}")
        dicts.append(averages)
        indices.append(f"stdev_{labels[i]}")
        dicts.append(stdevs)
    dict_data = pd.DataFrame(dicts, index=indices)
    if latex:
        dict_data = dict_data.style.format(decimal='.', precision=3)
        print(dict_data.to_latex())
    else:
        print(dict_data)


# Latex output
function_data(True)
# Raw output
# function_data()
