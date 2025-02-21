package org.mario;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {

    private long glfwWindow;

        // The window handle
        private static Window window = null;

        private Window() {
            int width = 1920;
            int height = 1080;
            String title = "Mario";
        }

        public static Window get() {
            if (Window.window == null) {
                Window.window = new Window();
            }
            return Window.window;
        }

        public void run() {
            System.out.println("Hello LWJGL " + Version.getVersion() + "!");

            init();
            loop();

            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(glfwWindow);
            glfwDestroyWindow(glfwWindow);

            // Terminate GLFW and free the error callback
            glfwTerminate();
            Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        }

        private void init() {
            // Setup an error callback. The default implementation
            // will print the error message in System.err.
            GLFWErrorCallback.createPrint(System.err).set();

            // Initialize GLFW. Most GLFW functions will not work before doing this.
            if ( !glfwInit() ) {
                throw new IllegalStateException("Unable to initialize GLFW");
            }

            // Configure GLFW
            glfwDefaultWindowHints(); // optional, the current window hints are already the default
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE); // sets the window to maxinmum size for convience

            // Create the window
            glfwWindow = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
            if ( glfwWindow == NULL ) {
                throw new RuntimeException("Failed to create the GLFW window");
            }
            
            // Setup a key callback. It will be called every time a key is pressed, repeated or released.
            glfwSetKeyCallback(glfwWindow, (window, key, scancode, action, mods) -> {
                if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                    glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            });

            // Get the thread stack and push a new frame
            try ( MemoryStack stack = stackPush() ) {
                IntBuffer pWidth = stack.mallocInt(1); // int*
                IntBuffer pHeight = stack.mallocInt(1); // int*

                // Get the window size passed to glfwCreateWindow
                glfwGetWindowSize(glfwWindow, pWidth, pHeight);

                // Get the resolution of the primary monitor
                GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

                // Center the window
                assert vidmode != null;
                glfwSetWindowPos(
                        glfwWindow,
                        (vidmode.width() - pWidth.get(0)) / 2,
                        (vidmode.height() - pHeight.get(0)) / 2
                );
            } // the stack frame is popped automatically

            // Make the OpenGL context current
            glfwMakeContextCurrent(glfwWindow);
            // Enable v-sync
            glfwSwapInterval(1);

            // Make the window visible
            glfwShowWindow(glfwWindow);
        }

        private void loop() {
            // This line is critical for LWJGL's interoperation with GLFW's
            // OpenGL context, or any context that is managed externally.
            // LWJGL detects the context that is current in the current thread,
            // creates the GLCapabilities instance and makes the OpenGL
            // bindings available for use.
            GL.createCapabilities();

            // Set the clear color
            glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

            // Run the rendering loop until the user has attempted to close
            // the window or has pressed the ESCAPE key.
            while ( !glfwWindowShouldClose(glfwWindow) ) {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

                glfwSwapBuffers(glfwWindow); // swap the color buffers

                // Poll for window events. The key callback above will only be
                // invoked during this call.
                glfwPollEvents();
            }
        }
}
