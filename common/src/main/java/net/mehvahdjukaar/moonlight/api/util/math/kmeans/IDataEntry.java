package net.mehvahdjukaar.moonlight.api.util.math.kmeans;

import java.util.List;

public interface IDataEntry<T> {


    IDataEntry<T> average(List<IDataEntry<T>> others);

    void setClusterNo(int no);

    int getClusterNo();

    float distTo(IDataEntry<T> a);

    T cast();
}
