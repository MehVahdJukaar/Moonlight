plugins {
    id("com.possible-triangle.neoforge")
}

neoforge {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

neoForge {
    accessTransformers {
    }
}

dependencies {
    modCompileOnly("curse.maven:irisshaders-455508:5789255")
    modCompileOnly ("curse.maven:map-atlases-forge-519759:7659933")
    modCompileOnly("curse.maven:modernfix-790626:4599353")
    modCompileOnly("curse.maven:quark-243121:7640331")

    modCompileOnly("curse.maven:autoreglib-250363:3857246")
    modCompileOnly("curse.maven:quark-oddities-301051:3575623")
    modCompileOnly("curse.maven:quark-243121:4463411")
    modCompileOnly("curse.maven:the-twilight-forest-227639:7398100")
//    modImplementation("curse.maven:open-loader-354339:6546293")
    //modImplementation("curse.maven:prickle-1023259:6961457")

    //modRuntimeOnly("curse.maven:productivetrees-867074:5290721")
    // modRuntimeOnly("com.tterrag.registrate:Registrate:MC1.19-1.1.5")
    //// modImplementation("com.jozufozu.flywheel:flywheel-forge-${flywheel_minecraft_version}:${flywheel_version}")

    // modRuntimeOnly("net.mehvahdjukaar:supplementaries-forge:1.19.2-2.2.3")
    // modRuntimeOnly("net.mehvahdjukaar:supplementaries-neoforge:1.21-3.5.18"){
    modCompileOnly ("curse.maven:map-atlases-forge-519759:4990003")
    //modImplementation ("curse.maven:supplementaries-412082:4995508")
    modImplementation ("curse.maven:configured-457570:7122915")
    modCompileOnly("curse.maven:yacl-667299:5424504")


}

