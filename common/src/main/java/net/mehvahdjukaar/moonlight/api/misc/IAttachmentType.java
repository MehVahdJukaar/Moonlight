package net.mehvahdjukaar.moonlight.api.misc;

public interface IAttachmentType<A> {

    //Unchecked, Only works with attachment holders objects
    A getOrCreate(Object obj);

    A getOrNull(Object obj);
}
