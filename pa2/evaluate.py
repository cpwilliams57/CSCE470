import os

import numpy as np
from scipy import sparse as sp
from math import log


LABEL_FILE = os.path.join("pa2","data", "train.txt")
PRED_FILE = os.path.join("pa2", "pred.txt")


def check_clusterings(labels_true, labels_pred):
    labels_true = np.asarray(labels_true)
    labels_pred = np.asarray(labels_pred)

    # input checks
    if labels_true.ndim != 1:
        raise ValueError(
            "labels_true must be 1D: shape is %r" % (labels_true.shape,))
    if labels_pred.ndim != 1:
        raise ValueError(
            "labels_pred must be 1D: shape is %r" % (labels_pred.shape,))
    if labels_true.shape != labels_pred.shape:
        raise ValueError(
            "labels_true and labels_pred must have same size, got %d and %d"
            % (labels_true.shape[0], labels_pred.shape[0]))
    return labels_true, labels_pred


def contingency_matrix(labels_true, labels_pred, eps=None):
    classes, class_idx = np.unique(labels_true, return_inverse=True)
    clusters, cluster_idx = np.unique(labels_pred, return_inverse=True)
    n_classes = classes.shape[0]
    n_clusters = clusters.shape[0]
    # Using coo_matrix to accelerate simple histogram calculation,
    # i.e. bins are consecutive integers
    # Currently, coo_matrix is faster than histogram2d for simple cases
    contingency = sp.coo_matrix((np.ones(class_idx.shape[0]),
                                 (class_idx, cluster_idx)),
                                shape=(n_classes, n_clusters),
                                dtype=np.int)

    contingency = contingency.tocsr()
    contingency.sum_duplicates()
    return contingency


def mutual_info_score(labels_true, labels_pred, contingency=None):
    nzx, nzy, nz_val = sp.find(contingency)

    contingency_sum = contingency.sum()
    pi = np.ravel(contingency.sum(axis=1))
    pj = np.ravel(contingency.sum(axis=0))
    log_contingency_nm = np.log(nz_val)
    contingency_nm = nz_val / contingency_sum
    # Don't need to calculate the full outer product, just for non-zeroes
    outer = pi.take(nzx).astype(np.int64) * pj.take(nzy).astype(np.int64)
    log_outer = -np.log(outer) + log(pi.sum()) + log(pj.sum())
    mi = (contingency_nm * (log_contingency_nm - log(contingency_sum)) +
          contingency_nm * log_outer)
    return mi.sum()


def entropy(labels):
    if len(labels) == 0:
        return 1.0
    label_idx = np.unique(labels, return_inverse=True)[1]
    pi = np.bincount(label_idx).astype(np.float64)
    pi = pi[pi > 0]
    pi_sum = np.sum(pi)
    # log(a / b) should be calculated as log(a) - log(b) for
    # possible loss of precision
    return -np.sum((pi / pi_sum) * (np.log(pi) - log(pi_sum)))


def normalized_mutual_info_score(labels_true, labels_pred):
    labels_true, labels_pred = check_clusterings(labels_true, labels_pred)
    classes = np.unique(labels_true)
    clusters = np.unique(labels_pred)
    # Special limit cases: no clustering since the data is not split.
    # This is a perfect match hence return 1.0.
    if (classes.shape[0] == clusters.shape[0] == 1 or
            classes.shape[0] == clusters.shape[0] == 0):
        return 1.0
    contingency = contingency_matrix(labels_true, labels_pred)
    contingency = contingency.astype(np.float64)
    # Calculate the MI for the two clusterings
    mi = mutual_info_score(labels_true, labels_pred,
                           contingency=contingency)
    # Calculate the expected value for the mutual information
    # Calculate entropy for each labeling
    h_true, h_pred = entropy(labels_true), entropy(labels_pred)
    normalizer = np.sqrt(h_true * h_pred)
    # Avoid 0.0 / 0.0 when either entropy is zero.
    normalizer = max(normalizer, np.finfo('float64').eps)
    nmi = mi / normalizer
    return nmi


if __name__ == "__main__":
    if not os.path.exists(PRED_FILE):
        print("Run k-means first.")
    else:
        with open(LABEL_FILE) as lf, open(PRED_FILE) as pf:
            labels = [v[1] for v in map(lambda line: line.split(), lf.read().split("\n")) if len(v) == 2]
            preds = [v[1] for v in map(lambda line: line.split(), pf.read().split("\n")) if len(v) == 2]
        
        assert len(labels) == len(preds)

        nmi = normalized_mutual_info_score(labels, preds)
        print("Normalized mutual information: {}".format(nmi))

