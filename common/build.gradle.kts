plugins {
    id("com.possible-triangle.common")
}

common {
    accessWidener()
}

val sable_version_comp: String by extra
val sable_companion_version: String by extra
val codecui_version: String by extra

dependencies {

    compileOnly("net.mehvahdjukaar:codecui-common:${codecui_version}")

    modCompileOnly("curse.maven:modernfix-790626:4599353")
    // modCompileOnly("pebjebs.mapatlases:map_atlases-fabric:1.21-6.5.1");
    modCompileOnly("curse.maven:map-atlases-forge-519759:7659933")
    modCompileOnly("curse.maven:quark-243121:7640331")
    //modCompileOnly("curse.maven:yacl-667299:4523734")
    modCompileOnly("curse.maven:yacl-667299:6662859")

    modCompileOnly("curse.maven:irisshaders-455508:5789255")

    modCompileOnly("curse.maven:configured-457570:7122913") // v2.6.2

    modCompileOnly("dev.ryanhcode.sable-companion:sable-companion-common-1.21.1:${sable_companion_version}")

}
tasks.named("copyAccessTransformersPublications") {
    dependsOn(":common:transformAccessWidener")
}
