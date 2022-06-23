package net.mehvahdjukaar.moonlight.math.kmeans;

import java.util.LinkedList;
import java.util.List;

/**
 * Credit to https://stackabuse.com/guide-to-k-means-clustering-with-java/
 */
public class KMeans {

    static final Double PRECISION = 0.01;

    /* K-Means++ implementation, initializes K centroids from data */
    public static <A> LinkedList<IDataEntry<A>> kMeansPP(DataSet<A> data, int K) {
        LinkedList<IDataEntry<A>> centroids = new LinkedList<>();

        centroids.add(data.randomFromDataSet());

        for (int i = 1; i < K; i++) {
            centroids.add(data.calculateWeighedCentroid());
        }
        return centroids;
    }

    /* K-Means itself, it takes a dataset and a number K and adds class numbers
     * to records in the dataset */
    public static <A> void kMeans(DataSet<A> data, int K) {
        // select K initial centroids
        List<IDataEntry<A>> centroids = kMeansPP(data, K);

        // initAfterSetup Sum of Squared Errors to max, we'll lower it at each iteration
        Double SSE = Double.MAX_VALUE;

        while (true) {

            // assign observations to centroids

            List<IDataEntry<A>> points = data.getColorPoints();

            // for each record
            for (IDataEntry<A> point : points) {
                float minDist = Float.MAX_VALUE;
                // find the centroid at a minimum distance from it and add the record to its cluster
                for (int i = 0; i < centroids.size(); i++) {
                    float dist = centroids.get(i).distTo(point);
                    if (dist < minDist) {
                        minDist = dist;
                        point.setClusterNo(i);
                    }
                }

            }

            // recompute centroids according to new cluster assignments
            centroids = data.recomputeCentroids(K);

            // exit condition, SSE changed less than PRECISION parameter
            Double newSSE = data.calculateTotalSSE(centroids);
            if (SSE - newSSE <= PRECISION) {
                break;
            }
            SSE = newSSE;
        }
    }

}
