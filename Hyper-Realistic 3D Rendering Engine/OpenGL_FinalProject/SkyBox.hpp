#ifndef SkyBox_hpp
#define SkyBox_hpp

#include <vector>
#include <string>
#include <iostream>

#include <GL/glew.h>
#include "glm/glm.hpp"
#include "Shader.hpp"

namespace gps {
    class SkyBox {
    public:
        SkyBox();
        void Load(std::vector<std::string> cubeFaces);
        void Draw(gps::Shader& shader, glm::mat4 viewMatrix, glm::mat4 projectionMatrix, float time);
        
    private:
        GLuint skyboxVAO;
        GLuint skyboxVBO;
        GLuint cubemapTexture;
        
        GLuint loadCubemap(std::vector<std::string> faces);
        void initSkyBox();
    };
}

#endif /* SkyBox_hpp */
