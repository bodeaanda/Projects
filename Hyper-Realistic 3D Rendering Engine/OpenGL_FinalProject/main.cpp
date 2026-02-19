#define GLEW_STATIC
#include <GL/glew.h>
#include <GLFW/glfw3.h>

#include <iostream>
#include <cmath>
#include "glm/glm.hpp" 
#include "glm/gtc/matrix_transform.hpp" 
#include "glm/gtc/type_ptr.hpp" 

#include "Camera.hpp"
#include "Shader.hpp"

#include "Model3D.hpp"
#include "SkyBox.hpp"

#include "stb_image.h"

// Window dimensions
const GLuint WIDTH = 1200, HEIGHT = 800;

GLFWwindow* glWindow = NULL;

// Camera
gps::Camera mySocketCamera(
    glm::vec3(0.0f, 0.0f, 3.0f),
    glm::vec3(0.0f, 0.0f, -10.0f),
    glm::vec3(0.0f, 1.0f, 0.0f)
);
GLfloat cameraSpeed = 0.05f;

bool pressedKeys[1024];

// Mouse
bool firstMouse = true;
float lastX = WIDTH / 2.0f;
float lastY = HEIGHT / 2.0f;
float yaw = -90.0f;
float pitch = 0.0f;

// Shaders and Objects
gps::Shader myBasicShader;

gps::Model3D myCottage;
gps::Model3D myEye;
gps::Model3D myTerrain;
gps::Model3D myBoat;
gps::Model3D myWater;
gps::SkyBox mySkyBox;
gps::Model3D myAirplane;
gps::Model3D myWomanDog;
gps::Model3D myBird;
gps::Model3D myMercury;

// Bird 
glm::vec3 birdPosition(0.0f, 15.0f, 0.0f); 
float birdRotation = 0.0f;
float birdSpeed = 0.08f;
float birdTurnSpeed = 1.0f;

float angle = 0.0f;
float airplaneX = 0.0f;

// View Mode
bool showWireframe = false;
bool showRain = false;

// Camera Animation
bool startAnimation = false;
float animationAngle = 0.0f;
float animationRadius = 25.0f; // Radius of the circular path
float animationSpeed = 20.0f; // Degrees per second

// Shadow Mapping
GLuint shadowMapFBO;
GLuint depthMapTexture;
const unsigned int SHADOW_WIDTH = 2048, SHADOW_HEIGHT = 2048;

// Terrain Height Map
GLuint heightMapTexture;

// Reflection
GLuint reflectionFBO;
GLuint reflectionTexture;
const unsigned int REFLECTION_WIDTH = 800, REFLECTION_HEIGHT = 600; 

gps::Shader depthMapShader;
gps::Shader waterShader;
gps::Shader skyboxShader;
gps::Shader rainShader;
gps::Shader terrainShader;
gps::Shader fireShader;
gps::Shader glowShader;

// Fire textures
GLuint fireColorTexture;
GLuint fireNoiseTexture;
GLuint fireAlphaTexture;

// Glow texture
GLuint windowGlowTexture;

// Fire and Glow quads
GLuint fireVAO, fireVBO;
GLuint glowVAO, glowVBO;

// Rain
const unsigned int RAIN_COUNT = 5000;
GLuint rainVAO, rainVBO;
std::vector<glm::vec3> rainParticles;

void windowResizeCallback(GLFWwindow* window, int width, int height) {
    fprintf(stdout, "window resized to width: %d , and height: %d\n", width, height);
    //TODO
}

void keyboardCallback(GLFWwindow* window, int key, int scancode, int action, int mode) {
    if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS)
        glfwSetWindowShouldClose(window, GL_TRUE);

    if (key == GLFW_KEY_M && action == GLFW_PRESS)
        showWireframe = !showWireframe;

    if (key == GLFW_KEY_R && action == GLFW_PRESS)
        showRain = !showRain;

    if (key == GLFW_KEY_V && action == GLFW_PRESS) {
        startAnimation = !startAnimation;
    }

    if (key >= 0 && key < 1024)
    {
        if (action == GLFW_PRESS)
            pressedKeys[key] = true;
        else if (action == GLFW_RELEASE)
            pressedKeys[key] = false;
    }
}

void mouseCallback(GLFWwindow* window, double xpos, double ypos) {
    if (firstMouse)
    {
        lastX = xpos;
        lastY = ypos;
        firstMouse = false;
    }

    float xoffset = xpos - lastX;
    float yoffset = lastY - ypos;
    lastX = xpos;
    lastY = ypos;

    float sensitivity = 0.1f;
    xoffset *= sensitivity;
    yoffset *= sensitivity;

    yaw += xoffset;
    pitch += yoffset;

    if (pitch > 89.0f)
        pitch = 89.0f;
    if (pitch < -89.0f)
        pitch = -89.0f;

    mySocketCamera.rotate(pitch, yaw);
}

