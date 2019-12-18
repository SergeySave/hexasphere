#version 330 core
layout (location = 0) in vec3 aPos;   // the position variable has attribute position 0
layout (location = 1) in vec3 aColor; // the color variable has attribute position 1
layout (location = 2) in vec2 aUV; // the uv variable has attribute position 2

uniform mat4 uCamera;
uniform mat4 uModel;

out vec3 ourColor; // output a color to the fragment shader
out vec2 outUV; // output a uv to the fragment shader

void main()
{
    gl_Position = uCamera * uModel * vec4(aPos, 1.0);
    ourColor = aColor; // set ourColor to the input color we got from the vertex data
    outUV = aUV;
}