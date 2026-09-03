import org.jetbrains.kotlin.gradle.utils.extendsFrom

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

val codecui_version: String by extra
val sable_companion_version: String by extra

dependencies {
    implementation("net.mehvahdjukaar:codecui-neoforge:${codecui_version}")
    jarJar("net.mehvahdjukaar:codecui-neoforge:${codecui_version}")

    modCompileOnly("curse.maven:irisshaders-455508:5789255")
    modCompileOnly("curse.maven:map-atlases-forge-519759:7659933")
    modCompileOnly("curse.maven:modernfix-790626:4599353")
    modCompileOnly("curse.maven:quark-243121:8146177")
    modCompileOnly("curse.maven:zeta-968868:7980010")
    modCompileOnly("dev.ryanhcode.sable-companion:sable-companion-common-1.21.1:${sable_companion_version}")

    modCompileOnly("curse.maven:the-twilight-forest-227639:7398100")
//    modImplementation("curse.maven:open-loader-354339:6546293")
    //modImplementation("curse.maven:prickle-1023259:6961457")

    //modRuntimeOnly("curse.maven:productivetrees-867074:5290721")
    // modRuntimeOnly("com.tterrag.registrate:Registrate:MC1.19-1.1.5")
    //// modImplementation("com.jozufozu.flywheel:flywheel-forge-${flywheel_minecraft_version}:${flywheel_version}")
    // 1.21.1 jars, the Biolith they jarjar blocks the 26.1.2 run
//    modImplementation("curse.maven:quark-243121:7640331")
//    modImplementation("curse.maven:zeta-968868:7640154")

    // modRuntimeOnly("net.mehvahdjukaar:supplementaries-forge:1.19.2-2.2.3")
  // modRuntimeOnly("curse.maven:supplementaries-412082:8051628")
  // modRuntimeOnly("curse.maven:amendments-896746:8345243")
  // modRuntimeOnly("curse.maven:snowy-spirit-566142:8195621")
  // modRuntimeOnly("curse.maven:haunted-harvest-541753:8332283")
  // modRuntimeOnly("curse.maven:vista-1368607:8348336")

    modCompileOnly("curse.maven:map-atlases-forge-519759:4990003")
    //modImplementation ("curse.maven:supplementaries-412082:4995508")
    modCompileOnly("curse.maven:alexs-caves-924854:4806837")
}

