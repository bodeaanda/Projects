#version 410 core

layout(vertices = 3) out;

in vec3 Position[];
in vec3 Normal[];
in vec2 TexCoords[];

out vec3 PositionES[];
out vec3 NormalES[];
out vec2 TexCoordsES[];

uniform float tessLevelInner;
uniform float tessLevelOuter;

void main()
{
    // Pass through
    PositionES[gl_InvocationID] = Position[gl_InvocationID];
    NormalES[gl_InvocationID] = Normal[gl_InvocationID];
    TexCoordsES[gl_InvocationID] = TexCoords[gl_InvocationID];
    
    // Set tessellation levels
    if (gl_InvocationID == 0) {
        gl_TessLevelInner[0] = tessLevelInner;
        gl_TessLevelOuter[0] = tessLevelOuter;
        gl_TessLevelOuter[1] = tessLevelOuter;
        gl_TessLevelOuter[2] = tessLevelOuter;
    }
}
