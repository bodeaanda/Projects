#version 410 core

in vec4 fPosEye;
in vec2 fTexCoords;
in vec4 clipSpace;

out vec4 fColor;

uniform sampler2D reflectionTexture;

void main() 
{
    vec2 ndc = (clipSpace.xy / clipSpace.w) / 2.0 + 0.5;
    
    vec4 reflectColor = texture(reflectionTexture, vec2(ndc.x, -ndc.y)); 
   
    vec2 reflectTexCoords = vec2(ndc.x, ndc.y);

    vec4 surfaceColor = mix(reflectColor, vec4(0.0, 0.2, 0.8, 1.0), 0.4);
    
    fColor = vec4(surfaceColor.rgb, 0.7); 
    // fColor = vec4(ndc.x, ndc.y, 0.0, 1.0); 
}