bool checkCollision(glm::vec3 pos) {
    // AABB Collision
    
    // Bounds
    float xMin = -6.0f;
    float xMax = 6.0f;
    float zMin = -21.0f; 
    float zMax = -9.0f;  
    float yMin = -15.0f;
    float yMax = 5.0f;  
    
    if (pos.x >= xMin && pos.x <= xMax &&
        pos.y >= yMin && pos.y <= yMax &&
        pos.z >= zMin && pos.z <= zMax) {
        return true;
    }
    return false;
}


void updateCameraAnimation(double deltaTime) {
    if (!startAnimation) return;

    animationAngle += animationSpeed * (float)deltaTime;
    if (animationAngle > 360.0f) animationAngle -= 360.0f;

    float camX = sin(glm::radians(animationAngle)) * animationRadius;
    float camZ = cos(glm::radians(animationAngle)) * animationRadius;
    float camY = 5.0f; 

    glm::vec3 newPos(camX, camY, camZ);
    mySocketCamera.setPosition(newPos);

    glm::vec3 target(0.0f, 0.0f, 0.0f);
    glm::vec3 direction = glm::normalize(target - newPos);

    pitch = glm::degrees(asin(direction.y));
    yaw = glm::degrees(atan2(direction.z, direction.x));

    mySocketCamera.rotate(pitch, yaw);
}

void processMovement() {
    glm::vec3 oldPos = mySocketCamera.getPosition();

    if (pressedKeys[GLFW_KEY_W]) {
        mySocketCamera.move(gps::MOVE_FORWARD, cameraSpeed);
        if (checkCollision(mySocketCamera.getPosition())) mySocketCamera.setPosition(oldPos);
    }

    if (pressedKeys[GLFW_KEY_S]) {
        mySocketCamera.move(gps::MOVE_BACKWARD, cameraSpeed);
        if (checkCollision(mySocketCamera.getPosition())) mySocketCamera.setPosition(oldPos);
    }

    if (pressedKeys[GLFW_KEY_A]) {
        mySocketCamera.move(gps::MOVE_LEFT, cameraSpeed);
        if (checkCollision(mySocketCamera.getPosition())) mySocketCamera.setPosition(oldPos);
    }

    if (pressedKeys[GLFW_KEY_D]) {
        mySocketCamera.move(gps::MOVE_RIGHT, cameraSpeed);
         if (checkCollision(mySocketCamera.getPosition())) mySocketCamera.setPosition(oldPos);
    }

    // Bird Controls
    if (pressedKeys[GLFW_KEY_K]) {
        birdPosition.x += sin(glm::radians(birdRotation)) * birdSpeed;
        birdPosition.z += cos(glm::radians(birdRotation)) * birdSpeed;
    }
    if (pressedKeys[GLFW_KEY_I]) {
        birdPosition.x -= sin(glm::radians(birdRotation)) * birdSpeed;
        birdPosition.z -= cos(glm::radians(birdRotation)) * birdSpeed;
    }
    if (pressedKeys[GLFW_KEY_J]) {
        birdRotation += birdTurnSpeed;
    }
    if (pressedKeys[GLFW_KEY_L]) {
        birdRotation -= birdTurnSpeed;
    }
    
    // Up/Down controls for flying
    if (pressedKeys[GLFW_KEY_U]) {
        birdPosition.y += birdSpeed;
    }
    if (pressedKeys[GLFW_KEY_O]) {
        birdPosition.y -= birdSpeed;
    }
    

}

