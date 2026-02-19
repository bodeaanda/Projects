#ifndef Terrain_hpp
#define Terrain_hpp

#include <vector>
#include <iostream>
#include <string>

#include <GL/glew.h>
#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>

#include "Shader.hpp"

namespace gps {

    class Terrain {
    public:
        Terrain();
        ~Terrain();

        void LoadHeightMap(const char* path);
        void Draw(gps::Shader& shader);
        
        // Helper to get terrain dimensions/scale if needed
        float getWidth() const { return (float)width; }
        float getHeight() const { return (float)height; }

    private:
        int width, height;
        int nrChannels;
        
        GLuint VAO, VBO, IBO;
        std::vector<unsigned int> indices;
        std::vector<float> vertices; // Stored as [Px, Py, Pz, Nx, Ny, Nz, Tu, Tv]

        void generateMesh(unsigned char* data);
        glm::vec3 calculateNormal(int x, int z, unsigned char* data);
    };

}

#endif /* Terrain_hpp */
