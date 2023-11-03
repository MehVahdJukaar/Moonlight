#version 150

uniform sampler2D Sampler0;

uniform vec2 Reach;

uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 currentPixel = texture(Sampler0, texCoord0);

    if (currentPixel.a == 1) {
        fragColor = ColorModulator;
        //fragColor = currentPixel * vertexColor; // Draw non-transparent pixels normally
    } else {
        // Offset for neighboring pixels in the texture
        vec2 offset = Reach;
        if(offset.x !=0) {
            //outline code that works but doesnt exactly do what we need cause no borders on edges
            // Check the 8 neighboring pixels
            float neighborPixels[8];
            neighborPixels[0] = texture(Sampler0, texCoord0 + vec2(-offset.x, -offset.y)).a;
            neighborPixels[1] = texture(Sampler0, texCoord0 + vec2(0, -offset.y)).a;
            neighborPixels[2] = texture(Sampler0, texCoord0 + vec2(offset.x, -offset.y)).a;
            neighborPixels[3] = texture(Sampler0, texCoord0 + vec2(-offset.x, 0)).a;
            neighborPixels[4] = texture(Sampler0, texCoord0 + vec2(offset.x, 0)).a;
            neighborPixels[5] = texture(Sampler0, texCoord0 + vec2(-offset.x, offset.y)).a;
            neighborPixels[6] = texture(Sampler0, texCoord0 + vec2(0, offset.y)).a;
            neighborPixels[7] = texture(Sampler0, texCoord0 + vec2(offset.x, offset.y)).a;

            // Check if any neighboring pixel is non-transparent
            bool hasNonTransparentNeighbor = false;
            for (int i = 0; i < 8; i++) {
                if (neighborPixels[i] != 0) {
                    hasNonTransparentNeighbor = true;
                    break;
                }
            }

            if (hasNonTransparentNeighbor) {
                fragColor = ColorModulator;
            } else {
                discard;
            }
        }else discard;

    }
}
