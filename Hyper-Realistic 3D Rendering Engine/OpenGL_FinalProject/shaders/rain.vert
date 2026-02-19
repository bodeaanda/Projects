#version 410 core

layout (location = 0) in vec3 aPos;

uniform mat4 projection;
uniform mat4 view;
uniform float time;

void main()
{
    // box dimensions
    float width = 100.0;
    float height = 50.0;
    float depth = 100.0;
    
    // speed 
    float speed = 40.0;
   
    float y = aPos.y - (time * speed);
    float h = mod(y, height);
  
    vec3 newPos = vec3(aPos.x, h - 20.0, aPos.z);
    
    if (gl_VertexID % 2 == 1) {
        newPos.y += 0.5; // length of the rain drop
    }

    gl_Position = projection * view * vec4(newPos, 1.0);
}
