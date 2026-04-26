plugins {
    id("com.possible-triangle.fabric")
}

fabric {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val cloth_version: String by extra
val cca_version: String by extra
dependencies {

    modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:${cloth_version}")
    modCompileOnly("curse.maven:yacl-667299:5424169")
    modCompileOnly("curse.maven:modmenu-308702:5810603")
    //modCompileOnly("curse.maven:super-better-grass-911433:4744836")
    modCompileOnly("curse.maven:map-atlases-436298:6345966")
    //modImplementation ("curse.maven:supplementaries-412082:4987505")
    // modRuntimeOnly("net.mehvahdjukaar:supplementaries-fabric:1.20-2.7.17")
    modCompileOnly ("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${cca_version}")
    modCompileOnly ("dev.onyxstudios.cardinal-components-api:cardinal-components-item:${cca_version}")
    modCompileOnly ("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${cca_version}")

}
