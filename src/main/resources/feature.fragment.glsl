#version 330 core
in vec2 outUV;
out vec4 FragColor;

uniform sampler2D texture1;

void main()
{
    FragColor = vec4(ourColor, 1.0) * texture(texture1, outUV);
}