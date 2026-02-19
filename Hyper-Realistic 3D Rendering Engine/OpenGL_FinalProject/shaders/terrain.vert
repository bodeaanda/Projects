#version 410 core

layout(location=0) in vec3 vPosition;
layout(location=1) in vec3 vNormal;
layout(location=2) in vec2 vTexCoords;

out vec3 Position;
out vec3 Normal;
out vec2 TexCoords;

void main() 
{
    Position = vPosition;
    Normal = vNormal;
    TexCoords = vTexCoords;
}
