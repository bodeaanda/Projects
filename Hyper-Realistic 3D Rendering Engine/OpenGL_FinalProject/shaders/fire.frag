#version 410 core

in vec2 texCoord1;
in vec2 texCoord2;
in vec2 texCoord3;

out vec4 fColor;

uniform sampler2D fireTexture;
uniform sampler2D noiseTexture;
uniform sampler2D alphaTexture;

void main()
{
    // Sample noise texture at three different speeds/offsets
    vec4 noise1 = texture(noiseTexture, texCoord1);
    vec4 noise2 = texture(noiseTexture, texCoord2);
    vec4 noise3 = texture(noiseTexture, texCoord3);

    // Combine noise and scale to [-1, 1] range roughly
    vec2 perturbation = (noise1.xy + noise2.xy + noise3.xy) - 1.5;
    perturbation *= 0.08; // Strength of distortion

    // Taper distortion for a teardrop shape: 
    // More distortion in the middle-top, but very little at the base (y=1) and tip (y=0)
    // Note: in our setup y goes from 0 (top) to 1 (bottom).
    float taper = sin(texCoord1.y * 3.14159); 
    perturbation *= taper;

    // Use perturbed coordinates for fire color and alpha mask
    vec2 finalTexCoord = texCoord1 + perturbation;
    
    vec4 fireColor = texture(fireTexture, finalTexCoord);
    
    // Stretch the alpha mask slightly to favor a sharper top
    vec2 alphaCoord = vec2(finalTexCoord.x, finalTexCoord.y * 1.1 - 0.05);
    vec4 alphaMask = texture(alphaTexture, clamp(alphaCoord, 0.0, 1.0));

    // Combine and apply alpha
    fColor = vec4(fireColor.rgb, alphaMask.r * fireColor.a);
    
    // Discard completely transparent bits to help with blending issues
    if (fColor.a < 0.05)
        discard;
}
