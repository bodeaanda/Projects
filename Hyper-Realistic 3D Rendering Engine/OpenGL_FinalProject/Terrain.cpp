#include "Terrain.hpp"
#include "stb_image.h"

namespace gps {

    Terrain::Terrain() : VAO(0), VBO(0), IBO(0), width(0), height(0), nrChannels(0) {}

    Terrain::~Terrain() {
        if (VAO != 0) glDeleteVertexArrays(1, &VAO);
        if (VBO != 0) glDeleteBuffers(1, &VBO);
        if (IBO != 0) glDeleteBuffers(1, &IBO);
    }

    void Terrain::LoadHeightMap(const char* path) {
        stbi_set_flip_vertically_on_load(true);
        unsigned char* data = stbi_load(path, &width, &height, &nrChannels, 0);
        
        if (data) {
            std::cout << "Loaded heightmap of size " << height << " x " << width << std::endl;
            generateMesh(data);
            stbi_image_free(data);
        } else {
            std::cout << "Failed to load heightmap: " << path << std::endl;
        }
    }

    glm::vec3 Terrain::calculateNormal(int x, int z, unsigned char* data) {
        // Handle boundary cases by clamping
        int xL = (x - 1 < 0) ? 0 : x - 1;
        int xR = (x + 1 >= width) ? width - 1 : x + 1;
        int zD = (z - 1 < 0) ? 0 : z - 1;
        int zU = (z + 1 >= height) ? height - 1 : z + 1;

        float hL = (float)data[(xL + z * width) * nrChannels];
        float hR = (float)data[(xR + z * width) * nrChannels];
        float hD = (float)data[(x + zD * width) * nrChannels];
        float hU = (float)data[(x + zU * width) * nrChannels];

        // Normal calculation using central differences
        // The y-scale factor (e.g., 0.1f) scaling height relative to x/z spacing affects the normal
        // Just assuming unit spacing in x,z and some scale in y
        // If we scale Y by 'yScale' later, we should account for it here, but let's approximate
        
        // Assuming vertices are generated with spacing of 1.0f in X and Z
        // And height is scaled by yScale.
        // Let's defer exact yScale usage or hardcode a reasonable value matching generation
        float yScale = 64.0f / 256.0f; // Matches generation

        glm::vec3 normal;
        normal.x = (hL - hR) * yScale;
        normal.y = 2.0f; // This is a simplification; depends on grid spacing (2.0f because xR-xL = 2 units)
        normal.z = (hD - hU) * yScale;
        
        return glm::normalize(normal);
    }

    void Terrain::generateMesh(unsigned char* data) {
        float yScale = 64.0f / 256.0f;
        float yShift = 16.0f;

        vertices.clear();
        // Reserve memory: width * height * 8 floats (3 pos, 3 norm, 2 tex)
        vertices.reserve(width * height * 8);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // Height from red channel (or first channel)
                unsigned char yChar = data[(j + width * i) * nrChannels];
                float y = (float)yChar;

                // Position
                float posX = -height / 2.0f + height * i / (float)height; // Assuming i maps to X? User snippet did this. 
                // Wait, typically i is row (Z) and j is col (X). User code:
                // vertices.push_back( -height/2.0f + height*i/(float)height );   // vx
                // vertices.push_back( (int) y * yScale - yShift);   // vy
                // vertices.push_back( -width/2.0f + width*j/(float)width );   // vz
                // i goes 0..height, used for X. j goes 0..width, used for Z.
                // That effectively rotates the map, but we'll stick to their logic or standard X/Z logic.
                // Let's standardise: x = j, z = i usually.
                // User code: vx = derived from i. vz = derived from j.
                // Let's follow user snippet direction to match their expectation of orientation.
                
                float vx = -height / 2.0f + (float)i; // Simply using grid units
                // Re-reading user snippet: height*i/(float)height is just 'i'.
                // So vx = -height/2 + i.
                
                float vy = y * yScale - yShift;
                float vz = -width / 2.0f + (float)j; 

                // Normal
                glm::vec3 normal = calculateNormal(j, i, data); // Note: calculateNormal expects (x, z) = (j, i) based on data indexing

                // TexCoords
                float u = (float)j / (width - 1);
                float v = (float)i / (height - 1);
                
                // Push Vertex Data
                // Pos
                vertices.push_back(vx);
                vertices.push_back(vy);
                vertices.push_back(vz);
                // Norm
                vertices.push_back(normal.x);
                vertices.push_back(normal.y);
                vertices.push_back(normal.z);
                // UV
                vertices.push_back(u);
                vertices.push_back(v);
            }
        }

        // Indices for Triangle Strips
        // We can just use triangles for simplicity with standard drawing, but User used strips.
        // Strips are faster but slightly annoying to setup with primitive restart or degenerate triangles for single draw call.
        // User snippet loop:
        /*
        for(unsigned i = 0; i < height-1; i += rez) {
            for(unsigned j = 0; j < width; j += rez) {
                for(unsigned k = 0; k < 2; k++) {
                    indices.push_back(j + width * (i + k*rez));
                }
            }
        }
        */
        // This generates indices for strips. 
        // We will stick to GL_TRIANGLES for compatibility with standard `Draw()` methods usually, 
        // OR implement a specific strip draw in `Draw`.
        // Let's use GL_TRIANGLES to be safe and compatible with generic Engine structure if possible,
        // BUT `Terrain::Draw` is custom, so we can do whatever.
        // Let's use GL_TRIANGLES for robust 'Mesh' like behavior.
        
        indices.clear();
        for (int i = 0; i < height - 1; i++) {
            for (int j = 0; j < width - 1; j++) {
                // Quad (i,j) to (i+1, j+1)
                // Top left: (i, j)
                // Top right: (i, j+1)
                // Bottom left: (i+1, j)
                // Bottom right: (i+1, j+1)
                
                // Vertices are stored row by row (i), then col (j)? 
                // Loop was i outer, j inner. 
                // Index = j + width * i
                
                int topLeft = j + width * i;
                int topRight = (j + 1) + width * i;
                int bottomLeft = j + width * (i + 1);
                int bottomRight = (j + 1) + width * (i + 1);

                // Triangle 1
                indices.push_back(topLeft);
                indices.push_back(bottomLeft);
                indices.push_back(topRight);
                
                // Triangle 2
                indices.push_back(topRight);
                indices.push_back(bottomLeft);
                indices.push_back(bottomRight);
            }
        }

        // Create Buffers
        glGenVertexArrays(1, &VAO);
        glGenBuffers(1, &VBO);
        glGenBuffers(1, &IBO);

        glBindVertexArray(VAO);

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertices.size() * sizeof(float), vertices.data(), GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.size() * sizeof(unsigned int), indices.data(), GL_STATIC_DRAW);

        // Link Attributes
        // Position
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 8 * sizeof(float), (void*)0);
        
        // Normal
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 8 * sizeof(float), (void*)(3 * sizeof(float)));
        
        // TexCoords
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, GL_FALSE, 8 * sizeof(float), (void*)(6 * sizeof(float)));

        glBindVertexArray(0);
    }

    void Terrain::Draw(gps::Shader& shader) {
        shader.useShaderProgram();
        glBindVertexArray(VAO);
        
        // Draw using Triangles
        glDrawElements(GL_TRIANGLES, (GLsizei)indices.size(), GL_UNSIGNED_INT, 0);
        
        glBindVertexArray(0);
    }
}
