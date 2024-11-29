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
import com.badlogic.gdx.graphics.Color
import kotlin.ranges.step



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
        height: Float,
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

    private fun calculateGravitationalForce(bodyA: Body, bodyB: Body): Vector2 {
        val distance = bodyB.position.dst(bodyA.position)
        val forceMagnitude = G * (bodyA.mass * bodyB.mass) / (distance * distance)
        val forceDirection = bodyB.position.cpy().sub(bodyA.position).nor()
        return forceDirection.scl(forceMagnitude)
    }

    private fun threeBodyProblem() {
        // Create three bodies
        val body1 = createBody("body1", "circle", BodyType.DynamicBody, -20f, 0f, 10f, 10f, 1f, 0f, 0f)
        val body2 = createBody("body2", "circle", BodyType.DynamicBody, 20f, 0f, 10f, 10f, 1f, 0f, 0f)
        val body3 = createBody("body3", "circle", BodyType.DynamicBody, 0f, 1f, 10f, 10f, 1f, 0f, 0f)

        // Initial velocities
        body1.linearVelocity = Vector2(0f, 0.5f)
        body2.linearVelocity = Vector2(0f, -0.5f)
        body3.linearVelocity = Vector2(0.5f, 0f)

        // Update loop
        Gdx.app.postRunnable {
            while (true) {
                val force12 = calculateGravitationalForce(body1, body2)
                val force13 = calculateGravitationalForce(body1, body3)
                val force21 = calculateGravitationalForce(body2, body1)
                val force23 = calculateGravitationalForce(body2, body3)
                val force31 = calculateGravitationalForce(body3, body1)
                val force32 = calculateGravitationalForce(body3, body2)

                body1.applyForceToCenter(force12.add(force13), true)
                body2.applyForceToCenter(force21.add(force23), true)
                body3.applyForceToCenter(force31.add(force32), true)

                world.step(1/60f, 6, 2)
            }
        }
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

        threeBodyProblem()
        shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = camera.combined
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.RED

        val bodyList = Array<Body>()
        world.getBodies(bodyList)

        for (body in bodyList) {
            val fixture = body.fixtureList.first
            when (fixture.shape) {
                is CircleShape -> {
                    val pos = body.position
                    val radius = fixture.shape.radius
                    shapeRenderer.circle(
                        pos.x + worldWidth/2,
                        pos.y + worldHeight/2,
                        radius
                    )
                }
                is PolygonShape -> {
                    val shape = fixture.shape as PolygonShape
                    val vertices = Array(shape.vertexCount) { Vector2() }
                    for (i in 0 until shape.vertexCount) {
                        shape.getVertex(i, vertices[i])
                        vertices[i].rotate(Math.toDegrees(body.angle.toDouble()).toFloat())
                        vertices[i].add(body.position)
                        vertices[i].add(worldWidth/2, worldHeight/2)
                    }
                    shapeRenderer.polygon(vertices.map { it.x }.toFloatArray(),
                        vertices.map { it.y }.toFloatArray())
                }
            }
        }
        shapeRenderer.end()

        world.step(1/60f, 6, 2)
    }

    override fun dispose() {
        shapeRenderer.dispose()
        world.dispose()
        debugRenderer.dispose()
    }
}
