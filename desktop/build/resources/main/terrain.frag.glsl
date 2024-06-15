#ifdef GL_ES
#define LOW lowp
#define MED mediump
#define HIGH highp
precision highp float;
#else
#define MED
#define LOW
#define HIGH
#endif

// Varying values passed from the vertex shader
varying vec2 v_diffuseUV;
varying float v_worldHeight; // Height passed from vertex shader

// Textures
uniform sampler2D u_diffuseBaseTexture; // Grass texture
uniform sampler2D u_diffuseHeightTexture; // Water texture

varying vec3 v_normal;

#if numDirectionalLights > 0
struct DirectionalLight
{
    vec3 color;
    vec3 direction;
};
#endif
uniform DirectionalLight u_dirLights[numDirectionalLights];

uniform vec3 u_ambientLight;

void main(void) {
    vec4 grass = texture2D(u_diffuseBaseTexture, v_diffuseUV);
    vec4 water = texture2D(u_diffuseHeightTexture, v_diffuseUV);

    // Define the range within which blending occurs
    float blendStart = -0.01; // Start blending from this height
    float blendEnd = 0.01; // End blending at this height

    // Calculate blend factor using smoothstep for a smooth transition
    float blendFactor = smoothstep(blendStart, blendEnd, v_worldHeight);



    // Blend textures based on the blend factor
    vec4 diffuse = mix(grass, water, blendFactor);

    vec3 diffuseLight = vec3(0);

    //#if numDirectionalLights > 0
    // The dot product gives us the angle between the light direction and surface normal
    // The closer the dot product is to 1.0, the less the angle is between vectors, and the brighter the light
    // will be
    diffuseLight = u_dirLights[0].color * (dot(normalize(-u_dirLights[0].direction), v_normal));
    //#endif
    diffuseLight += u_ambientLight;

    // Output the final color
    gl_FragColor = diffuse;
}

