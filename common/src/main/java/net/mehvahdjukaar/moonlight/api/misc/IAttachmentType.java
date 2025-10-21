package net.mehvahdjukaar.moonlight.api.misc;

import org.jetbrains.annotations.Nullable;

public interface IAttachmentType<A, T> {

    //Unchecked, Only works with attachment holders objects
    A getOrCreate(T obj);

    A getOrNull(T obj);

    void set(T attachmentHolder, @Nullable A data);

    void sync(T attachmentHolder);
}
