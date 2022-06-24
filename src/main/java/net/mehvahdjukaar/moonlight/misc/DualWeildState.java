package net.mehvahdjukaar.moonlight.misc;

public class DualWeildState {

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
