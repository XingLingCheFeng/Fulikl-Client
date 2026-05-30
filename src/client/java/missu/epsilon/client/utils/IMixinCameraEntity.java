package missu.epsilon.client.utils;

public interface IMixinCameraEntity {
	float getCameraPitch();
	float getCameraYaw();

	void setCameraPitch(float pitch);
	void setCameraYaw(float yaw);
}
