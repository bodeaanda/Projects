#version 410 core

layout(location=0) in vec3 vPosition;
layout(location=1) in vec2 vTexCoords;

out vec2 texCoord1;
out vec2 texCoord2;
out vec2 texCoord3;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform float time;

void main()
{
    gl_Position = projection * view * model * vec4(vPosition, 1.0f);

    // Three sets of texture coordinates for noise sampling, scrolling at different speeds
    texCoord1 = vTexCoords;
    texCoord1.y -= time * 0.1;

    texCoord2 = vTexCoords;
    texCoord2.y -= time * 0.15;

    texCoord3 = vTexCoords;
    texCoord3.y -= time * 0.2;
}
