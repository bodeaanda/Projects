#version 410 core

layout(location=0) in vec3 vPosition;
layout(location=1) in vec3 vNormal;
layout(location=2) in vec2 vTexCoords;

out vec4 fPosEye;
out vec2 fTexCoords;

out vec4 clipSpace;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform float time;

void main() 
{
    vec4 worldPos = model * vec4(vPosition, 1.0f);
    
    // Wave animation
    float amplitude = 0.15;
    float frequency = 1.5;
    // Wave 1
    worldPos.y += sin(time * frequency + worldPos.z * 0.5) * amplitude;
    // Wave 2
    worldPos.y += sin(time * 2.0 + worldPos.x * 0.3) * (amplitude * 0.6);
    
    fPosEye = view * worldPos;
    clipSpace = projection * fPosEye;
    gl_Position = clipSpace;
    fTexCoords = vTexCoords;
}
