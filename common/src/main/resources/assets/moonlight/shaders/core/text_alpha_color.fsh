#version 330

// Pairs with vanilla's core/rendertype_text vertex shader: only the shape of the texture is used,
// every opaque texel is painted with the vertex color

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    if (texture(Sampler0, texCoord0).a < 1.0) {
        discard;
    }
    fragColor = vertexColor;
}
