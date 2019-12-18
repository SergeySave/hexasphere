#version 330 core
in vec3 outPos;
in vec3 outNorm;
in vec2 outUV;
in vec4 outColor;

out vec4 FragColor;

uniform sampler2D texture_diffuse1;
uniform vec3 viewPos;

void main()
{
    // ambient
    float ambientStrength = 0.2;
    vec3 ambientColor = vec3(1.0, 1.0, 1.0);
    vec3 lightColor = vec3(1.0, 1.0, 0.8);
    vec3 ambient = ambientStrength * ambientColor;

    // diffuse
    vec3 norm = normalize(outNorm);
    vec3 lightDir = normalize(vec3(1.0, 1.0, 1.0));
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    // specular
    float specularStrength = 0.1;
    vec3 viewDir = normalize(viewPos - outPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
    vec3 specular = specularStrength * spec * lightColor;

    //Output Color
    FragColor = vec4((ambient + diffuse + specular), 1.0) * texture(texture_diffuse1, outUV) * outColor;
}