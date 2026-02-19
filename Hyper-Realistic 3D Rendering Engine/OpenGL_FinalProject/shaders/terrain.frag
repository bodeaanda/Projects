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
uniform int hasTexture;
uniform vec3 materialDiffuse;
uniform vec3 lightPos; 
uniform vec3 lightColor; 
uniform float lightIntensity;
uniform vec3 lightDir; // EYE space

uniform vec3 spotLightPos;
uniform vec3 spotLightDir;
uniform vec3 spotLightColor;
uniform float spotLightCutoff;
uniform float spotLightOuterCutoff;

float computeShadow()
{
    vec3 projCoords = fPosLightSpace.xyz / fPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    
    if(projCoords.z > 1.0)
        return 0.0;
        
    float closestDepth = texture(shadowMap, projCoords.xy).r; 
    float currentDepth = projCoords.z;
    
    vec3 normal = normalize(fNormal);
    vec3 lightDirNormalized = normalize(lightDir);
    float bias = max(0.0005 * (1.0 - dot(normal, lightDirNormalized)), 0.00005);
    
    // PCF
    float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(shadowMap, 0);
    for(int x = -1; x <= 1; ++x)
    {
        for(int y = -1; y <= 1; ++y)
        {
            float pcfDepth = texture(shadowMap, projCoords.xy + vec2(x, y) * texelSize).r; 
            shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;        
        }    
    }
    shadow /= 9.0;
    
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

void main() 
{
    vec4 colorFromTexture;
    if (hasTexture == 1) {
        colorFromTexture = texture(diffuseTexture, fTexCoords);
    } else {
        colorFromTexture = vec4(materialDiffuse, 1.0);
    }

    if(colorFromTexture.a < 0.1)
        discard;

    vec3 modifiedColor = colorFromTexture.rgb * colorModifier;

    vec3 norm = normalize(fNormal);
    vec3 lightDirEye = normalize(lightDir);
    vec3 lightColor = vec3(1.0, 1.0, 1.0);

    float ambientStrength = 0.45; // Slightly higher for terrain
    vec3 ambient = ambientStrength * modifiedColor;
    
    float diff = max(dot(norm, lightDirEye), 0.0);
    vec3 diffuse = diff * modifiedColor;
    
    // Specular for terrain - consistent with basic.frag
    float specularStrength = 0.2;
    vec3 viewDir = normalize(-fPosEye.xyz);
    vec3 reflectDir = reflect(-lightDirEye, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 16);
    vec3 specular = specularStrength * spec * lightColor;

    float shadow = computeShadow();
    vec3 lighting = (ambient + (1.0 - shadow) * (diffuse + specular)) * lightColor;
    
    // --- Point Light (Fire) ---
    vec3 lightDirPoint = normalize(lightPos - fPosEye.xyz);
    float distPoint = length(lightPos - fPosEye.xyz);
    float attenuation = 1.0 / (1.0 + 0.1 * distPoint + 0.05 * (distPoint * distPoint));
    float diffPoint = max(dot(norm, lightDirPoint), 0.0);
    vec3 pointDiffuse = diffPoint * vec3(1.0, 0.5, 0.2) * lightIntensity * attenuation;
    
    lighting += pointDiffuse * modifiedColor;
    
    // --- Spotlight (Window) ---
    vec3 lightDirSpot = normalize(spotLightPos - fPosEye.xyz);
    float theta = dot(lightDirSpot, normalize(-spotLightDir)); 
    float epsilon = spotLightCutoff - spotLightOuterCutoff;
    float spotIntensity = clamp((theta - spotLightOuterCutoff) / epsilon, 0.0, 1.0);
    
    float distSpot = length(spotLightPos - fPosEye.xyz);
    float attenuationSpot = 1.0 / (1.0 + 0.05 * distSpot + 0.02 * (distSpot * distSpot));
    
    float diffSpot = max(dot(norm, lightDirSpot), 0.0);
    vec3 spotDiffuse = diffSpot * spotLightColor * spotIntensity * attenuationSpot;
    
    lighting += spotDiffuse * modifiedColor;
    
    vec4 finalColor = vec4(lighting, colorFromTexture.a);

    // Fog Calculation
    float fogFactor = computeFog();
    vec4 fogColor = vec4(0.7f, 0.7f, 0.7f, 1.0f);

    fColor = mix(fogColor, finalColor, fogFactor); 
}
