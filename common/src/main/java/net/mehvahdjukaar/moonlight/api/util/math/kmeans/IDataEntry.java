package net.mehvahdjukaar.moonlight.api.util.math.kmeans;

import java.util.List;

public interface IDataEntry<T> {


    IDataEntry<T> average(List<IDataEntry<T>> others);

    public void setClusterNo(int no);
    public int getClusterNo();

     float distTo(IDataEntry<T> a);

     T cast();
}
