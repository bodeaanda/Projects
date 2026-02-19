#version 410 core

layout(triangles, equal_spacing, ccw) in;

in vec3 PositionES[];
in vec3 NormalES[];
in vec2 TexCoordsES[];

out vec3 fNormal;
out vec2 fTexCoords;
out vec4 fPosEye;
out vec4 fPosLightSpace;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform mat4 lightSpaceTrMatrix;
uniform sampler2D heightMap;
uniform float heightScale;
uniform float scaleUV;

void main()
{
    // Interpolate attributes
    vec3 pos = gl_TessCoord.x * PositionES[0] + 
               gl_TessCoord.y * PositionES[1] + 
               gl_TessCoord.z * PositionES[2];
    
    vec3 normal = gl_TessCoord.x * NormalES[0] + 
                  gl_TessCoord.y * NormalES[1] + 
                  gl_TessCoord.z * NormalES[2];
    
    vec2 texCoord = gl_TessCoord.x * TexCoordsES[0] + 
                    gl_TessCoord.y * TexCoordsES[1] + 
                    gl_TessCoord.z * TexCoordsES[2];
    
    // Apply UV scaling
    float s = (scaleUV > 0.0) ? scaleUV : 1.0;
    vec2 scaledTexCoord = texCoord * s;
    
    // Smooth height sampling function
    // Using a simple 3x3 box blur or Gaussian approximation to round peaks
    float texelSizeVar = 1.0 / textureSize(heightMap, 0).x; // Assume square texture
    // If textureSize fails or is huge, fallback to a small value, but calculate it if possible. 
    // Just use a fixed small step or uniform if textureSize isn't available in this version (410 core has it).
    
    float h00 = texture(heightMap, scaledTexCoord).r;
    float h10 = texture(heightMap, scaledTexCoord + vec2(texelSizeVar, 0.0)).r;
    float h_10 = texture(heightMap, scaledTexCoord + vec2(-texelSizeVar, 0.0)).r;
    float h01 = texture(heightMap, scaledTexCoord + vec2(0.0, texelSizeVar)).r;
    float h0_1 = texture(heightMap, scaledTexCoord + vec2(0.0, -texelSizeVar)).r;
    
    // 5-tap Gaussian-ish smoothing
    // Center weight 4, neighbors 1. Total 8.
    float height = (4.0 * h00 + h10 + h_10 + h01 + h0_1) / 8.0;

    // For even more smoothing, use a wider kernel or multiple passes (but here just wider kernel or simple box)
    // Let's try to mix it: 
    // float height = (h00 + h10 + h_10 + h01 + h0_1) / 5.0; // Box filter
    
    pos.y += height * heightScale;
    
    // Calculate new normal based on Smoothed height map
    // We need smoothed neighbors for the normal too!
    
    // Helper macro or just manual expanding for neighbors
    float dist = texelSizeVar;
    float hL = (4.0 * texture(heightMap, scaledTexCoord + vec2(-dist, 0.0)).r + 
                      texture(heightMap, scaledTexCoord + vec2(0.0, 0.0)).r + 
                      texture(heightMap, scaledTexCoord + vec2(-2.0*dist, 0.0)).r + 
                      texture(heightMap, scaledTexCoord + vec2(-dist, dist)).r + 
                      texture(heightMap, scaledTexCoord + vec2(-dist, -dist)).r) / 8.0;

    float hR = (4.0 * texture(heightMap, scaledTexCoord + vec2(dist, 0.0)).r + 
                      texture(heightMap, scaledTexCoord + vec2(2.0*dist, 0.0)).r + 
                      texture(heightMap, scaledTexCoord + vec2(0.0, 0.0)).r + 
                      texture(heightMap, scaledTexCoord + vec2(dist, dist)).r + 
                      texture(heightMap, scaledTexCoord + vec2(dist, -dist)).r) / 8.0;

    float hD = (4.0 * texture(heightMap, scaledTexCoord + vec2(0.0, -dist)).r + 
                      texture(heightMap, scaledTexCoord + vec2(dist, -dist)).r + 
                      texture(heightMap, scaledTexCoord + vec2(-dist, -dist)).r + 
                      texture(heightMap, scaledTexCoord + vec2(0.0, 0.0)).r + 
                      texture(heightMap, scaledTexCoord + vec2(0.0, -2.0*dist)).r) / 8.0;

    float hU = (4.0 * texture(heightMap, scaledTexCoord + vec2(0.0, dist)).r + 
                      texture(heightMap, scaledTexCoord + vec2(dist, dist)).r + 
                      texture(heightMap, scaledTexCoord + vec2(-dist, dist)).r + 
                      texture(heightMap, scaledTexCoord + vec2(0.0, 2.0*dist)).r + 
                      texture(heightMap, scaledTexCoord + vec2(0.0, 0.0)).r) / 8.0;
    
    // Calculate tangent and bitangent from height differences
    vec3 tangent = normalize(vec3(2.0 * dist, (hR - hL) * heightScale, 0.0));
    vec3 bitangent = normalize(vec3(0.0, (hU - hD) * heightScale, 2.0 * dist));
    normal = normalize(cross(tangent, bitangent));
    
    // Transform to world space
    vec4 worldPos = model * vec4(pos, 1.0);
    fPosEye = view * worldPos;
    gl_Position = projection * fPosEye;
    fPosLightSpace = lightSpaceTrMatrix * worldPos;
    
    // Transform normal to Eye space
    fNormal = normalize(mat3(transpose(inverse(view * model))) * normal);
    fTexCoords = scaledTexCoord;
}
