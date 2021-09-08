package net.mehvahdjukaar.selene.util;

public class TwoHandedAnimation {

    private boolean twoHanded = false;

    public boolean isTwoHanded(){
        return twoHanded;
    }

    /**
     * @param twoHanded true if off hand animation should be skipped and handled by main hand method only instead
     */
    public void setTwoHanded(boolean twoHanded) {
        this.twoHanded = twoHanded;
    }
}