bool initOpenGLWindow()
{
    if (!glfwInit()) {
        fprintf(stderr, "ERROR: could not start GLFW3\n");
        return false;
    }

    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

    glWindow = glfwCreateWindow(WIDTH, HEIGHT, "OpenGL Project", NULL, NULL);
    if (!glWindow) {
        fprintf(stderr, "ERROR: could not open window with GLFW3\n");
        glfwTerminate();
        return false;
    }

    glfwMakeContextCurrent(glWindow);

    glfwSetWindowSizeCallback(glWindow, windowResizeCallback);
    glfwSetKeyCallback(glWindow, keyboardCallback);
    glfwSetCursorPosCallback(glWindow, mouseCallback);
    glfwSetInputMode(glWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

    glewExperimental = GL_TRUE;
    glewInit();

    const GLubyte* renderer = glGetString(GL_RENDERER); 
    const GLubyte* version = glGetString(GL_VERSION); 
    printf("Renderer: %s\n", renderer);
    printf("OpenGL version supported %s\n", version);
    
    glEnable(GL_DEPTH_TEST); 
    glDepthFunc(GL_LESS); 

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    return true;
}

void initFBO() {
    //generate FBO ID
    glGenFramebuffers(1, &shadowMapFBO);

    //create depth texture for FBO
    glGenTextures(1, &depthMapTexture);
    glBindTexture(GL_TEXTURE_2D, depthMapTexture);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT,
        SHADOW_WIDTH, SHADOW_HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, NULL);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    float borderColor[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

    //attach texture to FBO
    glBindFramebuffer(GL_FRAMEBUFFER, shadowMapFBO);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMapTexture, 0);

    glDrawBuffer(GL_NONE);
    glReadBuffer(GL_NONE);

    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void initReflectionFBO() {
    
    glGenFramebuffers(1, &reflectionFBO);
    glBindFramebuffer(GL_FRAMEBUFFER, reflectionFBO);

    // Create reflection texture
    glGenTextures(1, &reflectionTexture);
    glBindTexture(GL_TEXTURE_2D, reflectionTexture);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, REFLECTION_WIDTH, REFLECTION_HEIGHT, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, reflectionTexture, 0);

    // Create Depth Buffer 
    GLuint rboVector;
    glGenRenderbuffers(1, &rboVector);
    glBindRenderbuffer(GL_RENDERBUFFER, rboVector);
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, REFLECTION_WIDTH, REFLECTION_HEIGHT);
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboVector);

    if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
        std::cout << "ERROR::FRAMEBUFFER:: Reflection Framebuffer is not complete!" << std::endl;

    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void initRain() {
    rainShader.loadShader("shaders/rain.vert", "shaders/rain.frag");

    for (unsigned int i = 0; i < RAIN_COUNT; i++) {
        float x = (rand() % 2000 / 10.0f) - 100.0f; 
        float y = (rand() % 500 / 10.0f);          
        float z = (rand() % 2000 / 10.0f) - 100.0f; 
        glm::vec3 pos(x, y, z);
        rainParticles.push_back(pos); 
        rainParticles.push_back(pos); 
    }

    glGenVertexArrays(1, &rainVAO);
    glGenBuffers(1, &rainVBO);
    glBindVertexArray(rainVAO);
    glBindBuffer(GL_ARRAY_BUFFER, rainVBO);
    glBufferData(GL_ARRAY_BUFFER, rainParticles.size() * sizeof(glm::vec3), &rainParticles[0], GL_STATIC_DRAW);
    
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, sizeof(glm::vec3), (void*)0);
    glBindVertexArray(0);
}

void initGlowQuad() {
    float glowQuadVertices[] = {
        -1.0f,  1.0f, 0.0f,  0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f,  0.0f, 0.0f,
         1.0f,  1.0f, 0.0f,  1.0f, 1.0f,
         1.0f, -1.0f, 0.0f,  1.0f, 0.0f,
    };

    glGenVertexArrays(1, &glowVAO);
    glGenBuffers(1, &glowVBO);
    glBindVertexArray(glowVAO);
    glBindBuffer(GL_ARRAY_BUFFER, glowVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(glowQuadVertices), &glowQuadVertices, GL_STATIC_DRAW);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 5 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 5 * sizeof(float), (void*)(3 * sizeof(float)));
    glBindVertexArray(0);
}

void initFireQuad() {
    float fireQuadVertices[] = {
        -1.0f,  1.0f, 0.0f,  0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f,  0.0f, 0.0f,
         1.0f,  1.0f, 0.0f,  1.0f, 1.0f,
         1.0f, -1.0f, 0.0f,  1.0f, 0.0f,
    };

    glGenVertexArrays(1, &fireVAO);
    glGenBuffers(1, &fireVBO);
    glBindVertexArray(fireVAO);
    glBindBuffer(GL_ARRAY_BUFFER, fireVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(fireQuadVertices), &fireQuadVertices, GL_STATIC_DRAW);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 5 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 5 * sizeof(float), (void*)(3 * sizeof(float)));
    glBindVertexArray(0);
}

GLuint loadTexture(const char* path) {
    int width, height, nChannels;
    stbi_set_flip_vertically_on_load(true);

    unsigned char* data = stbi_load(path, &width, &height, &nChannels, 4);
    GLuint textureID;
    glGenTextures(1, &textureID);
    glBindTexture(GL_TEXTURE_2D, textureID);
    if (data) {
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        glGenerateMipmap(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        stbi_image_free(data);
    } else {
        std::cout << "DEBUG: Failed to load fire texture at: " << path << std::endl;
        
        unsigned char white[] = {255, 255, 255, 255};
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, white);
    }
    return textureID;
}

