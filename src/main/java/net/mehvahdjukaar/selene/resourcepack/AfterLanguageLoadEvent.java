package net.mehvahdjukaar.selene.resourcepack;

import net.minecraft.client.resources.language.LanguageInfo;

import java.util.List;
import java.util.Map;

public class AfterLanguageLoadEvent extends DynamicLanguageManager.LanguageAccessor {

    public AfterLanguageLoadEvent(Map<String, String> lines, List<LanguageInfo> info) {
        super(lines, info);
    }

}
