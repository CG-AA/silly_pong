package io.github.some_example_name

import com.badlogic.gdx.Application
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Array



/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Main : ApplicationAdapter() {
    // GDX world class, used for physics simulation management
    private lateinit var world: World
    // GDX renderer class, used for rendering and drawing outlines of physics objects
    private lateinit var debugRenderer: Box2DDebugRenderer
    // GDX camera class
    private lateinit var camera: OrthographicCamera
    // body map
    private val bodies = mutableMapOf<String, Body>()
    // shape renderer
    private lateinit var shapeRenderer: ShapeRenderer

    private var worldHeight = 0f
    private var worldWidth = 0f
    private val screenRatio = 0.5625f
    private var gravity = -9.81f
    private val G = 6.67430e-11f

    // Using width as diameter if shape is circle
    private fun createBody(
        name: String,
        shape: String, // "box" or "circle"
        bodyType: BodyType,
        x: Float,
        y: Float,
        width: Float,
        height: Float = 1f,
        density: Float = 1.0f,
        friction: Float = 0f,
        restitution: Float = 1f
    ): Body {
        val bodyDef = BodyDef()
        bodyDef.type = bodyType
        bodyDef.position.set(x, y)
        val body = world.createBody(bodyDef)

        val fixtureDef = FixtureDef()
        fixtureDef.density = density
        fixtureDef.friction = friction
        fixtureDef.restitution = restitution

        when (shape.lowercase()) {
            "box" -> {
                val boxShape = PolygonShape()
                boxShape.setAsBox(width/2, height/2)
                fixtureDef.shape = boxShape
                body.createFixture(fixtureDef)
                boxShape.dispose()
            }
            "circle" -> {
                val circleShape = CircleShape()
                circleShape.radius = width/2 // Using width as diameter
                fixtureDef.shape = circleShape
                body.createFixture(fixtureDef)
                circleShape.dispose()
            }
        }

        bodies[name] = body
        return body
    }

    private fun calcWorldSize() {
        val screenWidth = Gdx.graphics.width.toFloat()
        val screenHeight = Gdx.graphics.height.toFloat()
        Gdx.app.debug("screenWidth", screenWidth.toString())
        Gdx.app.debug("screenHeight", screenHeight.toString())
        // make the world 16:9
        if(screenWidth / screenHeight > screenRatio) {
            worldHeight = screenHeight
            worldWidth = screenHeight * screenRatio
        } else {
            worldWidth = screenWidth
            worldHeight = screenWidth / screenRatio
        }
        Gdx.app.debug("worldWidth", worldWidth.toString())
        Gdx.app.debug("worldHeight", worldHeight.toString())
    }

    // Create default bodies
    private fun createBodies() {
        val screenWidth = Gdx.graphics.width.toFloat()
        val screenHeight = Gdx.graphics.height.toFloat()
        // edges
        createBody("bottom_edge", "box", BodyType.StaticBody, 0f, -worldHeight/2, worldWidth, 0f)
        createBody("top_edge", "box", BodyType.StaticBody, 0f, worldHeight/2, worldWidth, 0f)
        createBody("left_edge", "box", BodyType.StaticBody, -worldWidth/2, 0f, 0f, worldHeight)
        createBody("right_edge", "box", BodyType.StaticBody, worldWidth/2, 0f, 0f, worldHeight)
        // bouncy ball >w<
        createBody("ball0", "circle", BodyType.DynamicBody, 0f, worldHeight/2*0.8f, 0.1f * worldWidth, 0.1f * worldWidth, 1f, 0f, 1000f)
        createBody("ball1", "circle", BodyType.DynamicBody, 0f, worldHeight/2*0.6f, 0.1f * worldWidth, 0.1f * worldWidth, 1f, 0f, 1000f)
    }

    private fun testBall() {
        createBody("testBall0", "circle", BodyType.StaticBody, 0f, 0f, 0.1f * worldWidth)
        createBody("testBox0", "box", BodyType.StaticBody, 0.01f * worldWidth, 0.01f * worldWidth, 0.05f * worldWidth, 0.05f * worldWidth)
    }

    private fun calculateGravitationalForce(bodyA: Body, bodyB: Body): Vector2 {
        val distance = bodyB.position.dst(bodyA.position)
        val forceMagnitude = G * (bodyA.mass * bodyB.mass) / (distance * distance)
        val forceDirection = bodyB.position.cpy().sub(bodyA.position).nor()
        return forceDirection.scl(forceMagnitude)
    }

    private fun threeBodyProblem() {
        // Scale factor to make simulation visible on screen
        val scale = worldWidth / 50f

        // Create boundaries
//        createBody("bottom_edge", "box", BodyType.StaticBody, 0f, -worldHeight/2+1, worldWidth, 2f)
//        createBody("top_edge", "box", BodyType.StaticBody, 0f, worldHeight/2-1, worldWidth, 2f)
//        createBody("left_edge", "box", BodyType.StaticBody, -worldWidth/2+1, 0f, 2f, worldHeight)
//        createBody("right_edge", "box", BodyType.StaticBody, worldWidth/2-1, 0f, 2f, worldHeight)

        // Create bodies scaled to screen size
        val body1 = createBody("body1", "circle", BodyType.DynamicBody, -5f * scale, 0f, scale, scale, 1f, 0f, 0f)
        val body2 = createBody("body2", "circle", BodyType.DynamicBody, 5f * scale, 0f, scale, scale, 1f, 0f, 0f)
        val body3 = createBody("body3", "circle", BodyType.DynamicBody, 0f, scale, scale, scale, 1f, 0f, 0f)

        // Set initial velocities
        body1.linearVelocity = Vector2(0f, scale * 0.5f)
        body2.linearVelocity = Vector2(0f, scale * -0.5f)
        body3.linearVelocity = Vector2(scale * 0.5f, 0f)

        bodies["body1"] = body1
        bodies["body2"] = body2
        bodies["body3"] = body3
    }


    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        Box2D.init()
        calcWorldSize()
//        gravity = -9.81f * worldHeight / 100f
        gravity = 0f
        // The vector2 is gravity, bool is improve performance by not simulating inactive bodies.
        world = World(Vector2(0f, gravity), true)
        debugRenderer = Box2DDebugRenderer()
        // Viewport size
        camera = OrthographicCamera(worldWidth, worldHeight)
        camera.position.set(0f, 0f, 0f)

        shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = camera.combined
//        testBall()
        threeBodyProblem()
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        for (body in bodies) {
            val fixture = body.value.fixtureList.first()
            shapeRenderer.color = if (body.key.startsWith("test")) {
                com.badlogic.gdx.graphics.Color.BLUE
            } else {
                com.badlogic.gdx.graphics.Color.RED
            }

            when (fixture.shape) {
                is CircleShape -> {
                    val pos = body.value.position
                    val radius = fixture.shape.radius
                    shapeRenderer.circle(
                        pos.x,  // Remove worldWidth/2
                        pos.y,  // Remove worldHeight/2
                        radius
                    )
                }
                is PolygonShape -> {
                    val shape = fixture.shape as PolygonShape
                    val vertexCount = shape.vertexCount
                    val vertices = Array<Vector2>(vertexCount)

                    for (i in 0 until vertexCount) {
                        vertices.add(Vector2())
                        shape.getVertex(i, vertices.get(i))
                        vertices.get(i).rotate(Math.toDegrees(body.value.angle.toDouble()).toFloat())
                        vertices.get(i).add(body.value.position)
                        // Remove the worldWidth/2 and worldHeight/2 additions
                    }

                    for (i in 1 until vertexCount - 1) {
                        shapeRenderer.triangle(
                            vertices.get(0).x, vertices.get(0).y,
                            vertices.get(i).x, vertices.get(i).y,
                            vertices.get(i + 1).x, vertices.get(i + 1).y
                        )
                    }
                }
            }
        }
        shapeRenderer.end()
        // Update physics for three body problem
        this@Main.bodies["body1"]?.let { body1 ->
            this@Main.bodies["body2"]?.let { body2 ->
                this@Main.bodies["body3"]?.let { body3 ->
                    val force12 = calculateGravitationalForce(body1, body2)
                    val force13 = calculateGravitationalForce(body1, body3)
                    val force21 = calculateGravitationalForce(body2, body1)
                    val force23 = calculateGravitationalForce(body2, body3)
                    val force31 = calculateGravitationalForce(body3, body1)
                    val force32 = calculateGravitationalForce(body3, body2)

                    body1.applyForceToCenter(force12.add(force13.cpy()), true)
                    body2.applyForceToCenter(force21.add(force23.cpy()), true)
                    body3.applyForceToCenter(force31.add(force32.cpy()), true)
                }
            }
        }

        world.step(1/60f, 6, 2)
    }

    override fun dispose() {
        shapeRenderer.dispose()
        world.dispose()
        debugRenderer.dispose()
    }
}