GLuint loadHeightMap(const char* path) {
    int width, height, nChannels;
    stbi_set_flip_vertically_on_load(true);
    unsigned char* data = stbi_load(path, &width, &height, &nChannels, 0);
    
    GLuint textureID;
    if (data) {
        glGenTextures(1, &textureID);
        glBindTexture(GL_TEXTURE_2D, textureID);
        
        GLenum format = GL_RED;
        if (nChannels == 1) format = GL_RED;
        else if (nChannels == 3) format = GL_RGB;
        else if (nChannels == 4) format = GL_RGBA;
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, width, height, 0, format, GL_UNSIGNED_BYTE, data);
        glGenerateMipmap(GL_TEXTURE_2D);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        
        stbi_image_free(data);
    } else {
        std::cout << "Failed to load height map at: " << path << std::endl;
    
        glGenTextures(1, &textureID);
        glBindTexture(GL_TEXTURE_2D, textureID);
        unsigned char defaultData[4] = {128, 128, 128, 255}; 
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, defaultData);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }
    
    return textureID;
}

void initResources() {
    myBasicShader.loadShader("shaders/basic.vert", "shaders/basic.frag");
    depthMapShader.loadShader("shaders/depthMap.vert", "shaders/depthMap.frag");
    waterShader.loadShader("shaders/water.vert", "shaders/water.frag");
    skyboxShader.loadShader("shaders/skybox.vert", "shaders/skybox.frag");
    
    terrainShader.loadShader("shaders/terrain.vert", "shaders/terrain.tesc", 
                            "shaders/terrain.tese", "shaders/terrain.frag");

    /*
    fireShader.loadShader("shaders/fire.vert", "shaders/fire.frag");
    fireColorTexture = loadTexture("models/Fire/textures/fire_color.png");
    fireNoiseTexture = loadTexture("models/Fire/textures/fire_noise.png");
    fireAlphaTexture = loadTexture("models/Fire/textures/fire_alpha.png");
    initFireQuad();
    */

    glowShader.loadShader("shaders/glow.vert", "shaders/glow.frag");
    windowGlowTexture = loadTexture("models/House/textures/window_glow.png");
    initGlowQuad();

    myCottage.LoadModel("models/House/45-cottage_free_other/Cottage_FREE.obj");
    myEye.LoadModel("models/Eye/eyeball_split.obj");

    myTerrain.LoadModel("models/terrain/terrain.obj");

    myAirplane.LoadModel("models/Airplane/11805_airplane_v2_L2.obj");
    myBoat.LoadModel("models/Boat/Boat.obj");
    myWomanDog.LoadModel("models/Woman with dog/Woman_Dog.obj");
    myBird.LoadModel("models/Bird/uploads_files_3632334_achara_base2_obj.obj");
    myMercury.LoadModel("models/Mercury/Mercury 1K.obj");

    myWater.LoadModel("models/Ground/ground.obj");
   
    heightMapTexture = loadHeightMap("models/Terrain/textures/terrain_Normal.png");
 
    std::vector<std::string> faces;
    faces.push_back("c:/Users/Anda/source/repos/OpenGL_FinalProject/OpenGL_FinalProject/skybox/right.tga");
    faces.push_back("c:/Users/Anda/source/repos/OpenGL_FinalProject/OpenGL_FinalProject/skybox/left.tga");
    faces.push_back("c:/Users/Anda/source/repos/OpenGL_FinalProject/OpenGL_FinalProject/skybox/top.tga");
    faces.push_back("c:/Users/Anda/source/repos/OpenGL_FinalProject/OpenGL_FinalProject/skybox/bottom.tga");
    faces.push_back("c:/Users/Anda/source/repos/OpenGL_FinalProject/OpenGL_FinalProject/skybox/back.tga");
    faces.push_back("c:/Users/Anda/source/repos/OpenGL_FinalProject/OpenGL_FinalProject/skybox/front.tga");
    mySkyBox.Load(faces);
}

glm::mat4 computeLightSpaceTrMatrix() {
    glm::mat4 lightView = glm::lookAt(glm::vec3(-150.0f, 150.0f, 0.0f), glm::vec3(0.0f, 0.0f, 0.0f), glm::vec3(0.0f, 1.0f, 0.0f));
    const GLfloat near_plane = 1.0f, far_plane = 500.0f;
    glm::mat4 lightProjection = glm::ortho(-300.0f, 300.0f, -300.0f, 300.0f, near_plane, far_plane);
    return lightProjection * lightView;
}

