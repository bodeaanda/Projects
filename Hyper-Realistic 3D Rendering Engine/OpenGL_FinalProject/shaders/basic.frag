#version 410 core

in vec3 fNormal;
in vec2 fTexCoords;
in vec4 fPosLightSpace;
in vec4 fPosEye;

out vec4 fColor;

uniform sampler2D diffuseTexture;
uniform sampler2D shadowMap;
uniform int isShadow;
uniform vec3 colorModifier;
uniform vec3 lightPos; 
uniform vec3 lightColor; 
uniform float lightIntensity; 
uniform vec3 lightDir; 

uniform vec3 spotLightPos;
uniform vec3 spotLightDir;
uniform vec3 spotLightColor;
uniform float spotLightCutoff;
uniform float spotLightOuterCutoff;

float computeShadow()
{
    // perform perspective divide
    vec3 projCoords = fPosLightSpace.xyz / fPosLightSpace.w;
    // transform to [0,1] range
    projCoords = projCoords * 0.5 + 0.5;
    
    // get closest depth value from light's perspective (using [0,1] range fragPosLight as coords)
    float closestDepth = texture(shadowMap, projCoords.xy).r; 
    
    // get depth of current fragment from light's perspective
    float currentDepth = projCoords.z;
    
    // check whether current frag pos is in shadow
    // bias to prevent shadow acne
    float bias = 0.001; 
    float shadow = currentDepth - bias > closestDepth ? 1.0 : 0.0;
    
    if(projCoords.z > 1.0)
        shadow = 0.0;
        
    return shadow;
}

float computeFog()
{
    float fogStart = 100.0f;
    float fogEnd = 1500.0f;
    float fragmentDistance = length(fPosEye.xyz);
    float fogFactor = (fogEnd - fragmentDistance) / (fogEnd - fogStart);
    return clamp(fogFactor, 0.0f, 1.0f);
}

uniform int hasTexture;
uniform vec3 materialDiffuse;

void main() 
{
    // Retrieve diffuse color
    vec4 colorFromTexture;
    if (hasTexture == 1) {
        colorFromTexture = texture(diffuseTexture, fTexCoords);
    } else {
        colorFromTexture = vec4(materialDiffuse, 1.0);
    }
    
    // Alpha discard - lowered threshold for testing
    if(colorFromTexture.a < 0.01)
        discard;

    // Apply color modifier
    vec3 baseColor = colorFromTexture.rgb * colorModifier;

    // Properties
    vec3 norm = normalize(fNormal);
    vec3 viewDir = normalize(-fPosEye.xyz);
    
    // Shadow Calculation
    // Shadow Calculation
    float shadow = computeShadow();

    // --- Directional Light (Dynamic from main.cpp) ---
    vec3 lightDirEye = normalize(lightDir); 
    vec3 dirLightColor = vec3(1.0, 1.0, 1.0);
    
    float dirAmbientStrength = 0.35; 
    vec3 dirAmbient = dirAmbientStrength * dirLightColor;
    
    float diff = max(dot(norm, lightDirEye), 0.0);
    vec3 dirDiffuse = diff * dirLightColor;
    
    // Specular highlight - Broadened and strengthened for "Sun Ray" feel
    float specularStrength = 0.8;
    vec3 reflectDir = reflect(-lightDirEye, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 16);
    vec3 specular = specularStrength * spec * dirLightColor;
    
    vec3 directionalLighting = (dirAmbient + (1.0 - shadow) * (dirDiffuse + specular));
    
    vec3 finalColor = directionalLighting * baseColor;
    
    // Point Light (Fire) 
    vec3 lightDirPoint = normalize(lightPos - fPosEye.xyz);
    float dist = length(lightPos - fPosEye.xyz);
    float attenuation = 1.0 / (1.0 + 0.1 * dist + 0.05 * (dist * dist));
    
    float diffPoint = max(dot(norm, lightDirPoint), 0.0);
    vec3 pointDiffuse = diffPoint * lightColor * lightIntensity * attenuation;
    
    finalColor += pointDiffuse * baseColor;
    
    // Spotlight (Window)
    vec3 lightDirSpot = normalize(spotLightPos - fPosEye.xyz);
    
    float theta = dot(lightDirSpot, normalize(-spotLightDir));
    float epsilon = spotLightCutoff - spotLightOuterCutoff;
    
    float spotIntensity = clamp((theta - spotLightOuterCutoff) / epsilon, 0.0, 1.0);

    float distSpot = length(spotLightPos - fPosEye.xyz);
    float attenuationSpot = 1.0 / (1.0 + 0.05 * distSpot + 0.02 * (distSpot * distSpot));

    // Diffuse lighting
    float diffSpot = max(dot(norm, lightDirSpot), 0.0);
    vec3 spotDiffuse = diffSpot * spotLightColor * spotIntensity * attenuationSpot;
    finalColor += spotDiffuse * baseColor;

    // Aura
    float haloDist = smoothstep(10.0, 0.0, distSpot);
    float haloCone = smoothstep(spotLightOuterCutoff, spotLightCutoff, theta);

    float haloIntensity = haloDist * haloCone;
    vec3 spotAura = spotLightColor * haloIntensity * attenuationSpot * 0.6;

    finalColor += spotAura;

    // Fog Calculation
    float fogFactor = computeFog();
    vec4 fogColor = vec4(0.7f, 0.7f, 0.7f, 1.0f); 

    fColor = mix(fogColor, vec4(finalColor, colorFromTexture.a), fogFactor);
}
