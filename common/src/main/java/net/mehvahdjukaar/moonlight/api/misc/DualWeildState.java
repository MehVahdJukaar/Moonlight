package net.mehvahdjukaar.moonlight.api.misc;

//TODO: rename
public class DualWeildState {

    private boolean twoHanded = false;

    public boolean isTwoHanded(){
        return twoHanded;
    }

    /**
     * @param twoHanded true if offhand animation should be skipped and handled by main hand method only instead
     */
    public void setTwoHanded(boolean twoHanded) {
        this.twoHanded = twoHanded;
    }
}
