package game;

import engine.GameObject;
import engine.IGameLogic;
import engine.MouseInput;
import engine.Window;
import engine.graph.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.*;

public class DummyGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;

    private final Vector4f cameraInc;

    private final Renderer renderer;

    private final Camera camera;

    private GameObject[] gameObjects = {};

    private Vector3f ambientLight;

    private PointLight pointLight;

    private DirectionalLight directionalLight;

    private SpotLight spotLight;

    private float lightAngle;

    private static final float CAMERA_POS_STEP = 0.05f;
    private static final float CAMERA_ROT_STEP = 5f;

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector4f(0, 0, 0, 0);
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        float reflectance = 0.4f;

        Mesh mesh = OBJLoader.loadMesh("/models/zelenski.obj");
        Texture texture = new Texture("/textures/zelenski.png");
        Material material = new Material(texture, reflectance);

        mesh.setMaterial(material);
        GameObject gameObject = new GameObject(mesh);
        gameObject.setScale(0.5f);
        gameObject.setPosition(0, 0, -2);
        gameObjects = new GameObject[]{gameObject};

        ambientLight = new Vector3f(0.3f, 0.3f, 0.3f);

        // Point light
        Vector3f lightColor = new Vector3f(1, 1, 1);
        Vector3f lightPosition = new Vector3f(0, 0, 1);
        float lightIntensity = 1.0f;
        pointLight = new PointLight(lightColor, lightPosition, lightIntensity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);

        // Directional light
        Vector3f dirLightColor = new Vector3f(1, 1, 1);
        Vector3f dirLightPosition = new Vector3f(0, 0, 1);
        float dirLightIntensity = 1.0f;
        directionalLight = new DirectionalLight(dirLightColor, dirLightPosition, dirLightIntensity);

        // Spot light
        Vector3f spotLightColor = new Vector3f(1, 1, 1);
        Vector3f spotLightPosition = new Vector3f(0, 3, 2);
        float spotLightIntensity = 1.0f;
        PointLight spotPointLight = new PointLight(spotLightColor, spotLightPosition, spotLightIntensity);
        PointLight.Attenuation spotAtt = new PointLight.Attenuation(0.0f, 0.0f, 0.02f);
        spotPointLight.setAttenuation(att);
        Vector3f coneDir = new Vector3f(0, 0, -1);
        float cutoff = (float) Math.cos(Math.toRadians(140));
        spotLight = new SpotLight(spotPointLight, coneDir, cutoff);
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        cameraInc.set(0, 0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.y = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Q)) {
            cameraInc.w = -1;
        } else if (window.isKeyPressed(GLFW_KEY_E)) {
            cameraInc.w = 1;
        }

        if (window.isKeyPressed(GLFW_KEY_UP)) {
            this.pointLight.getPosition().z -= 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            this.pointLight.getPosition().z += 0.1f;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            this.pointLight.getPosition().x -= 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            this.pointLight.getPosition().x += 0.1f;
        }
        if (window.isKeyPressed(GLFW_KEY_BACKSLASH)) {
            this.pointLight.getPosition().y -= 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT_BRACKET)) {
            this.pointLight.getPosition().y += 0.1f;
        }

        float verticalVelocity = mouseInput.getWheelVerticalVelocity();
        float horizontalVelocity = mouseInput.getWheelHorizontalVelocity();

        if (Math.abs(verticalVelocity) > 0.4) {
            gameObjects[0].setScale(gameObjects[0].getScale() + verticalVelocity / 100);
        }

        if (Math.abs(horizontalVelocity) > 0.4) {
            Vector3f rot = gameObjects[0].getRotation();
            gameObjects[0].setRotation(rot.x, rot.y - horizontalVelocity, rot.z);
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput) {
        // Update camera position
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);
        camera.moveRotation(0, 0, cameraInc.w * CAMERA_ROT_STEP);

        // Update camera based on mouse            
        if (mouseInput.isLeftButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        // Update directional light direction, intensity and color
        lightAngle += 1.1f;
        if (lightAngle > 90) {
            directionalLight.setIntensity(0);
            if (lightAngle >= 360) {
                lightAngle = -90;
            }
        } else if (lightAngle <= -80 || lightAngle >= 80) {
            float factor = 1 - (Math.abs(lightAngle) - 80) / 10.0f;
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }
        double angRad = Math.toRadians(lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);
    }

    @Override
    public void render(Window window) {
        renderer.render(window, camera, gameObjects, ambientLight, pointLight, spotLight, directionalLight);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        for (GameObject gameObject : gameObjects) {
            gameObject.getMesh().cleanUp();
        }
    }

}
