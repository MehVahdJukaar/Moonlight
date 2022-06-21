package net.mehvahdjukaar.selene.resourcepack;

import net.mehvahdjukaar.selene.client.asset_generators.LangBuilder;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class AfterLanguageLoadEvent extends DynamicLanguageManager.LanguageAccessor {

    public AfterLanguageLoadEvent(Map<String, String> lines, List<LanguageInfo> info) {
        super(lines, info);
    }

}
