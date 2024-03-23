#version 400

uniform sampler2D texture;

in vec2 texcoord_final;
flat in uint texid_final;

void main()
{
    vec4 c = texture2D (texture, texcoord_final);
	if (c.w == 0) {
		discard;
	}
    gl_FragColor = c;
}