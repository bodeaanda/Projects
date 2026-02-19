#version 410 core

in vec2 fTexCoords;
out vec4 fColor;

uniform sampler2D glowTexture;
uniform float time;
uniform vec3 glowColor;

void main() 
{
    vec4 textureColor = texture(glowTexture, fTexCoords);
    
    // Subtle pulse effect
    float pulse = 0.8 + 0.2 * sin(time * 2.0);
    
    // Additive blending will be used, so we focus on the color and alpha
    fColor = vec4(textureColor.rgb * glowColor * pulse, textureColor.a * pulse);
    
    if (fColor.a < 0.01)
        discard;
}
