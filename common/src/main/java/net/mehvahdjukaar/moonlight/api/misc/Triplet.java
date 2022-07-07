package net.mehvahdjukaar.moonlight.api.misc;

public record Triplet<L, M, R>(L left, M middle, R right) {

    public static <A,B,C> Triplet<A,B,C> of(A left, B middle, C right){
        return new Triplet<>(left, middle, right);
    }
}
