#version 410 core

layout(location=0) in vec3 vPosition;
layout(location=1) in vec3 vNormal;
layout(location=2) in vec2 vTexCoords;

out vec3 fNormal;
out vec2 fTexCoords;
out vec4 fPosEye;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform int useSphericalUV;
uniform mat4 lightSpaceTrMatrix;
uniform float scaleUV;

out vec4 fPosLightSpace;

void main() 
{
    fPosEye = view * model * vec4(vPosition, 1.0f);
    gl_Position = projection * fPosEye;
    
	fNormal = normalize(mat3(transpose(inverse(view * model))) * vNormal); 
    
    fPosLightSpace = lightSpaceTrMatrix * model * vec4(vPosition, 1.0f);

    if (useSphericalUV == 1) {
        vec3 p = normalize(vPosition); 
      
        float u = 0.5 + atan(p.x, -p.z) / (2.0 * 3.14159265);
        float v = 0.5 - asin(p.y) / 3.14159265;
        fTexCoords = vec2(u, v);
    } else {
        float s = (scaleUV > 0.0) ? scaleUV : 1.0;
        fTexCoords = vTexCoords * s;
    }
}
