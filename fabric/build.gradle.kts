plugins {
    id("com.possible-triangle.fabric")
}

fabric {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val modmenu_version: String by extra
val cca_version: String by extra
val codecui_version: String by extra
val sable_companion_version: String by extra
dependencies {

    modImplementation("net.mehvahdjukaar:codecui-fabric:${codecui_version}")
    include("net.mehvahdjukaar:codecui-fabric:${codecui_version}")

    modCompileOnly("curse.maven:irisshaders-455508:5789255")
    modCompileOnly ("curse.maven:map-atlases-forge-519759:7659933")
    modCompileOnly("curse.maven:modernfix-790626:4599353")
    modCompileOnly("curse.maven:quark-243121:7640331")
    modCompileOnly("dev.ryanhcode.sable-companion:sable-companion-fabric-1.21.1:${sable_companion_version}")

    modImplementation("com.terraformersmc:modmenu:${modmenu_version}")
    //modCompileOnly("curse.maven:super-better-grass-911433:4744836")
    modCompileOnly("curse.maven:map-atlases-436298:6345966")
    //modImplementation ("curse.maven:supplementaries-412082:4987505")
    // modRuntimeOnly("net.mehvahdjukaar:supplementaries-fabric:1.20-2.7.17")
    modCompileOnly ("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${cca_version}")
    modCompileOnly ("dev.onyxstudios.cardinal-components-api:cardinal-components-item:${cca_version}")
    modCompileOnly ("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${cca_version}")

    modRuntimeOnly("maven.modrinth:nekomas-fixed:0.5.2-26.1.2")

}