void drawObjects(gps::Shader shader, bool depthPass) {
    shader.useShaderProgram();

    glm::mat4 model;
    
    // Draw Cottage
    glUniform1f(glGetUniformLocation(shader.shaderProgram, "scaleUV"), 1.0f); 
    model = glm::translate(glm::mat4(1.0f), glm::vec3(0.0f, -13.8f, -15.0f)); 
    model = glm::rotate(model, glm::radians(-130.0f), glm::vec3(0.0f, 1.0f, 0.0f));
    glUniformMatrix4fv(glGetUniformLocation(shader.shaderProgram, "model"), 1, GL_FALSE, glm::value_ptr(model));
    myCottage.Draw(shader);

    // Draw Airplane
    model = glm::translate(glm::mat4(1.0f), glm::vec3(airplaneX, 20.0f, 0.0f));
    model = glm::rotate(model, glm::radians(-90.0f), glm::vec3(1.0f, 0.0f, 0.0f)); 
    model = glm::rotate(model, glm::radians(-90.0f), glm::vec3(0.0f, 0.0f, 1.0f)); 
    model = glm::scale(model, glm::vec3(0.01f)); 
    glUniformMatrix4fv(glGetUniformLocation(shader.shaderProgram, "model"), 1, GL_FALSE, glm::value_ptr(model));
    myAirplane.Draw(shader);

    // Draw Bird 
    model = glm::mat4(1.0f);
    model = glm::translate(model, birdPosition);
    model = glm::rotate(model, glm::radians(birdRotation), glm::vec3(0.0f, 1.0f, 0.0f));
    model = glm::rotate(model, glm::radians(360.0f), glm::vec3(1.0f, 0.0f, 0.0f)); 
    model = glm::scale(model, glm::vec3(0.003f)); 
    glUniformMatrix4fv(glGetUniformLocation(shader.shaderProgram, "model"), 1, GL_FALSE, glm::value_ptr(model));
    myBird.Draw(shader);
    
    float currentTime = glfwGetTime();
    float boatY = -10.5f + sin(currentTime * 1.5f) * 0.15f; 
    float boatRock = sin(currentTime * 2.0f) * 2.0f; 
    
    model = glm::translate(glm::mat4(1.0f), glm::vec3(30.0f, boatY, 15.0f)); 
    model = glm::scale(model, glm::vec3(0.055f)); 
    model = glm::rotate(model, glm::radians(-90.0f), glm::vec3(0.0f, 1.0f, 0.0f)); 
    model = glm::rotate(model, glm::radians(boatRock), glm::vec3(1.0f, 0.0f, 0.0f)); 
    glUniformMatrix4fv(glGetUniformLocation(shader.shaderProgram, "model"), 1, GL_FALSE, glm::value_ptr(model));
    myBoat.Draw(shader);

    // Draw Woman with Dog 
    model = glm::translate(glm::mat4(1.0f), glm::vec3(5.5f, -14.4f, -8.0f)); 
    model = glm::scale(model, glm::vec3(0.01f)); 
    model = glm::rotate(model, glm::radians(-90.0f), glm::vec3(1.0f, 0.0f, 0.0f)); 
    glUniformMatrix4fv(glGetUniformLocation(shader.shaderProgram, "model"), 1, GL_FALSE, glm::value_ptr(model));
    myWomanDog.Draw(shader);
}

