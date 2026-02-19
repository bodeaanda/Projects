#version 410 core

layout (location = 0) in vec3 aPos;

out vec3 pos;
out vec3 fsun;

uniform mat4 projection;
uniform mat4 view;
uniform float time;

void main()
{
    pos = aPos; 
    
    // sun position
    float timeScaled = time * 0.01 + 11.0; 
    fsun = vec3(0.0, sin(timeScaled), cos(timeScaled));

    vec4 p = projection * view * vec4(aPos, 1.0);
    gl_Position = p.xyww; 
}