void renderScene() {
    // Reflection Pass
    glm::mat4 projection = glm::perspective(glm::radians(45.0f), (float)WIDTH / (float)HEIGHT, 0.1f, 1000.0f);

    float waterHeight = -14.5f;
    float distance = mySocketCamera.getPosition().y - waterHeight;
    glm::vec3 reflectedPos = mySocketCamera.getPosition();
    reflectedPos.y = waterHeight - distance; 
    
    float reflectedPitch = -pitch;
    glm::vec3 reflectedFront;
    reflectedFront.x = cos(glm::radians(yaw)) * cos(glm::radians(reflectedPitch));
    reflectedFront.y = sin(glm::radians(reflectedPitch));
    reflectedFront.z = sin(glm::radians(yaw)) * cos(glm::radians(reflectedPitch));
    reflectedFront = glm::normalize(reflectedFront);

    glm::mat4 reflectedView = glm::lookAt(reflectedPos, reflectedPos + reflectedFront, glm::vec3(0.0f, 1.0f, 0.0f));
    
    glBindFramebuffer(GL_FRAMEBUFFER, reflectionFBO);
    glViewport(0, 0, REFLECTION_WIDTH, REFLECTION_HEIGHT);
    glClearColor(0.53f, 0.81f, 0.92f, 1.0f); 
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    
    myBasicShader.useShaderProgram();
    glUniformMatrix4fv(glGetUniformLocation(myBasicShader.shaderProgram, "view"), 1, GL_FALSE, glm::value_ptr(reflectedView));
    glUniformMatrix4fv(glGetUniformLocation(myBasicShader.shaderProgram, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
    glUniformMatrix4fv(glGetUniformLocation(myBasicShader.shaderProgram, "lightSpaceTrMatrix"), 1, GL_FALSE, glm::value_ptr(computeLightSpaceTrMatrix()));
    
    glUniform1f(glGetUniformLocation(myBasicShader.shaderProgram, "lightIntensity"), 0.0f);

    glActiveTexture(GL_TEXTURE0);
    glUniform1i(glGetUniformLocation(myBasicShader.shaderProgram, "diffuseTexture"), 0);
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, depthMapTexture);
    glUniform1i(glGetUniformLocation(myBasicShader.shaderProgram, "shadowMap"), 1);
    glUniform1i(glGetUniformLocation(myBasicShader.shaderProgram, "isShadow"), 0);

    float time = (float)glfwGetTime() * 0.1f; 
    mySkyBox.Draw(skyboxShader, reflectedView, projection, time);
    glUniform3f(glGetUniformLocation(myBasicShader.shaderProgram, "colorModifier"), 1.0f, 1.0f, 1.0f);
    
    // Draw Reflected Objects 
    drawObjects(myBasicShader, false);

    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    // Shadow Map Pass
    depthMapShader.useShaderProgram();
    glUniformMatrix4fv(glGetUniformLocation(depthMapShader.shaderProgram, "lightSpaceTrMatrix"), 1, GL_FALSE, glm::value_ptr(computeLightSpaceTrMatrix()));
        
    glViewport(0, 0, SHADOW_WIDTH, SHADOW_HEIGHT);
    glBindFramebuffer(GL_FRAMEBUFFER, shadowMapFBO);
    glClear(GL_DEPTH_BUFFER_BIT);
    
    airplaneX += 0.01f;
    if (airplaneX > 300.0f) airplaneX = -300.0f;

    drawObjects(depthMapShader, true);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    // Render Scene Pass
    glViewport(0, 0, WIDTH, HEIGHT);
    glClearColor(0.53f, 0.81f, 0.92f, 1.0f); 
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    
    if (showWireframe) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    } else {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    myBasicShader.useShaderProgram();

    glm::mat4 view = mySocketCamera.getViewMatrix();
    glUniformMatrix4fv(glGetUniformLocation(myBasicShader.shaderProgram, "view"), 1, GL_FALSE, glm::value_ptr(view));
    glUniformMatrix4fv(glGetUniformLocation(myBasicShader.shaderProgram, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
    glUniformMatrix4fv(glGetUniformLocation(myBasicShader.shaderProgram, "lightSpaceTrMatrix"), 1, GL_FALSE, glm::value_ptr(computeLightSpaceTrMatrix()));

    glm::vec3 lightPosWorld = glm::vec3(-150.0f, 150.0f, 0.0f);
    glm::vec3 lightDirEye = glm::mat3(view) * glm::normalize(lightPosWorld);
    glUniform3fv(glGetUniformLocation(myBasicShader.shaderProgram, "lightDir"), 1, glm::value_ptr(lightDirEye));

    // Fire Light flickering logic
    /*
    float flicker = 1.0f + 0.3f * sin((float)glfwGetTime() * 10.0f);
  
    glm::vec3 firePosWorld = glm::vec3(10.0f, -10.5f, 10.0f);
    glm::vec3 firePosEye = glm::vec3(view * glm::vec4(firePosWorld, 1.0f));
    
    glUniform3fv(glGetUniformLocation(myBasicShader.shaderProgram, "lightPos"), 1, glm::value_ptr(firePosEye));
    glUniform3f(glGetUniformLocation(myBasicShader.shaderProgram, "lightColor"), 1.0f, 0.5f, 0.2f);
    glUniform1f(glGetUniformLocation(myBasicShader.shaderProgram, "lightIntensity"), flicker);
    */
    glUniform1f(glGetUniformLocation(myBasicShader.shaderProgram, "lightIntensity"), 0.0f);

    glm::vec3 spotLightPosWorld = glm::vec3(-1.0f, -10.3f, -14.0f);
    glm::vec3 spotLightDirWorld = glm::vec3(-1.0f, -0.5f, 1.0f); 
    glm::vec3 windowCenterWorld = glm::vec3(-2.8f, -11.0f, -12.5f);
    glm::vec3 windowNormalWorld = glm::vec3(0.0f, 0.0f, 1.0f);
    glm::vec3 spotLightPosEye = glm::vec3(view * glm::vec4(spotLightPosWorld, 1.0f));
    glm::vec3 spotLightDirEye = glm::mat3(view) * spotLightDirWorld;

    glUniform3fv(glGetUniformLocation(myBasicShader.shaderProgram, "spotLightPos"), 1, glm::value_ptr(spotLightPosEye));
    glUniform3fv(glGetUniformLocation(myBasicShader.shaderProgram, "spotLightDir"), 1, glm::value_ptr(spotLightDirEye));
    glUniform3f(glGetUniformLocation(myBasicShader.shaderProgram, "spotLightColor"), 1.0f, 0.9f, 0.4f); 
    glUniform1f(glGetUniformLocation(myBasicShader.shaderProgram, "spotLightCutoff"), glm::cos(glm::radians(12.5f)));
    glUniform1f(glGetUniformLocation(myBasicShader.shaderProgram, "spotLightOuterCutoff"), glm::cos(glm::radians(17.5f)));

    glActiveTexture(GL_TEXTURE0);
    glUniform1i(glGetUniformLocation(myBasicShader.shaderProgram, "diffuseTexture"), 0);
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, depthMapTexture);
    glUniform1i(glGetUniformLocation(myBasicShader.shaderProgram, "shadowMap"), 1);

    glUniform1i(glGetUniformLocation(myBasicShader.shaderProgram, "isShadow"), 0);
    glUniform3f(glGetUniformLocation(myBasicShader.shaderProgram, "colorModifier"), 1.0f, 1.0f, 1.0f);

    drawObjects(myBasicShader, false);

    // Draw Terrain with Tessellation
    terrainShader.useShaderProgram();
    glUniformMatrix4fv(glGetUniformLocation(terrainShader.shaderProgram, "view"), 1, GL_FALSE, glm::value_ptr(view));
    glUniformMatrix4fv(glGetUniformLocation(terrainShader.shaderProgram, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
    glUniformMatrix4fv(glGetUniformLocation(terrainShader.shaderProgram, "lightSpaceTrMatrix"), 1, GL_FALSE, glm::value_ptr(computeLightSpaceTrMatrix()));
    
    // Pass lightDir to terrain shader
    glUniform3fv(glGetUniformLocation(terrainShader.shaderProgram, "lightDir"), 1, glm::value_ptr(lightDirEye));

    glUniform1f(glGetUniformLocation(terrainShader.shaderProgram, "lightIntensity"), 0.0f);
    
    // Pass spotlight to terrain
    glUniform3fv(glGetUniformLocation(terrainShader.shaderProgram, "spotLightPos"), 1, glm::value_ptr(spotLightPosEye));
    glUniform3fv(glGetUniformLocation(terrainShader.shaderProgram, "spotLightDir"), 1, glm::value_ptr(spotLightDirEye));
    glUniform3f(glGetUniformLocation(terrainShader.shaderProgram, "spotLightColor"), 1.0f, 0.9f, 0.4f);
    glUniform1f(glGetUniformLocation(terrainShader.shaderProgram, "spotLightCutoff"), glm::cos(glm::radians(12.5f)));
    glUniform1f(glGetUniformLocation(terrainShader.shaderProgram, "spotLightOuterCutoff"), glm::cos(glm::radians(17.5f)));

    glUniform1f(glGetUniformLocation(terrainShader.shaderProgram, "tessLevelInner"), 32.0f);
    glUniform1f(glGetUniformLocation(terrainShader.shaderProgram, "tessLevelOuter"), 32.0f);
    glUniform1f(glGetUniformLocation(terrainShader.shaderProgram, "heightScale"), 2.0f);
    glUniform1f(glGetUniformLocation(terrainShader.shaderProgram, "scaleUV"), 30.0f);
    
    glActiveTexture(GL_TEXTURE0);
    glUniform1i(glGetUniformLocation(terrainShader.shaderProgram, "diffuseTexture"), 0);
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, depthMapTexture);
    glUniform1i(glGetUniformLocation(terrainShader.shaderProgram, "shadowMap"), 1);
    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, heightMapTexture);
    glUniform1i(glGetUniformLocation(terrainShader.shaderProgram, "heightMap"), 2);
    
    glUniform3f(glGetUniformLocation(terrainShader.shaderProgram, "colorModifier"), 1.0f, 1.0f, 1.0f);
    glPatchParameteri(GL_PATCH_VERTICES, 3);
    glm::mat4 terrainModel = glm::scale(glm::mat4(1.0f), glm::vec3(0.01f));
    glUniformMatrix4fv(glGetUniformLocation(terrainShader.shaderProgram, "model"), 1, GL_FALSE, glm::value_ptr(terrainModel));
    myTerrain.DrawPatches(terrainShader);
    
    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, 0);

    // Draw Water
    waterShader.useShaderProgram();
    glUniformMatrix4fv(glGetUniformLocation(waterShader.shaderProgram, "view"), 1, GL_FALSE, glm::value_ptr(view));
    glUniformMatrix4fv(glGetUniformLocation(waterShader.shaderProgram, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
    
    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, reflectionTexture);
    glUniform1i(glGetUniformLocation(waterShader.shaderProgram, "reflectionTexture"), 2);

    glm::mat4 modelWater = glm::translate(glm::mat4(1.0f), glm::vec3(5.0f, -14.5f, 15.0f)); 
    modelWater = glm::scale(modelWater, glm::vec3(10.0f, 1.0f, 10.0f)); 
    glUniformMatrix4fv(glGetUniformLocation(waterShader.shaderProgram, "model"), 1, GL_FALSE, glm::value_ptr(modelWater));
    glUniform1f(glGetUniformLocation(waterShader.shaderProgram, "time"), (float)glfwGetTime());
    myWater.Draw(waterShader);

    // Draw Skybox 
    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    mySkyBox.Draw(skyboxShader, view, projection, time);
 
    // Wireframe
    if (showWireframe) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

    // Draw Window Glow
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE); 
    glDepthMask(GL_FALSE);

    glowShader.useShaderProgram();
    glUniformMatrix4fv(glGetUniformLocation(glowShader.shaderProgram, "view"), 1, GL_FALSE, glm::value_ptr(view));
    glUniformMatrix4fv(glGetUniformLocation(glowShader.shaderProgram, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
    glUniform1f(glGetUniformLocation(glowShader.shaderProgram, "time"), (float)glfwGetTime());
    glUniform3f(glGetUniformLocation(glowShader.shaderProgram, "glowColor"), 1.0f, 0.8f, 0.3f); 

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, windowGlowTexture);
    glUniform1i(glGetUniformLocation(glowShader.shaderProgram, "glowTexture"), 0);

    glm::vec3 glowPos = glm::vec3(-2.8f, -11.0f, -12.5f);
    glm::mat4 glowModel = glm::translate(glm::mat4(1.0f), glowPos);
    
    glm::vec3 cameraPos = mySocketCamera.getPosition();
    glm::vec3 look = glm::normalize(cameraPos - glowPos);
    look.y = 0.0f;
    look = glm::normalize(look);
    glm::vec3 right = glm::normalize(glm::cross(glm::vec3(0.0f, 1.0f, 0.0f), look));
    
    glowModel[0] = glm::vec4(right, 0.0f);
    glowModel[1] = glm::vec4(0.0f, 1.0f, 0.0f, 0.0f);
    glowModel[2] = glm::vec4(look, 0.0f);
    
    glowModel = glm::scale(glowModel, glm::vec3(1.5f, 2.0f, 1.0f));
    
    glUniformMatrix4fv(glGetUniformLocation(glowShader.shaderProgram, "model"), 1, GL_FALSE, glm::value_ptr(glowModel));

    glBindVertexArray(glowVAO);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    glBindVertexArray(0);

    glDepthMask(GL_TRUE);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); 
    
    // Draw Rain
    if (showRain) {
        rainShader.useShaderProgram();
        glUniformMatrix4fv(glGetUniformLocation(rainShader.shaderProgram, "view"), 1, GL_FALSE, glm::value_ptr(view));
        glUniformMatrix4fv(glGetUniformLocation(rainShader.shaderProgram, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
        glUniform1f(glGetUniformLocation(rainShader.shaderProgram, "time"), time);

        glBindVertexArray(rainVAO);
        glLineWidth(2.0f); 
        glDrawArrays(GL_LINES, 0, rainParticles.size());
        glBindVertexArray(0);
    }
}

void cleanup() {
    glfwDestroyWindow(glWindow);
    glfwTerminate();
}

int main(int argc, const char* argv[]) {

    if (!initOpenGLWindow()) {
        glfwTerminate();
        return 1;
    }

    initResources();
    initFBO();
    initReflectionFBO();
    initRain();

    double lastTimeStamp = glfwGetTime();

    while (!glfwWindowShouldClose(glWindow)) {
        double currentTimeStamp = glfwGetTime();
        double delta = currentTimeStamp - lastTimeStamp;
        lastTimeStamp = currentTimeStamp;

        updateCameraAnimation(delta);
        processMovement();
        renderScene();

        glfwSwapBuffers(glWindow);
        glfwPollEvents();
    }

    cleanup();
    return 0;
}
